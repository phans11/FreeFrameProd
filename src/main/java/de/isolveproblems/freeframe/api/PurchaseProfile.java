package de.isolveproblems.freeframe.api;

public class PurchaseProfile {
    private final int slot;
    private final int amount;
    private final double price;
    private final String displayName;

    public PurchaseProfile(int slot, int amount, double price, String displayName) {
        this.slot = slot;
        this.amount = Math.max(1, amount);
        this.price = Math.max(0.0D, price);
        this.displayName = displayName == null ? "" : displayName;
    }

    public int getSlot() {
        return this.slot;
    }

    public int getAmount() {
        return this.amount;
    }

    public double getPrice() {
        return this.price;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
