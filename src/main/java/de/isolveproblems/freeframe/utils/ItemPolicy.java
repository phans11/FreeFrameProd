package de.isolveproblems.freeframe.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ItemPolicy {

    public static class Decision {
        private final boolean allowed;
        private final String reason;

        public Decision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public boolean isAllowed() {
            return this.allowed;
        }

        public String getReason() {
            return this.reason;
        }
    }

    public Decision check(FileConfiguration config, Material material) {
        if (material == null) {
            return new Decision(false, "Item is null");
        }

        String mode = config.getString("freeframe.items.mode", "off");
        if (mode == null || "off".equalsIgnoreCase(mode.trim())) {
            return new Decision(true, "mode=off");
        }

        String materialName = material.name().toUpperCase(Locale.ENGLISH);
        if ("blacklist".equalsIgnoreCase(mode.trim())) {
            Set<String> blocked = toNormalizedSet(config.getStringList("freeframe.items.blacklist"));
            return new Decision(!blocked.contains(materialName), "blacklist");
        }

        if ("whitelist".equalsIgnoreCase(mode.trim())) {
            Set<String> allowed = toNormalizedSet(config.getStringList("freeframe.items.whitelist"));
            return new Decision(allowed.contains(materialName), "whitelist");
        }

        return new Decision(true, "unknown-mode");
    }

    private Set<String> toNormalizedSet(List<String> values) {
        Set<String> normalized = new HashSet<String>();
        if (values == null) {
            return normalized;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                normalized.add(value.trim().toUpperCase(Locale.ENGLISH));
            }
        }

        return normalized;
    }
}
