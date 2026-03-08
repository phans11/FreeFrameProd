package de.isolveproblems.freeframe.api;

public class PurchaseResult {
    public enum Status {
        SUCCESS,
        BLOCKED,
        PREVIEW,
        ERROR
    }

    private final Status status;
    private final String messagePath;
    private final String fallbackMessage;
    private final double finalPrice;
    private final int purchasedAmount;

    public PurchaseResult(Status status, String messagePath, String fallbackMessage, double finalPrice, int purchasedAmount) {
        this.status = status;
        this.messagePath = messagePath;
        this.fallbackMessage = fallbackMessage;
        this.finalPrice = Math.max(0.0D, finalPrice);
        this.purchasedAmount = Math.max(0, purchasedAmount);
    }

    public static PurchaseResult success(String path, String fallback, double finalPrice, int amount) {
        return new PurchaseResult(Status.SUCCESS, path, fallback, finalPrice, amount);
    }

    public static PurchaseResult blocked(String path, String fallback) {
        return new PurchaseResult(Status.BLOCKED, path, fallback, 0.0D, 0);
    }

    public static PurchaseResult preview(String path, String fallback) {
        return new PurchaseResult(Status.PREVIEW, path, fallback, 0.0D, 0);
    }

    public static PurchaseResult error(String path, String fallback) {
        return new PurchaseResult(Status.ERROR, path, fallback, 0.0D, 0);
    }

    public Status getStatus() {
        return this.status;
    }

    public String getMessagePath() {
        return this.messagePath;
    }

    public String getFallbackMessage() {
        return this.fallbackMessage;
    }

    public double getFinalPrice() {
        return this.finalPrice;
    }

    public int getPurchasedAmount() {
        return this.purchasedAmount;
    }
}
