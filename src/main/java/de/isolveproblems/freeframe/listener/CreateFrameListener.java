package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import org.bukkit.Bukkit;
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

        this.freeframe.getFrameRegistry().track(itemFrame);
        int configuredAmount = this.freeframe.getConfiguredItemAmount();
        if (itemStack.getType() == Material.ARMOR_STAND) {
            this.updateFrameItemAmount(itemFrame, itemStack, configuredAmount);
            itemFrame.setRotation(Rotation.NONE);
            event.setCancelled(true);
            return;
        }

        this.openItemFrame(event.getPlayer(), itemStack, configuredAmount);
        itemFrame.setRotation(Rotation.NONE);
        event.setCancelled(true);
    }

    private void updateFrameItemAmount(ItemFrame itemFrame, ItemStack sourceItem, int amount) {
        ItemStack updatedItem = sourceItem.clone();
        int stackSize = Math.max(1, updatedItem.getMaxStackSize());
        updatedItem.setAmount(Math.max(1, Math.min(amount, stackSize)));
        itemFrame.setItem(updatedItem);
    }

    private void openItemFrame(Player player, ItemStack sourceItem, int amount) {
        ItemStack displayItem = sourceItem.clone();
        if (displayItem.getMaxStackSize() > 1) {
            displayItem.setAmount(Math.max(1, Math.min(amount, displayItem.getMaxStackSize())));
        }

        Inventory inventory = Bukkit.createInventory(new FreeFrameInventoryHolder(), 9, this.freeframe.getPrefix());
        inventory.setItem(2, displayItem);
        inventory.setItem(4, displayItem);
        inventory.setItem(6, displayItem);
        player.openInventory(inventory);
    }

    private boolean isAir(Material material) {
        return material == null || material.name().equals("AIR");
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
