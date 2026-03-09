package de.isolveproblems.freeframe.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AnalyticsInventoryHolder implements InventoryHolder {
    private final String mode;
    private final String target;

    public AnalyticsInventoryHolder(String mode, String target) {
        this.mode = mode == null ? "global" : mode;
        this.target = target == null ? "" : target;
    }

    public String getMode() {
        return this.mode;
    }

    public String getTarget() {
        return this.target;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
