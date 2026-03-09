package de.isolveproblems.freeframe.api;

public class CampaignEffect {
    private final String id;
    private final boolean active;
    private final double priceMultiplier;
    private final Double taxOverridePercent;
    private final String brandingOverrideId;

    public CampaignEffect(String id, boolean active, double priceMultiplier, Double taxOverridePercent, String brandingOverrideId) {
        this.id = id == null ? "" : id;
        this.active = active;
        this.priceMultiplier = Math.max(0.0D, priceMultiplier);
        this.taxOverridePercent = taxOverridePercent;
        this.brandingOverrideId = brandingOverrideId == null ? "" : brandingOverrideId;
    }

    public static CampaignEffect inactive() {
        return new CampaignEffect("", false, 1.0D, null, "");
    }

    public String getId() {
        return this.id;
    }

    public boolean isActive() {
        return this.active;
    }

    public double getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public Double getTaxOverridePercent() {
        return this.taxOverridePercent;
    }

    public String getBrandingOverrideId() {
        return this.brandingOverrideId;
    }
}
