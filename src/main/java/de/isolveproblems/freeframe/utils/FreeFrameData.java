package de.isolveproblems.freeframe.utils;

import org.bukkit.configuration.ConfigurationSection;

public class FreeFrameData {
    private final String id;
    private FrameReference reference;
    private String ownerUuid;
    private String ownerName;
    private long createdAt;
    private String itemType;
    private double price;
    private String currency;
    private boolean active;

    public FreeFrameData(String id, FrameReference reference, String ownerUuid, String ownerName, long createdAt,
                         String itemType, double price, String currency, boolean active) {
        this.id = id;
        this.reference = reference;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
        this.itemType = itemType;
        this.price = price;
        this.currency = currency;
        this.active = active;
    }

    public static FreeFrameData fromSection(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        FrameReference reference = FrameReference.parse(section.getString("reference", ""));
        if (reference == null) {
            return null;
        }

        return new FreeFrameData(
            id,
            reference,
            section.getString("ownerUuid", "unknown"),
            section.getString("ownerName", "unknown"),
            section.getLong("createdAt", System.currentTimeMillis()),
            section.getString("itemType", "UNKNOWN"),
            section.getDouble("price", 0.0D),
            section.getString("currency", "$") ,
            section.getBoolean("active", true)
        );
    }

    public void writeToSection(ConfigurationSection section) {
        section.set("reference", this.reference.serialize());
        section.set("ownerUuid", this.ownerUuid);
        section.set("ownerName", this.ownerName);
        section.set("createdAt", this.createdAt);
        section.set("itemType", this.itemType);
        section.set("price", this.price);
        section.set("currency", this.currency);
        section.set("active", this.active);
    }

    public String getId() {
        return this.id;
    }

    public FrameReference getReference() {
        return this.reference;
    }

    public void setReference(FrameReference reference) {
        this.reference = reference;
    }

    public String getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getItemType() {
        return this.itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isOwnedBy(String uuid) {
        return uuid != null && uuid.equalsIgnoreCase(this.ownerUuid);
    }
}
