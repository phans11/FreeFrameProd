package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import de.isolveproblems.freeframe.api.LocalizationService;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class YamlLocalizationService implements LocalizationService {
    private final FreeFrame freeframe;
    private final Map<String, ConfigAPI> localeConfigs = new HashMap<String, ConfigAPI>();

    public YamlLocalizationService(FreeFrame freeframe) {
        this.freeframe = freeframe;
        this.localeConfigs.put("en", new ConfigAPI(freeframe.getDataFolder(), "messages_en.yml"));
        this.localeConfigs.put("de", new ConfigAPI(freeframe.getDataFolder(), "messages_de.yml"));
        this.seedDefaults();
    }

    @Override
    public String resolveMessage(Player player, String path, String fallback) {
        String locale = this.resolveLocale(player);
        ConfigAPI configApi = this.localeConfigs.get(locale);
        if (configApi == null) {
            configApi = this.localeConfigs.get("en");
        }

        if (configApi != null) {
            String localized = configApi.getConfig().getString(path);
            if (localized != null && !localized.trim().isEmpty()) {
                return localized;
            }
        }
        return fallback;
    }

    private String resolveLocale(Player player) {
        String configured = this.freeframe.getPluginConfig().getString("freeframe.localization.defaultLocale", "en");
        String locale = configured == null ? "en" : configured.toLowerCase(Locale.ENGLISH);
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.localization.usePlayerLocale", true) || player == null) {
            return locale;
        }

        try {
            Method getLocale = player.getClass().getMethod("getLocale");
            Object result = getLocale.invoke(player);
            if (result != null) {
                String raw = String.valueOf(result).toLowerCase(Locale.ENGLISH);
                if (raw.startsWith("de")) {
                    return "de";
                }
                if (raw.startsWith("en")) {
                    return "en";
                }
            }
        } catch (Throwable ignored) {
            return locale;
        }
        return locale;
    }

    private void seedDefaults() {
        ConfigAPI english = this.localeConfigs.get("en");
        ConfigAPI german = this.localeConfigs.get("de");
        if (english != null) {
            english.getConfig().addDefault("freeframe.purchase.success", "%prefix% &aPurchased &e%amount%x &afor &e%currency%%price%&a.");
            english.getConfig().addDefault("freeframe.types.previewOnly", "%prefix% &7This frame is preview-only.");
            english.getConfig().options().copyDefaults(true);
            english.saveConfig();
        }
        if (german != null) {
            german.getConfig().addDefault("freeframe.purchase.success", "%prefix% &aDu hast &e%amount%x &afuer &e%currency%%price% &agekauft.");
            german.getConfig().addDefault("freeframe.types.previewOnly", "%prefix% &7Dieser Frame ist nur zur Vorschau.");
            german.getConfig().options().copyDefaults(true);
            german.saveConfig();
        }
    }
}
