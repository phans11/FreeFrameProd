package de.isolveproblems.freeframe.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CampaignEffectTest {
    @Test
    void inactiveFactoryShouldUseSafeDefaults() {
        CampaignEffect effect = CampaignEffect.inactive();
        assertFalse(effect.isActive());
        assertEquals("", effect.getId());
        assertEquals(1.0D, effect.getPriceMultiplier());
        assertEquals(null, effect.getTaxOverridePercent());
        assertEquals("", effect.getBrandingOverrideId());
    }

    @Test
    void constructorShouldClampNegativeMultiplier() {
        CampaignEffect effect = new CampaignEffect("sale", true, -2.0D, Double.valueOf(4.5D), "theme");
        assertTrue(effect.isActive());
        assertEquals(0.0D, effect.getPriceMultiplier());
    }
}
