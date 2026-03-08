package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.api.PurchaseRequest;
import de.isolveproblems.freeframe.api.PurchaseResult;
import de.isolveproblems.freeframe.economy.EconomyChargeResult;
import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import de.isolveproblems.freeframe.utils.PurchaseWindowLimiter;
import org.bukkit.Material;
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
        this.processPurchase((Player) event.getWhoClicked(), holder, event.getRawSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (this.isFreeFrameInventory(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    private void processPurchase(Player player, FreeFrameInventoryHolder holder, int rawSlot) {
        ItemStack templateItem = holder == null ? null : holder.getSaleItem();
        if (holder == null || templateItem == null || this.isAir(templateItem.getType())) {
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

        if (!this.freeframe.isItemAllowed(templateItem.getType())) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.items.blockedMessage",
                "%prefix% &cThis item type is blocked by item policy.",
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
        PurchaseProfile profile = this.resolveProfile(frameData, rawSlot, holder);

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
                profile.getAmount(),
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

        String lockKey = player.getUniqueId().toString() + ":" + frameData.getId();
        if (!this.freeframe.getTransactionGuard().tryAcquire(lockKey)) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.busy",
                "%prefix% &cThis frame is processing another transaction.",
                player
            ));
            return;
        }

        try {
            PurchaseResult result = this.freeframe.getPurchaseProcessor().process(
                new PurchaseRequest(player, frameData, templateItem, profile)
            );

            if (result.getStatus() == PurchaseResult.Status.BLOCKED || result.getStatus() == PurchaseResult.Status.ERROR) {
                player.sendMessage(this.freeframe.getMessage(result.getMessagePath(), result.getFallbackMessage(), player));
                return;
            }

            if (result.getStatus() == PurchaseResult.Status.PREVIEW) {
                player.sendMessage(this.freeframe.getMessage(result.getMessagePath(), result.getFallbackMessage(), player));
                return;
            }

            int amount = Math.max(1, result.getPurchasedAmount());
            double price = Math.max(0.0D, result.getFinalPrice());
            String currency = this.resolveCurrency(frameData.getCurrency(), holder.getCurrency());

            boolean charged = this.chargePlayer(player, price, currency);
            if (price > 0.0D && !charged) {
                return;
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

            this.giveItem(player, templateItem, amount);
            frameData.setCurrency(currency);

            if (price > 0.0D && charged) {
                frameData.addRevenue(price);
                this.payOwner(player, frameData, price);
            }

            this.freeframe.getMetricsTracker().incrementPurchases();
            this.freeframe.getStatisticsService().recordPurchase(player, frameData, amount, price);
            this.freeframe.getWebhookExportService().sendPurchase(player, frameData, amount, price, price > 0.0D ? "charged" : "free");
            this.sendSuccessMessage(player, amount, price, currency);
            this.freeframe.getAuditLogger().logPurchase(player, frameData, amount, price, price > 0.0D ? "charged" : "free");
            this.freeframe.getDisplayService().refresh(frameData);
            this.freeframe.getFrameRegistry().saveToConfig();

            if (this.freeframe.getPluginConfig().getBoolean("freeframe.gui.closeAfterPurchase", false)) {
                player.closeInventory();
            }

            if (autoRefilled) {
                this.freeframe.getAuditLogger().logPurchase(player, frameData, 0, 0.0D, "auto-refill");
            }
        } finally {
            this.freeframe.getTransactionGuard().release(lockKey);
        }
    }

    private PurchaseProfile resolveProfile(FreeFrameData frameData, int rawSlot, FreeFrameInventoryHolder holder) {
        PurchaseProfile profile = frameData.findProfileBySlot(rawSlot);
        if (profile != null) {
            return profile;
        }
        return new PurchaseProfile(rawSlot, Math.max(1, holder.getSaleItem().getAmount()), holder.getPrice(), "");
    }

    private boolean chargePlayer(Player player, double price, String currency) {
        if (price <= 0.0D) {
            return false;
        }

        boolean allowWithoutVault = this.freeframe.getPluginConfig().getBoolean("freeframe.economy.allowWithoutVault", false);
        if (!this.freeframe.getEconomyService().isAvailable() && !allowWithoutVault) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.economyUnavailable",
                "%prefix% &cEconomy is not available right now.",
                player
            ));
            return false;
        }

        if (!this.freeframe.getEconomyService().isAvailable()) {
            return true;
        }

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
            return false;
        }

        if (chargeResult.getStatus() == EconomyChargeResult.Status.ECONOMY_UNAVAILABLE
            || chargeResult.getStatus() == EconomyChargeResult.Status.ERROR) {
            if (!allowWithoutVault) {
                player.sendMessage(this.freeframe.getMessage(
                    "freeframe.purchase.economyUnavailable",
                    "%prefix% &cEconomy is not available right now.",
                    player
                ));
                return false;
            }
            return true;
        }

        return true;
    }

    private void payOwner(Player buyer, FreeFrameData frameData, double price) {
        boolean payOwnerOnSelf = this.freeframe.getPluginConfig().getBoolean("freeframe.economy.payOwnerOnSelfPurchase", false);
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.economy.payOwner", true)) {
            return;
        }

        if (!payOwnerOnSelf && frameData.isOwnedBy(buyer.getUniqueId().toString())) {
            return;
        }

        EconomyChargeResult payoutResult = this.freeframe.getEconomyService().depositToOwner(
            frameData.getOwnerUuid(),
            frameData.getOwnerName(),
            price
        );
        if (payoutResult.getStatus() == EconomyChargeResult.Status.SUCCESS) {
            this.freeframe.getMetricsTracker().incrementOwnerPayouts();
        }
    }

    private void giveItem(Player player, ItemStack templateItem, int totalAmount) {
        int remainingAmount = Math.max(1, totalAmount);
        int maxStackSize = Math.max(1, templateItem.getMaxStackSize());
        Map<Integer, ItemStack> overflow = new HashMap<Integer, ItemStack>();

        while (remainingAmount > 0) {
            int stackAmount = Math.min(maxStackSize, remainingAmount);
            ItemStack reward = templateItem.clone();
            reward.setAmount(stackAmount);
            overflow.putAll(player.getInventory().addItem(reward));
            remainingAmount -= stackAmount;
        }

        if (overflow.isEmpty()) {
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

        for (ItemStack item : overflow.values()) {
            if (item != null && !this.isAir(item.getType())) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        player.sendMessage(this.freeframe.getMessage(
            "freeframe.purchase.inventoryDrop",
            "%prefix% &eYour inventory was full. Remaining items were dropped.",
            player
        ));
    }

    private void sendSuccessMessage(Player player, int amount, double price, String currency) {
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("%amount%", String.valueOf(amount));
        tokens.put("%price%", this.formatPrice(price));
        tokens.put("%currency%", currency);

        if (price > 0.0D) {
            this.sendTemplatedMessage(
                player,
                "freeframe.purchase.success",
                "%prefix% &aPurchased &e%amount%x &afor &e%currency%%price%&a.",
                tokens
            );
            return;
        }

        this.sendTemplatedMessage(
            player,
            "freeframe.purchase.free",
            "%prefix% &aYou received &e%amount%x &afor free.",
            tokens
        );
    }

    private void sendTemplatedMessage(Player player, String path, String fallback, Map<String, String> tokens) {
        String template = this.freeframe.getMessage(path, fallback, player);
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            template = template.replace(token.getKey(), token.getValue());
        }
        player.sendMessage(template);
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
