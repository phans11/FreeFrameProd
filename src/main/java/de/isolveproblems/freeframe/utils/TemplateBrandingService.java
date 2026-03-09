package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.BrandingService;
import de.isolveproblems.freeframe.api.ShopOwnerType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public class TemplateBrandingService implements BrandingService {
    private final FreeFrame freeframe;

    public TemplateBrandingService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public String resolveDisplayTemplate(FreeFrameData frameData) {
        return this.resolveDisplayTemplate(frameData, "");
    }

    @Override
    public String resolveDisplayTemplate(FreeFrameData frameData, String forcedThemeId) {
        String themeId = forcedThemeId == null || forcedThemeId.trim().isEmpty()
            ? this.resolveThemeId(frameData)
            : forcedThemeId.trim().toLowerCase(Locale.ENGLISH);
        String fallback = this.freeframe.getPluginConfig().getString(
            "freeframe.display.template",
            "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%"
        );
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.branding.enabled", false)) {
            return fallback;
        }

        ConfigurationSection section = this.freeframe.getPluginConfig().getConfigurationSection("freeframe.branding.themes." + themeId);
        if (section == null) {
            return fallback;
        }
        String template = section.getString("displayTemplate", fallback);
        return template == null || template.trim().isEmpty() ? fallback : template;
    }

    @Override
    public String resolveThemeId(FreeFrameData frameData) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.branding.enabled", false)) {
            return "";
        }

        if (frameData != null && frameData.getBrandingId() != null && !frameData.getBrandingId().trim().isEmpty()) {
            return frameData.getBrandingId().trim().toLowerCase(Locale.ENGLISH);
        }

        if (frameData != null && frameData.getShopOwnerType() == ShopOwnerType.ADMIN) {
            String adminDefault = this.freeframe.getPluginConfig().getString("freeframe.branding.defaultAdminTheme", "");
            if (adminDefault != null && !adminDefault.trim().isEmpty()) {
                return adminDefault.trim().toLowerCase(Locale.ENGLISH);
            }
        }

        String userDefault = this.freeframe.getPluginConfig().getString("freeframe.branding.defaultUserTheme", "");
        if (userDefault != null && !userDefault.trim().isEmpty()) {
            return userDefault.trim().toLowerCase(Locale.ENGLISH);
        }
        return this.freeframe.getPluginConfig().getString("freeframe.branding.defaultTheme", "classic")
            .trim()
            .toLowerCase(Locale.ENGLISH);
    }
}
