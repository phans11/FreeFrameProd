package de.isolveproblems.freeframe.api;

public enum ShopOwnerType {
    ADMIN,
    USER;

    public static ShopOwnerType fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return USER;
        }

        for (ShopOwnerType value : values()) {
            if (value.name().equalsIgnoreCase(raw.trim())) {
                return value;
            }
        }
        return USER;
    }
}
