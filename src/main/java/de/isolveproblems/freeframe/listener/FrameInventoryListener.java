package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.economy.EconomyChargeResult;
import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.InteractionLimiter;
import org.bukkit.Location;
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

        if (!this.isSaleSlot(event.getRawSlot())) {
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
        if (holder == null || holder.getSaleItem() == null || this.isAir(holder.getSaleItem().getType())) {
            return;
        }

        FreeFrameData frameData = this.freeframe.getFrameRegistry().findById(holder.getFrameId());
        if (frameData == null || !frameData.isActive()) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.frame.inactive",
                "%prefix% &cThis FreeFrame is currently inactive."
            ));
            return;
        }

        if (!this.freeframe.canPlayerUseFrame(player, frameData)) {
            this.freeframe.getMetricsTracker().incrementDeniedAccess();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.access.denied",
                "%prefix% &cYou are not allowed to use this FreeFrame."
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
                "%prefix% &cPlease wait before using another FreeFrame."
            ));
            return;
        }

        if (limitResult == InteractionLimiter.LimitResult.FRAME_RATE_LIMIT) {
            this.freeframe.getMetricsTracker().incrementFrameRateLimitHits();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.rateLimit.message",
                "%prefix% &cThis FreeFrame is currently rate-limited."
            ));
            return;
        }

        double price = Math.max(0.0D, frameData.getPrice());
        String currency = this.resolveCurrency(frameData.getCurrency(), holder.getCurrency());

        boolean charged = false;
        if (price > 0.0D) {
            boolean allowWithoutVault = this.freeframe.getPluginConfig().getBoolean("freeframe.economy.allowWithoutVault", false);
            if (!this.freeframe.getEconomyService().isAvailable() && !allowWithoutVault) {
                player.sendMessage(this.freeframe.getMessage(
                    "freeframe.purchase.economyUnavailable",
                    "%prefix% &cEconomy is not available right now."
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
                            "%prefix% &cEconomy is not available right now."
                        ));
                        return;
                    }
                } else {
                    charged = true;
                }
            }
        }

        this.giveItem(player, holder.getSaleItem());
        this.freeframe.getMetricsTracker().incrementPurchases();

        if (price > 0.0D && charged) {
            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("%price%", this.formatPrice(price));
            tokens.put("%currency%", currency);
            this.sendTemplatedMessage(
                player,
                "freeframe.purchase.success",
                "%prefix% &aPurchased item for &e%currency%%price%&a.",
                tokens
            );
        } else {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.free",
                "%prefix% &aYou received this item for free."
            ));
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

        Location dropLocation = player.getLocation();
        for (ItemStack item : remaining.values()) {
            if (item != null && !this.isAir(item.getType())) {
                player.getWorld().dropItemNaturally(dropLocation, item);
            }
        }

        player.sendMessage(this.freeframe.getMessage(
            "freeframe.purchase.inventoryDrop",
            "%prefix% &eYour inventory was full. Remaining items were dropped."
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

        player.sendMessage(this.freeframe.formatMessage(template));
    }

    private String resolveCurrency(String preferred, String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) {
            return preferred.trim();
        }

        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback.trim();
        }

        String configured = this.freeframe.getPluginConfig().getString("freeframe.default.currency", "$" );
        return configured == null || configured.trim().isEmpty() ? "$" : configured.trim();
    }

    private boolean isSaleSlot(int rawSlot) {
        return rawSlot == 2 || rawSlot == 4 || rawSlot == 6;
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
