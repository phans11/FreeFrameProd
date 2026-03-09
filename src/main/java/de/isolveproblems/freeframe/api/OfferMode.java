package de.isolveproblems.freeframe.api;

public enum OfferMode {
    BOTH,
    ADMIN,
    USER;

    public static OfferMode fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return BOTH;
        }

        for (OfferMode value : values()) {
            if (value.name().equalsIgnoreCase(raw.trim())) {
                return value;
            }
        }
        return BOTH;
    }
}
