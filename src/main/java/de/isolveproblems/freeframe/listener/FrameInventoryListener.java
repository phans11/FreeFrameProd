package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.economy.EconomyChargeResult;
import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import de.isolveproblems.freeframe.utils.ItemPolicy;
import de.isolveproblems.freeframe.utils.PurchaseWindowLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FrameInventoryListener implements Listener {
    private final FreeFrame freeframe;

    public FrameInventoryListener(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!this.isFreeFrameInventory(topInventory)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) {
            return;
        }

        if (!this.freeframe.isSaleSlot(event.getRawSlot())) {
            return;
        }

        FreeFrameInventoryHolder holder = (FreeFrameInventoryHolder) topInventory.getHolder();
        this.processPurchase((Player) event.getWhoClicked(), holder);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (this.isFreeFrameInventory(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    private void processPurchase(Player player, FreeFrameInventoryHolder holder) {
        ItemStack saleItem = holder == null ? null : holder.getSaleItem();
        if (holder == null || saleItem == null || this.isAir(saleItem.getType())) {
            return;
        }

        ItemPolicy.Decision itemDecision = this.freeframe.getItemPolicy().check(this.freeframe.getPluginConfig(), saleItem.getType());
        if (!itemDecision.isAllowed()) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.items.blockedMessage",
                "%prefix% &cThis item type is blocked by item policy.",
                player
            ));
            return;
        }

        FreeFrameData frameData = this.freeframe.getFrameRegistry().findById(holder.getFrameId());
        if (frameData == null || !frameData.isActive()) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.frame.inactive",
                "%prefix% &cThis FreeFrame is currently inactive.",
                player
            ));
            return;
        }

        Location frameLocation = this.resolveLocation(frameData);
        if (frameLocation != null && !this.freeframe.isLocationAllowed(frameLocation)) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.restrictions.denied",
                "%prefix% &cFreeFrame is disabled in this world/region.",
                player
            ));
            return;
        }

        if (!this.freeframe.canPlayerUseFrame(player, frameData)) {
            this.freeframe.getMetricsTracker().incrementDeniedAccess();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.access.denied",
                "%prefix% &cYou are not allowed to use this FreeFrame.",
                player
            ));
            return;
        }

        boolean autoRefilled = frameData.applyAutoRefillIfDue(System.currentTimeMillis());

        int amount = Math.max(1, saleItem.getAmount());
        if (frameData.getStock() < amount) {
            this.freeframe.getMetricsTracker().incrementStockOutHits();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.stockOut",
                "%prefix% &cThis frame is out of stock.",
                player
            ));
            return;
        }

        InteractionLimiter.LimitResult limitResult = this.freeframe.getInteractionLimiter().checkAndMark(
            player.getUniqueId(),
            frameData.getId(),
            this.freeframe.getPluginConfig().getLong("freeframe.cooldown.playerMillis", 300L),
            this.freeframe.getPluginConfig().getLong("freeframe.cooldown.frameMillis", 100L)
        );

        if (limitResult == InteractionLimiter.LimitResult.PLAYER_COOLDOWN) {
            this.freeframe.getMetricsTracker().incrementCooldownHits();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.cooldown.message",
                "%prefix% &cPlease wait before using another FreeFrame.",
                player
            ));
            return;
        }

        if (limitResult == InteractionLimiter.LimitResult.FRAME_RATE_LIMIT) {
            this.freeframe.getMetricsTracker().incrementFrameRateLimitHits();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.rateLimit.message",
                "%prefix% &cThis FreeFrame is currently rate-limited.",
                player
            ));
            return;
        }

        if (this.freeframe.getPluginConfig().getBoolean("freeframe.limits.enabled", false)) {
            PurchaseWindowLimiter.LimitState state = this.freeframe.getPurchaseWindowLimiter().checkAndConsume(
                player.getUniqueId(),
                amount,
                this.freeframe.getPluginConfig().getInt("freeframe.limits.maxItemsPerWindow", 64),
                this.freeframe.getPluginConfig().getLong("freeframe.limits.windowMillis", 600000L)
            );

            if (!state.isAllowed()) {
                this.freeframe.getMetricsTracker().incrementPurchaseLimitHits();
                Map<String, String> tokens = new HashMap<String, String>();
                tokens.put("%remaining%", String.valueOf(state.getRemaining()));
                tokens.put("%waitSeconds%", String.valueOf(Math.max(1L, state.getWaitMillis() / 1000L)));
                this.sendTemplatedMessage(
                    player,
                    "freeframe.purchase.limited",
                    "%prefix% &cPurchase limit reached. Try again later.",
                    tokens
                );
                return;
            }
        }

        double price = Math.max(0.0D, frameData.getPrice());
        String currency = this.resolveCurrency(frameData.getCurrency(), holder.getCurrency());

        boolean charged = false;
        if (price > 0.0D) {
            boolean allowWithoutVault = this.freeframe.getPluginConfig().getBoolean("freeframe.economy.allowWithoutVault", false);
            if (!this.freeframe.getEconomyService().isAvailable() && !allowWithoutVault) {
                player.sendMessage(this.freeframe.getMessage(
                    "freeframe.purchase.economyUnavailable",
                    "%prefix% &cEconomy is not available right now.",
                    player
                ));
                return;
            }

            if (this.freeframe.getEconomyService().isAvailable()) {
                EconomyChargeResult chargeResult = this.freeframe.getEconomyService().charge(player, price);
                if (chargeResult.getStatus() == EconomyChargeResult.Status.NOT_ENOUGH_MONEY) {
                    Map<String, String> tokens = new HashMap<String, String>();
                    tokens.put("%price%", this.formatPrice(price));
                    tokens.put("%currency%", currency);
                    this.sendTemplatedMessage(
                        player,
                        "freeframe.purchase.notEnoughMoney",
                        "%prefix% &cNot enough money. Required: &e%currency%%price%&c.",
                        tokens
                    );
                    return;
                }

                if (chargeResult.getStatus() == EconomyChargeResult.Status.ECONOMY_UNAVAILABLE
                    || chargeResult.getStatus() == EconomyChargeResult.Status.ERROR) {
                    if (!allowWithoutVault) {
                        player.sendMessage(this.freeframe.getMessage(
                            "freeframe.purchase.economyUnavailable",
                            "%prefix% &cEconomy is not available right now.",
                            player
                        ));
                        return;
                    }
                } else {
                    charged = true;
                }
            }
        }

        if (!frameData.consumeStock(amount)) {
            this.freeframe.getMetricsTracker().incrementStockOutHits();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.stockOut",
                "%prefix% &cThis frame is out of stock.",
                player
            ));
            return;
        }

        this.giveItem(player, saleItem);
        frameData.setCurrency(currency);

        if (price > 0.0D && charged) {
            frameData.addRevenue(price);

            boolean payOwnerOnSelf = this.freeframe.getPluginConfig().getBoolean("freeframe.economy.payOwnerOnSelfPurchase", false);
            if (this.freeframe.getPluginConfig().getBoolean("freeframe.economy.payOwner", true)
                && (payOwnerOnSelf || !frameData.isOwnedBy(player.getUniqueId().toString()))) {
                EconomyChargeResult payoutResult = this.freeframe.getEconomyService().depositToOwner(
                    frameData.getOwnerUuid(),
                    frameData.getOwnerName(),
                    price
                );
                if (payoutResult.getStatus() == EconomyChargeResult.Status.SUCCESS) {
                    this.freeframe.getMetricsTracker().incrementOwnerPayouts();
                }
            }

            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("%price%", this.formatPrice(price));
            tokens.put("%currency%", currency);
            this.sendTemplatedMessage(
                player,
                "freeframe.purchase.success",
                "%prefix% &aPurchased item for &e%currency%%price%&a.",
                tokens
            );
            this.freeframe.getAuditLogger().logPurchase(player, frameData, amount, price, "charged");
        } else {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.free",
                "%prefix% &aYou received this item for free.",
                player
            ));
            this.freeframe.getAuditLogger().logPurchase(player, frameData, amount, 0.0D, "free");
        }

        this.freeframe.getMetricsTracker().incrementPurchases();
        this.freeframe.getDisplayService().refresh(frameData);
        this.freeframe.getFrameRegistry().saveToConfig();
        if (this.freeframe.getPluginConfig().getBoolean("freeframe.gui.closeAfterPurchase", false)) {
            player.closeInventory();
        }

        if (autoRefilled) {
            this.freeframe.getAuditLogger().logPurchase(player, frameData, 0, 0.0D, "auto-refill");
        }
    }

    private void giveItem(Player player, ItemStack saleItem) {
        ItemStack reward = saleItem.clone();
        if (reward.getAmount() < 1) {
            reward.setAmount(1);
        }

        Map<Integer, ItemStack> remaining = player.getInventory().addItem(reward);
        if (remaining.isEmpty()) {
            return;
        }

        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.gui.dropOnFullInventory", true)) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.inventoryFull",
                "%prefix% &cYour inventory is full.",
                player
            ));
            return;
        }

        Location dropLocation = player.getLocation();
        for (ItemStack item : remaining.values()) {
            if (item != null && !this.isAir(item.getType())) {
                player.getWorld().dropItemNaturally(dropLocation, item);
            }
        }

        player.sendMessage(this.freeframe.getMessage(
            "freeframe.purchase.inventoryDrop",
            "%prefix% &eYour inventory was full. Remaining items were dropped.",
            player
        ));
    }

    private void sendTemplatedMessage(Player player, String path, String fallback, Map<String, String> tokens) {
        String template = this.freeframe.getPluginConfig().getString(path, fallback);
        if (template == null) {
            template = fallback;
        }

        for (Map.Entry<String, String> token : tokens.entrySet()) {
            template = template.replace(token.getKey(), token.getValue());
        }

        player.sendMessage(this.freeframe.formatMessage(template, player));
    }

    private String resolveCurrency(String preferred, String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) {
            return preferred.trim();
        }

        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback.trim();
        }

        String configured = this.freeframe.getPluginConfig().getString("freeframe.default.currency", "$");
        return configured == null || configured.trim().isEmpty() ? "$" : configured.trim();
    }

    private Location resolveLocation(FreeFrameData frameData) {
        if (frameData == null || frameData.getReference() == null) {
            return null;
        }

        World world = Bukkit.getWorld(frameData.getReference().getWorldName());
        if (world == null) {
            return null;
        }

        return new Location(world,
            frameData.getReference().getX(),
            frameData.getReference().getY(),
            frameData.getReference().getZ());
    }

    private boolean isFreeFrameInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof FreeFrameInventoryHolder;
    }

    private boolean isAir(Material material) {
        return material == null || "AIR".equals(material.name());
    }

    private String formatPrice(double price) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, price));
    }
}
