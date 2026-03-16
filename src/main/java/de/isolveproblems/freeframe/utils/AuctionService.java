package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.SaleMode;
import de.isolveproblems.freeframe.economy.EconomyChargeResult;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AuctionService {
    private final FreeFrame freeframe;
    private final Map<String, Integer> offlineRetryCountByFrame = new HashMap<String, Integer>();
    private BukkitTask task;

    public AuctionService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void start() {
        this.stop();
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_AUCTION_ENABLED)) {
            return;
        }
        long intervalTicks = Math.max(20L, this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_AUCTION_TICKINTERVALTICKS));
        this.task = Bukkit.getScheduler().runTaskTimer(this.freeframe, new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    public boolean startAuction(FreeFrameData frameData, long durationMillis, double minBid) {
        if (frameData == null || durationMillis <= 0L) {
            return false;
        }
        this.clearOfflineRetryCount(frameData.getId());
        frameData.setSaleMode(SaleMode.AUCTION);
        frameData.setAuctionEndAt(System.currentTimeMillis() + durationMillis);
        frameData.setAuctionMinBid(Math.max(0.0D, minBid));
        frameData.setHighestBid(0.0D);
        frameData.setHighestBidderUuid("");
        frameData.setHighestBidderName("");
        this.freeframe.getFrameRegistry().saveToConfig();
        return true;
    }

    public void stopAuction(FreeFrameData frameData) {
        if (frameData == null) {
            return;
        }
        this.clearOfflineRetryCount(frameData.getId());
        frameData.setSaleMode(SaleMode.INSTANT);
        frameData.clearAuctionState();
        this.freeframe.getFrameRegistry().saveToConfig();
    }

    public String placeBid(Player player, FreeFrameData frameData, double bidAmount) {
        if (player == null || frameData == null) {
            return this.freeframe.getMessage("freeframe.auction.invalid", "%prefix% &cInvalid auction request.", player);
        }
        if (frameData.getSaleMode() != SaleMode.AUCTION) {
            return this.freeframe.getMessage("freeframe.auction.notEnabled", "%prefix% &cThis frame is not in auction mode.", player);
        }
        if (!frameData.isAuctionActive(System.currentTimeMillis())) {
            return this.freeframe.getMessage("freeframe.auction.ended", "%prefix% &cAuction already ended.", player);
        }

        double minimum = Math.max(frameData.getAuctionMinBid(), frameData.getHighestBid() + 0.01D);
        if (bidAmount < minimum) {
            return this.freeframe.getMessage(
                "freeframe.auction.bidTooLow",
                "%prefix% &cBid too low. Minimum is &e%price%&c.",
                player
            ).replace("%price%", format(minimum));
        }

        frameData.placeBid(player.getUniqueId().toString(), player.getName(), bidAmount);
        this.freeframe.getFrameRegistry().saveToConfig();
        if (this.freeframe.getTransactionJournalService() != null) {
            this.freeframe.getTransactionJournalService().logBid("bid-" + System.currentTimeMillis(), player, frameData, bidAmount);
        }
        return this.freeframe.getMessage(
            "freeframe.auction.bidAccepted",
            "%prefix% &aBid accepted: &e%price%&a.",
            player
        ).replace("%price%", format(bidAmount));
    }

    public String describe(FreeFrameData frameData) {
        if (frameData == null) {
            return "unknown";
        }
        long remaining = Math.max(0L, frameData.getAuctionEndAt() - System.currentTimeMillis());
        long seconds = remaining / 1000L;
        return "highest=" + format(frameData.getHighestBid())
            + " bidder=" + (frameData.getHighestBidderName() == null || frameData.getHighestBidderName().isEmpty() ? "-" : frameData.getHighestBidderName())
            + " endsIn=" + seconds + "s";
    }

    public void tick() {
        List<FreeFrameData> frames = this.freeframe.getFrameRegistry().listFrames();
        long now = System.currentTimeMillis();
        for (FreeFrameData frameData : frames) {
            if (!frameData.isAuctionFinished(now)) {
                continue;
            }
            this.settle(frameData);
        }
    }

    private void settle(FreeFrameData frameData) {
        if (frameData.getHighestBid() <= 0.0D || frameData.getHighestBidderUuid() == null || frameData.getHighestBidderUuid().isEmpty()) {
            this.clearOfflineRetryCount(frameData.getId());
            frameData.setSaleMode(SaleMode.INSTANT);
            frameData.clearAuctionState();
            this.freeframe.getFrameRegistry().saveToConfig();
            return;
        }

        Player winner = null;
        try {
            winner = Bukkit.getPlayer(UUID.fromString(frameData.getHighestBidderUuid()));
        } catch (Exception ignored) {
            winner = null;
        }

        if (winner == null || !winner.isOnline()) {
            int retries = this.incrementOfflineRetryCount(frameData.getId());
            int maxRetries = Math.max(0, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_AUCTION_OFFLINEMAXEXTENSIONS));
            if (retries > maxRetries) {
                this.freeframe.getAlertService().alertAuctionIssue(frameData.getId(), "winner-offline-max-retries");
                this.clearOfflineRetryCount(frameData.getId());
                frameData.setSaleMode(SaleMode.INSTANT);
                frameData.clearAuctionState();
                this.freeframe.getFrameRegistry().saveToConfig();
                this.freeframe.getDisplayService().refresh(frameData);
                return;
            }

            this.freeframe.getAlertService().alertAuctionIssue(frameData.getId(), "winner-offline");
            frameData.setAuctionEndAt(System.currentTimeMillis() + Math.max(60_000L, this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_AUCTION_OFFLINEGRACEMILLIS)));
            this.freeframe.getFrameRegistry().saveToConfig();
            return;
        }

        if (frameData.getStock() <= 0) {
            this.clearOfflineRetryCount(frameData.getId());
            this.freeframe.getAlertService().alertAuctionIssue(frameData.getId(), "out-of-stock");
            frameData.setSaleMode(SaleMode.INSTANT);
            frameData.clearAuctionState();
            this.freeframe.getFrameRegistry().saveToConfig();
            return;
        }

        Material material;
        try {
            material = Material.valueOf(frameData.getItemType().toUpperCase(Locale.ENGLISH));
        } catch (Exception exception) {
            this.clearOfflineRetryCount(frameData.getId());
            this.freeframe.getAlertService().alertAuctionIssue(frameData.getId(), "invalid-item-type");
            frameData.setSaleMode(SaleMode.INSTANT);
            frameData.clearAuctionState();
            this.freeframe.getFrameRegistry().saveToConfig();
            return;
        }

        this.clearOfflineRetryCount(frameData.getId());

        EconomyChargeResult charge = this.freeframe.getEconomyService().charge(winner, frameData.getHighestBid());
        if (charge.getStatus() != EconomyChargeResult.Status.SUCCESS) {
            this.freeframe.getAlertService().alertAuctionIssue(frameData.getId(), "charge-failed");
            frameData.setSaleMode(SaleMode.INSTANT);
            frameData.clearAuctionState();
            this.freeframe.getFrameRegistry().saveToConfig();
            return;
        }

        ItemStack reward = new ItemStack(material, 1);
        Map<Integer, ItemStack> overflow = winner.getInventory().addItem(reward);
        if (!overflow.isEmpty()) {
            for (ItemStack overflowItem : overflow.values()) {
                if (overflowItem != null && overflowItem.getType() != null && !"AIR".equals(overflowItem.getType().name())) {
                    winner.getWorld().dropItemNaturally(winner.getLocation(), overflowItem);
                }
            }
            winner.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.inventoryDrop",
                "%prefix% &eYour inventory was full. Remaining items were dropped.",
                winner
            ));
        }
        frameData.consumeStock(1);
        frameData.addRevenue(frameData.getHighestBid());
        this.freeframe.getStatisticsService().recordPurchase(winner, frameData, 1, frameData.getHighestBid());
        winner.sendMessage(this.freeframe.formatMessage("%prefix% &aYou won auction &e" + frameData.getId() + " &afor &e" + format(frameData.getHighestBid())));

        frameData.setSaleMode(SaleMode.INSTANT);
        frameData.clearAuctionState();
        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refresh(frameData);
    }

    private int incrementOfflineRetryCount(String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return 1;
        }
        Integer current = this.offlineRetryCountByFrame.get(frameId);
        int next = current == null ? 1 : current.intValue() + 1;
        this.offlineRetryCountByFrame.put(frameId, Integer.valueOf(next));
        return next;
    }

    private void clearOfflineRetryCount(String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return;
        }
        this.offlineRetryCountByFrame.remove(frameId);
    }

    private static String format(double value) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, value));
    }
}
