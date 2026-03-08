package de.isolveproblems.freeframe.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class FreeFrameInventoryHolder implements InventoryHolder {
    private final String frameId;
    private final ItemStack saleItem;
    private final double price;
    private final String currency;

    public FreeFrameInventoryHolder(String frameId, ItemStack saleItem, double price, String currency) {
        this.frameId = frameId;
        this.saleItem = saleItem == null ? null : saleItem.clone();
        this.price = Math.max(0.0D, price);
        this.currency = currency == null ? "$" : currency;
    }

    public String getFrameId() {
        return this.frameId;
    }

    public ItemStack getSaleItem() {
        return this.saleItem == null ? null : this.saleItem.clone();
    }

    public double getPrice() {
        return this.price;
    }

    public String getCurrency() {
        return this.currency;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
