
package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CreateFrameListener
implements Listener {
    private final FreeFrame freeframe = (FreeFrame)FreeFrame.getPlugin(FreeFrame.class);

    @EventHandler
    public void onFrameCreation(PlayerInteractEntityEvent event) {
        if (this.isOffHandInteraction(event)) {
            return;
        }
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ItemFrame)) {
            return;
        }

        ItemFrame itemFrame = (ItemFrame)entity;
        ItemStack itemStack = itemFrame.getItem();
        if (itemStack == null || this.isAir(itemStack.getType())) {
            return;
        }

        int amount = this.freeframe.getConfigHandler().config.getConfig().getInt("freeframe.item.amount");
        if (itemStack.getType() == Material.ARMOR_STAND) {
            itemStack.setAmount(amount);
            itemFrame.setRotation(Rotation.NONE);
            event.setCancelled(true);
            return;
        }

        this.openItemFrame(player, itemStack, amount);
        itemFrame.setRotation(Rotation.NONE);
        event.setCancelled(true);
    }

    public void openItemFrame(Player player, ItemStack itemstack, int amount) {
        ItemStack displayItem = itemstack.clone();
        if (displayItem.getMaxStackSize() > 1) {
            displayItem.setAmount(Math.max(1, Math.min(amount, displayItem.getMaxStackSize())));
        }
        Inventory itemframe = Bukkit.createInventory(null, (int)9, (String)this.freeframe.getPrefix());
        itemframe.setItem(2, displayItem);
        itemframe.setItem(4, displayItem);
        itemframe.setItem(6, displayItem);
        player.openInventory(itemframe);
    }

    private boolean isAir(Material material) {
        return material == null || material.name().equals("AIR");
    }

    private boolean isOffHandInteraction(PlayerInteractEntityEvent event) {
        try {
            Object hand = event.getClass().getMethod("getHand").invoke(event);
            return hand != null && "OFF_HAND".equals(String.valueOf(hand));
        }
        catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
