package de.isolveproblems.freeframe.economy;

public class EconomyChargeResult {
    public enum Status {
        SUCCESS,
        NOT_ENOUGH_MONEY,
        ECONOMY_UNAVAILABLE,
        ERROR
    }

    private final Status status;
    private final String errorMessage;

    public EconomyChargeResult(Status status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static EconomyChargeResult success() {
        return new EconomyChargeResult(Status.SUCCESS, null);
    }

    public static EconomyChargeResult notEnoughMoney(String message) {
        return new EconomyChargeResult(Status.NOT_ENOUGH_MONEY, message);
    }

    public static EconomyChargeResult unavailable() {
        return new EconomyChargeResult(Status.ECONOMY_UNAVAILABLE, null);
    }

    public static EconomyChargeResult error(String message) {
        return new EconomyChargeResult(Status.ERROR, message);
    }

    public Status getStatus() {
        return this.status;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
