package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import de.isolveproblems.freeframe.utils.FreeFrameData;
import de.isolveproblems.freeframe.utils.ItemPolicy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CreateFrameListener implements Listener {
    private final FreeFrame freeframe;

    public CreateFrameListener(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFrameCreation(PlayerInteractEntityEvent event) {
        if (this.isOffHandInteraction(event)) {
            return;
        }

        Entity clickedEntity = event.getRightClicked();
        if (!(clickedEntity instanceof ItemFrame)) {
            return;
        }

        ItemFrame itemFrame = (ItemFrame) clickedEntity;
        ItemStack itemStack = itemFrame.getItem();
        if (itemStack == null || this.isAir(itemStack.getType())) {
            return;
        }

        Location frameLocation = itemFrame.getLocation();
        if (!this.freeframe.isLocationAllowed(frameLocation)) {
            event.getPlayer().sendMessage(this.freeframe.getMessage(
                "freeframe.restrictions.denied",
                "%prefix% &cFreeFrame is disabled in this world/region.",
                event.getPlayer()
            ));
            event.setCancelled(true);
            return;
        }

        ItemPolicy.Decision decision = this.freeframe.getItemPolicy().check(this.freeframe.getPluginConfig(), itemStack.getType());
        if (!decision.isAllowed()) {
            event.getPlayer().sendMessage(this.freeframe.getMessage(
                "freeframe.items.blockedMessage",
                "%prefix% &cThis item type is blocked by item policy.",
                event.getPlayer()
            ));
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        FreeFrameData frameData = this.freeframe.getFrameRegistry().getOrCreate(itemFrame, player, itemStack);
        if (frameData == null) {
            return;
        }

        if (!frameData.isActive()) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.frame.inactive",
                "%prefix% &cThis FreeFrame is currently inactive.",
                player
            ));
            event.setCancelled(true);
            return;
        }

        if (!this.freeframe.canPlayerUseFrame(player, frameData)) {
            this.freeframe.getMetricsTracker().incrementDeniedAccess();
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.access.denied",
                "%prefix% &cYou are not allowed to use this FreeFrame.",
                player
            ));
            event.setCancelled(true);
            return;
        }

        if (frameData.applyAutoRefillIfDue(System.currentTimeMillis())) {
            this.freeframe.getFrameRegistry().saveToConfig();
            this.freeframe.getDisplayService().refresh(frameData);
        }

        int availableAmount = Math.max(0, frameData.getStock());
        if (availableAmount <= 0) {
            player.sendMessage(this.freeframe.getMessage(
                "freeframe.purchase.stockOut",
                "%prefix% &cThis frame is out of stock.",
                player
            ));
            event.setCancelled(true);
            return;
        }

        PurchaseProfile previewProfile = frameData.getPurchaseProfiles().isEmpty()
            ? null
            : frameData.getPurchaseProfiles().get(0);
        int displayAmount = previewProfile == null
            ? Math.max(1, Math.min(this.freeframe.getConfiguredItemAmount(), availableAmount))
            : Math.max(1, Math.min(previewProfile.getAmount(), availableAmount));
        if (this.freeframe.getPluginConfig().getBoolean("freeframe.compat.armorStandAmountFix", true)
            && itemStack.getType() == Material.ARMOR_STAND) {
            this.updateFrameItemAmount(itemFrame, itemStack, displayAmount);
            if (this.freeframe.getPluginConfig().getBoolean("freeframe.compat.cancelRotation", true)) {
                itemFrame.setRotation(Rotation.NONE);
            }
            event.setCancelled(true);
            return;
        }

        this.openItemFrame(player, frameData, itemStack, displayAmount);
        if (this.freeframe.getPluginConfig().getBoolean("freeframe.compat.cancelRotation", true)) {
            itemFrame.setRotation(Rotation.NONE);
        }
        event.setCancelled(true);
    }

    private void updateFrameItemAmount(ItemFrame itemFrame, ItemStack sourceItem, int amount) {
        ItemStack updatedItem = sourceItem.clone();
        int stackSize = Math.max(1, updatedItem.getMaxStackSize());
        updatedItem.setAmount(Math.max(1, Math.min(amount, stackSize)));
        itemFrame.setItem(updatedItem);
    }

    private void openItemFrame(Player player, FreeFrameData frameData, ItemStack sourceItem, int amount) {
        ItemStack displayItem = sourceItem.clone();
        if (displayItem.getMaxStackSize() > 1) {
            displayItem.setAmount(Math.max(1, Math.min(amount, displayItem.getMaxStackSize())));
        }

        String currency = frameData.getCurrency() == null ? "$" : frameData.getCurrency();
        Inventory inventory = Bukkit.createInventory(
            new FreeFrameInventoryHolder(frameData.getId(), displayItem, frameData.getPrice(), currency),
            this.freeframe.getGuiInventorySize(),
            this.freeframe.getGuiTitle(player)
        );

        for (PurchaseProfile profile : frameData.getPurchaseProfiles()) {
            if (profile.getSlot() < 0 || profile.getSlot() >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(profile.getSlot(), this.createProfileItem(displayItem, profile, currency));
        }

        for (Integer slot : this.freeframe.getSaleSlots()) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, displayItem.clone());
            }
        }
        player.openInventory(inventory);
    }

    private ItemStack createProfileItem(ItemStack template, PurchaseProfile profile, String currency) {
        ItemStack item = template.clone();
        if (item.getMaxStackSize() > 1) {
            item.setAmount(Math.max(1, Math.min(profile.getAmount(), item.getMaxStackSize())));
        }

        if (profile.getDisplayName() == null || profile.getDisplayName().trim().isEmpty()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        String name = profile.getDisplayName()
            .replace("%amount%", String.valueOf(profile.getAmount()))
            .replace("%price%", String.format(java.util.Locale.ENGLISH, "%.2f", profile.getPrice()))
            .replace("%currency%", currency == null ? "$" : currency);
        meta.setDisplayName(this.freeframe.colorize(name));
        item.setItemMeta(meta);
        return item;
    }

    private boolean isAir(Material material) {
        return material == null || "AIR".equals(material.name());
    }

    private boolean isOffHandInteraction(PlayerInteractEntityEvent event) {
        try {
            Object hand = event.getClass().getMethod("getHand").invoke(event);
            return hand != null && "OFF_HAND".equals(String.valueOf(hand));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
