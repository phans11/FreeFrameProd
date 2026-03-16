package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.DiscountService;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class DefaultDiscountService implements DiscountService {
    private final FreeFrame freeframe;

    public DefaultDiscountService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public double applyDiscount(Player player, FreeFrameData frameData, double basePrice) {
        if (player == null || basePrice <= 0.0D) {
            return Math.max(0.0D, basePrice);
        }

        ConfigurationSection section = this.freeframe.cfgSection(FreeFrameConfigKey.FREEFRAME_DISCOUNTS_PERMISSIONS);
        double bestPercent = 0.0D;
        if (section != null) {
            for (String permission : section.getKeys(false)) {
                if (player.hasPermission(permission)) {
                    bestPercent = Math.max(bestPercent, Math.max(0.0D, section.getDouble(permission, 0.0D)));
                }
            }
        }

        double modifier = Math.max(0.0D, 1.0D - Math.min(100.0D, bestPercent) / 100.0D);
        return Math.max(0.0D, basePrice * modifier);
    }
}
