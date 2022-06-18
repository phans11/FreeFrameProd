
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
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame)event.getRightClicked();
            if (event.getRightClicked() instanceof ItemFrame && !((ItemFrame)event.getRightClicked()).getItem().getType().equals((Object)Material.AIR)) {
                ItemStack itemstack = itemFrame.getItem();
                if (itemFrame.getItem().equals((Object)Material.ARMOR_STAND)) {
                    itemstack.setAmount(this.freeframe.getConfigHandler().config.getConfig().getInt("freeframe.item.amount"));
                    itemFrame.setRotation(itemFrame.getRotation());
                    itemFrame.setRotation(Rotation.NONE);
                    event.setCancelled(true);
                } else {
                    this.openItemFrame(player, itemstack, this.freeframe.getConfigHandler().config.getConfig().getInt("freeframe.item.amount"));
                    itemFrame.setRotation(itemFrame.getRotation());
                    itemFrame.setRotation(Rotation.NONE);
                    event.setCancelled(true);
                }
            }
        }
    }

    public void openItemFrame(Player player, ItemStack itemstack, int amount) {
        if (itemstack.getMaxStackSize() > 1) {
            itemstack.setAmount(amount);
        }
        Inventory itemframe = Bukkit.createInventory(null, (int)9, (String)this.freeframe.getPrefix());
        itemframe.setItem(1, itemstack);
        itemframe.setItem(2, itemstack);
        itemframe.setItem(3, itemstack);
        itemframe.setItem(4, itemstack);
        itemframe.setItem(5, itemstack);
        itemframe.setItem(6, itemstack);
        itemframe.setItem(7, itemstack);
        player.openInventory(itemframe);
    }
}

