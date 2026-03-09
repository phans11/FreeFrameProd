package de.isolveproblems.freeframe.api;

public enum SaleMode {
    INSTANT,
    AUCTION;

    public static SaleMode fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return INSTANT;
        }

        for (SaleMode value : values()) {
            if (value.name().equalsIgnoreCase(raw.trim())) {
                return value;
            }
        }
        return INSTANT;
    }
}
