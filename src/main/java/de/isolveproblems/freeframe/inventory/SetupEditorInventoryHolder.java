package de.isolveproblems.freeframe.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SetupEditorInventoryHolder implements InventoryHolder {
    private final String frameId;

    public SetupEditorInventoryHolder(String frameId) {
        this.frameId = frameId;
    }

    public String getFrameId() {
        return this.frameId;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
