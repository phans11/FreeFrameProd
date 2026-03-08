package de.isolveproblems.freeframe.utils;

public final class AmountValidator {
    private static final int MIN_AMOUNT = 1;
    private static final int MAX_AMOUNT = 64;

    private AmountValidator() {
    }

    public static int sanitize(int configuredAmount) {
        return Math.max(MIN_AMOUNT, Math.min(MAX_AMOUNT, configuredAmount));
    }
}
