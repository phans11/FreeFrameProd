package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.configuration.ConfigurationSection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SeasonalRulesService {
    public static class SeasonalRule {
        private final String id;
        private final long startAt;
        private final long endAt;
        private final double priceMultiplier;
        private final Double taxOverridePercent;

        public SeasonalRule(String id, long startAt, long endAt, double priceMultiplier, Double taxOverridePercent) {
            this.id = id;
            this.startAt = startAt;
            this.endAt = endAt;
            this.priceMultiplier = Math.max(0.0D, priceMultiplier);
            this.taxOverridePercent = taxOverridePercent;
        }

        public boolean isActive(long now) {
            return now >= this.startAt && now <= this.endAt;
        }

        public String getId() {
            return this.id;
        }

        public double getPriceMultiplier() {
            return this.priceMultiplier;
        }

        public Double getTaxOverridePercent() {
            return this.taxOverridePercent;
        }
    }

    private final FreeFrame freeframe;

    public SeasonalRulesService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public SeasonalRule resolveActiveRule(FreeFrameData frameData, long now) {
        List<SeasonalRule> rules = this.readRules();
        if (rules.isEmpty()) {
            return null;
        }

        String forcedId = frameData == null ? "" : frameData.getSeasonRuleId();
        if (forcedId != null && !forcedId.trim().isEmpty()) {
            String normalized = forcedId.trim().toLowerCase(Locale.ENGLISH);
            for (SeasonalRule rule : rules) {
                if (normalized.equals(rule.getId()) && rule.isActive(now)) {
                    return rule;
                }
            }
            return null;
        }

        for (SeasonalRule rule : rules) {
            if (rule.isActive(now)) {
                return rule;
            }
        }
        return null;
    }

    public double applyPriceMultiplier(FreeFrameData frameData, double basePrice, long now) {
        SeasonalRule rule = this.resolveActiveRule(frameData, now);
        if (rule == null) {
            return Math.max(0.0D, basePrice);
        }
        return Math.max(0.0D, basePrice * rule.getPriceMultiplier());
    }

    public Double resolveTaxOverridePercent(FreeFrameData frameData, long now) {
        SeasonalRule rule = this.resolveActiveRule(frameData, now);
        return rule == null ? null : rule.getTaxOverridePercent();
    }

    public String resolveRuleId(FreeFrameData frameData, long now) {
        SeasonalRule rule = this.resolveActiveRule(frameData, now);
        return rule == null ? "" : rule.getId();
    }

    private List<SeasonalRule> readRules() {
        List<SeasonalRule> rules = new ArrayList<SeasonalRule>();
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_SEASONS_ENABLED)) {
            return rules;
        }

        ConfigurationSection section = this.freeframe.cfgSection(FreeFrameConfigKey.FREEFRAME_SEASONS_RULES);
        if (section == null) {
            return rules;
        }

        for (String rawId : section.getKeys(false)) {
            ConfigurationSection ruleSection = section.getConfigurationSection(rawId);
            if (ruleSection == null) {
                continue;
            }
            if (!ruleSection.getBoolean("enabled", true)) {
                continue;
            }

            long startAt = parseDate(ruleSection.getString("start", ""));
            long endAt = parseDate(ruleSection.getString("end", ""));
            if (startAt <= 0L || endAt <= 0L || endAt < startAt) {
                continue;
            }

            double priceMultiplier = Math.max(0.0D, ruleSection.getDouble("priceMultiplier", 1.0D));
            Double taxOverride = null;
            if (ruleSection.contains("taxOverridePercent")) {
                taxOverride = Double.valueOf(Math.max(0.0D, Math.min(100.0D, ruleSection.getDouble("taxOverridePercent", 0.0D))));
            }

            rules.add(new SeasonalRule(rawId.toLowerCase(Locale.ENGLISH), startAt, endAt, priceMultiplier, taxOverride));
        }
        return rules;
    }

    private long parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0L;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
        String timezone = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_SEASONS_TIMEZONE);
        try {
            format.setTimeZone(TimeZone.getTimeZone(timezone));
            Date date = format.parse(raw.trim());
            return date == null ? 0L : date.getTime();
        } catch (ParseException exception) {
            return 0L;
        }
    }
}
