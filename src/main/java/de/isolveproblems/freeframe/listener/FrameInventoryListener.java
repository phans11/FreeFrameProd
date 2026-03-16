package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.api.PurchaseRequest;
import de.isolveproblems.freeframe.api.PurchaseResult;
import de.isolveproblems.freeframe.api.SaleMode;
import de.isolveproblems.freeframe.api.BuyerRiskProfile;
import de.isolveproblems.freeframe.api.CampaignEffect;
import de.isolveproblems.freeframe.economy.EconomyChargeResult;
import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import de.isolveproblems.freeframe.utils.PurchaseWindowLimiter;
import de.isolveproblems.freeframe.utils.SignedPurchaseToken;
import de.isolveproblems.freeframe.utils.TaxBreakdown;
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

        if (frameData.getSaleMode() == SaleMode.AUCTION) {
            player.sendMessage(this.freeframe.formatMessage("%prefix% &eAuction status: &f" + this.freeframe.getAuctionService().describe(frameData), player));
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
        if (this.freeframe.getModerationService().isFrameFrozen(frameData.getId())) {
            player.sendMessage(this.freeframe.formatMessage(
                "%prefix% &cThis shop is frozen: &e" + this.freeframe.getModerationService().frameRestrictionReason(frameData.getId()),
                player
            ));
            return;
        }
        if (this.freeframe.getModerationService().isPlayerRestricted(player.getUniqueId().toString())) {
            player.sendMessage(this.freeframe.formatMessage(
                "%prefix% &cYour shop actions are restricted: &e" + this.freeframe.getModerationService().playerRestrictionReason(player.getUniqueId().toString()),
                player
            ));
            return;
        }

        boolean autoRefilled = frameData.applyAutoRefillIfDue(System.currentTimeMillis());
        PurchaseProfile profile = this.resolveProfile(frameData, rawSlot, holder);

        InteractionLimiter.LimitResult limitResult = this.freeframe.getInteractionLimiter().checkAndMark(
            player.getUniqueId(),
            frameData.getId(),
            this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_COOLDOWN_PLAYERMILLIS),
            this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_COOLDOWN_FRAMEMILLIS)
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

        if (this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_LIMITS_ENABLED)) {
            PurchaseWindowLimiter.LimitState state = this.freeframe.getPurchaseWindowLimiter().checkAndConsume(
                player.getUniqueId(),
                profile.getAmount(),
                this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_LIMITS_MAXITEMSPERWINDOW),
                this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_LIMITS_WINDOWMILLIS)
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
                this.freeframe.getAlertService().alertPurchaseFailure(frameData.getId(), result.getStatus().name());
                this.freeframe.getBuyerReputationService().recordPurchaseFailure(player, frameData, result.getStatus().name().toLowerCase(Locale.ENGLISH));
                return;
            }

            if (result.getStatus() == PurchaseResult.Status.PREVIEW) {
                player.sendMessage(this.freeframe.getMessage(result.getMessagePath(), result.getFallbackMessage(), player));
                return;
            }

            int amount = Math.max(1, result.getPurchasedAmount());
            double grossPrice = Math.max(0.0D, result.getFinalPrice());
            String currency = this.resolveCurrency(frameData.getCurrency(), holder.getCurrency());
            BuyerRiskProfile risk = this.freeframe.getBuyerReputationService().evaluate(player, frameData, grossPrice);
            if (risk.isBlocked()) {
                this.freeframe.getMetricsTracker().incrementReputationBlocks();
                this.freeframe.getAlertService().alertPurchaseFailure(frameData.getId(), "risk-block");
                this.freeframe.getBuyerReputationService().recordPurchaseFailure(player, frameData, "risk-block");
                player.sendMessage(this.freeframe.getMessage(
                    "freeframe.reputation.blocked",
                    "%prefix% &cPurchase blocked by fraud protection.",
                    player
                ));
                return;
            }

            SignedPurchaseToken token = this.freeframe.getPurchaseSecurityService().createToken(player, frameData, profile, grossPrice);
            if (!this.freeframe.getPurchaseSecurityService().verify(token)) {
                this.freeframe.getAlertService().alertPurchaseFailure(frameData.getId(), "invalid-signature");
                this.freeframe.getBuyerReputationService().recordPurchaseFailure(player, frameData, "invalid-signature");
                player.sendMessage(this.freeframe.getMessage("freeframe.security.invalid", "%prefix% &cTransaction signature invalid.", player));
                return;
            }
            if (this.freeframe.getPurchaseSecurityService().isCommitted(token.getTxId())) {
                this.freeframe.getBuyerReputationService().recordPurchaseFailure(player, frameData, "duplicate");
                player.sendMessage(this.freeframe.getMessage("freeframe.security.duplicate", "%prefix% &eDuplicate purchase blocked.", player));
                return;
            }
            if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_GUI_DROPONFULLINVENTORY)
                && !this.canFit(player, templateItem, amount)) {
                player.sendMessage(this.freeframe.getMessage("freeframe.purchase.inventoryFull", "%prefix% &cYour inventory is full.", player));
                return;
            }

            long now = System.currentTimeMillis();
            CampaignEffect campaignEffect = this.freeframe.getCampaignRuntimeService().resolve(frameData, now);
            Double seasonalTaxOverride = this.freeframe.getSeasonalRulesService().resolveTaxOverridePercent(frameData, now);
            Double taxOverride = campaignEffect.getTaxOverridePercent() != null ? campaignEffect.getTaxOverridePercent() : seasonalTaxOverride;
            TaxBreakdown breakdown = this.freeframe.getTaxService().calculate(frameData, grossPrice, taxOverride);
            boolean consumed = this.freeframe.getShopNetworkService().consumeStock(frameData, amount);
            if (!consumed) {
                this.freeframe.getMetricsTracker().incrementStockOutHits();
                this.freeframe.getAlertService().alertPurchaseFailure(frameData.getId(), "network-stock-race");
                this.freeframe.getBuyerReputationService().recordPurchaseFailure(player, frameData, "network-stock-race");
                player.sendMessage(this.freeframe.getMessage(
                    "freeframe.purchase.stockOut",
                    "%prefix% &cThis frame is out of stock.",
                    player
                ));
                return;
            }

            boolean charged = this.chargePlayer(player, breakdown.getGross(), currency);
            if (breakdown.getGross() > 0.0D && !charged) {
                this.freeframe.getShopNetworkService().restoreStock(frameData, amount);
                this.freeframe.getTransactionJournalService().logPurchaseCommit(
                    token.getTxId(), player, frameData, amount, breakdown.getGross(), breakdown.getTax(), breakdown.getNet(), "charge_failed", token.getSignature()
                );
                this.freeframe.getBuyerReputationService().recordPurchaseFailure(player, frameData, "charge-failed");
                return;
            }

            this.giveItem(player, templateItem, amount);
            frameData.setCurrency(currency);

            if (breakdown.getGross() > 0.0D && charged) {
                frameData.addRevenue(breakdown.getNet());
                frameData.addCollectedTax(breakdown.getTax());
                this.payOwner(player, frameData, breakdown.getNet());
                this.freeframe.getTaxService().depositTax(breakdown.getTax());
            }

            this.freeframe.getMetricsTracker().incrementPurchases();
            this.freeframe.getStatisticsService().recordPurchase(player, frameData, amount, breakdown.getGross());
            this.freeframe.getDynamicPricingService().recordPurchase(frameData, System.currentTimeMillis());
            this.freeframe.getPurchaseSecurityService().markCommitted(token.getTxId());
            this.freeframe.getBuyerReputationService().recordPurchaseSuccess(player, frameData, breakdown.getGross());
            this.freeframe.getTransactionJournalService().logPurchaseCommit(
                token.getTxId(), player, frameData, amount, breakdown.getGross(), breakdown.getTax(), breakdown.getNet(), "success", token.getSignature()
            );
            this.freeframe.getWebhookExportService().sendPurchase(player, frameData, amount, breakdown.getGross(), breakdown.getGross() > 0.0D ? "charged" : "free");
            this.sendSuccessMessage(player, amount, breakdown.getGross(), breakdown.getTax(), currency);
            this.freeframe.getAuditLogger().logPurchase(player, frameData, amount, breakdown.getGross(), breakdown.getGross() > 0.0D ? "charged" : "free");
            this.freeframe.getDisplayService().refresh(frameData);
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getNetworkSyncService().publishFrameUpdate(frameData, "purchase");
            this.freeframe.getAlertService().alertLowStock(frameData);

            if (this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_GUI_CLOSEAFTERPURCHASE)) {
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

        boolean allowWithoutVault = this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_ECONOMY_ALLOWWITHOUTVAULT);
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

    private void payOwner(Player buyer, FreeFrameData frameData, double amount) {
        boolean payOwnerOnSelf = this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_ECONOMY_PAYOWNERONSELFPURCHASE);
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_ECONOMY_PAYOWNER)) {
            return;
        }

        if (!payOwnerOnSelf && frameData.isOwnedBy(buyer.getUniqueId().toString())) {
            return;
        }

        EconomyChargeResult payoutResult = this.freeframe.getEconomyService().depositToOwner(
            frameData.getOwnerUuid(),
            frameData.getOwnerName(),
            amount
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

        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_GUI_DROPONFULLINVENTORY)) {
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

    private void sendSuccessMessage(Player player, int amount, double gross, double tax, String currency) {
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("%amount%", String.valueOf(amount));
        tokens.put("%price%", this.formatPrice(gross));
        tokens.put("%currency%", currency);
        tokens.put("%tax%", this.formatPrice(tax));

        if (gross > 0.0D) {
            this.sendTemplatedMessage(
                player,
                "freeframe.purchase.success",
                "%prefix% &aPurchased &e%amount%x &afor &e%currency%%price%&a (&7tax %currency%%tax%&a).",
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

        String configured = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_DEFAULT_CURRENCY);
        return configured == null || configured.trim().isEmpty() ? "$" : configured.trim();
    }

    private boolean isFreeFrameInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof FreeFrameInventoryHolder;
    }

    private boolean isAir(Material material) {
        return material == null || "AIR".equals(material.name());
    }

    private boolean canFit(Player player, ItemStack template, int amount) {
        if (player == null || template == null || amount <= 0) {
            return false;
        }

        ItemStack probe = template.clone();
        int maxStackSize = Math.max(1, probe.getMaxStackSize());
        int remaining = amount;

        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack content : contents) {
            if (remaining <= 0) {
                return true;
            }
            if (content == null || content.getType() == null || "AIR".equals(content.getType().name())) {
                remaining -= Math.min(maxStackSize, remaining);
                continue;
            }
            if (content.isSimilar(probe) && content.getAmount() < maxStackSize) {
                int free = maxStackSize - content.getAmount();
                remaining -= Math.min(free, remaining);
            }
        }
        return remaining <= 0;
    }

    private String formatPrice(double price) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, price));
    }
}
