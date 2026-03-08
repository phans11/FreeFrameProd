package de.isolveproblems.freeframe.api;

public enum FrameType {
    FREE,
    SHOP,
    LIMITED,
    ADMIN_ONLY,
    PREVIEW_ONLY;

    public static FrameType fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return SHOP;
        }

        String normalized = raw.trim().replace('-', '_').replace(' ', '_');
        for (FrameType value : values()) {
            if (value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return SHOP;
    }
}
