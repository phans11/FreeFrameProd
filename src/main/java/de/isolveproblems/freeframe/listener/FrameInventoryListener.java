package de.isolveproblems.freeframe.listener;

import de.isolveproblems.freeframe.inventory.FreeFrameInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class FrameInventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (this.isFreeFrameInventory(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (this.isFreeFrameInventory(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    private boolean isFreeFrameInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof FreeFrameInventoryHolder;
    }
}
