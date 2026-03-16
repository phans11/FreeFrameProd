package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.CampaignEffect;
import de.isolveproblems.freeframe.api.CampaignRuntimeService;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class CampaignEngineService implements CampaignRuntimeService {
    private final FreeFrame freeframe;

    public CampaignEngineService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public CampaignEffect resolve(FreeFrameData frameData, long nowEpochMillis) {
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_CAMPAIGNS_ENABLED)) {
            return CampaignEffect.inactive();
        }

        String configuredCampaign = frameData == null ? "" : frameData.getCampaignId();
        if (configuredCampaign == null || configuredCampaign.trim().isEmpty()) {
            configuredCampaign = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_CAMPAIGNS_DEFAULTRULE);
        }
        String campaignId = configuredCampaign == null ? "" : configuredCampaign.trim().toLowerCase(Locale.ENGLISH);
        if (campaignId.isEmpty()) {
            return CampaignEffect.inactive();
        }

        ConfigurationSection section = this.freeframe.cfgSection(FreeFrameConfigKey.FREEFRAME_CAMPAIGNS_RULES, campaignId);
        if (section == null || !section.getBoolean("enabled", false)) {
            return CampaignEffect.inactive();
        }

        if (!this.isWithinWindow(section, nowEpochMillis)) {
            return CampaignEffect.inactive();
        }

        double multiplier = Math.max(0.0D, section.getDouble("priceMultiplier", 1.0D));
        Double taxOverride = null;
        if (section.contains("taxOverridePercent")) {
            taxOverride = Double.valueOf(Math.max(0.0D, section.getDouble("taxOverridePercent", 0.0D)));
        }
        String branding = section.getString("brandingOverride", "");
        return new CampaignEffect(campaignId, true, multiplier, taxOverride, branding);
    }

    @Override
    public double applyPrice(FreeFrameData frameData, double basePrice, long nowEpochMillis) {
        CampaignEffect effect = this.resolve(frameData, nowEpochMillis);
        if (!effect.isActive()) {
            return Math.max(0.0D, basePrice);
        }
        return Math.max(0.0D, basePrice) * effect.getPriceMultiplier();
    }

    private boolean isWithinWindow(ConfigurationSection section, long nowEpochMillis) {
        String timezone = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_CAMPAIGNS_TIMEZONE);
        ZoneId zoneId = parseZone(timezone);
        long start = parseEpoch(section.getString("start", ""), zoneId);
        long end = parseEpoch(section.getString("end", ""), zoneId);

        if (start > 0L && nowEpochMillis < start) {
            return false;
        }
        if (end > 0L && nowEpochMillis > end) {
            return false;
        }
        return true;
    }

    private static ZoneId parseZone(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(value.trim());
        } catch (Exception ignored) {
            return ZoneId.of("UTC");
        }
    }

    private static long parseEpoch(String raw, ZoneId zoneId) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0L;
        }
        try {
            return LocalDateTime.parse(raw.trim()).atZone(zoneId).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            return 0L;
        }
    }
}
