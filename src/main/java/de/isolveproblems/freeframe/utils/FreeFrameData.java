package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private int stock;
    private int maxStock;
    private boolean autoRefill;
    private long refillIntervalMillis;
    private long lastRefillAt;
    private double revenueTotal;
    private String displayEntityUuid;
    private FrameType frameType;
    private BlockReference linkedChest;
    private final List<PurchaseProfile> purchaseProfiles;

    public FreeFrameData(String id, FrameReference reference, String ownerUuid, String ownerName, long createdAt,
                         String itemType, double price, String currency, boolean active) {
        this(id, reference, ownerUuid, ownerName, createdAt, itemType, price, currency, active,
            64, 64, false, 300_000L, System.currentTimeMillis(), 0.0D, "", FrameType.SHOP, null, new ArrayList<PurchaseProfile>());
    }

    public FreeFrameData(String id, FrameReference reference, String ownerUuid, String ownerName, long createdAt,
                         String itemType, double price, String currency, boolean active,
                         int stock, int maxStock, boolean autoRefill, long refillIntervalMillis,
                         long lastRefillAt, double revenueTotal, String displayEntityUuid,
                         FrameType frameType, BlockReference linkedChest, List<PurchaseProfile> purchaseProfiles) {
        this.id = id;
        this.reference = reference;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
        this.itemType = itemType;
        this.price = price;
        this.currency = currency;
        this.active = active;
        this.stock = Math.max(0, stock);
        this.maxStock = Math.max(1, maxStock);
        this.autoRefill = autoRefill;
        this.refillIntervalMillis = Math.max(0L, refillIntervalMillis);
        this.lastRefillAt = Math.max(0L, lastRefillAt);
        this.revenueTotal = Math.max(0.0D, revenueTotal);
        this.displayEntityUuid = displayEntityUuid == null ? "" : displayEntityUuid;
        this.frameType = frameType == null ? FrameType.SHOP : frameType;
        this.linkedChest = linkedChest;
        this.purchaseProfiles = new ArrayList<PurchaseProfile>();
        this.setPurchaseProfiles(purchaseProfiles);

        if (this.stock > this.maxStock) {
            this.stock = this.maxStock;
        }
    }

    public static FreeFrameData fromSection(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        FrameReference reference = FrameReference.parse(section.getString("reference", ""));
        if (reference == null) {
            return null;
        }

        int maxStock = Math.max(1, section.getInt("maxStock", section.getInt("stock", 64)));
        int stock = Math.max(0, Math.min(maxStock, section.getInt("stock", maxStock)));

        return new FreeFrameData(
            id,
            reference,
            section.getString("ownerUuid", "unknown"),
            section.getString("ownerName", "unknown"),
            section.getLong("createdAt", System.currentTimeMillis()),
            section.getString("itemType", "UNKNOWN"),
            section.getDouble("price", 0.0D),
            section.getString("currency", "$"),
            section.getBoolean("active", true),
            stock,
            maxStock,
            section.getBoolean("autoRefill", false),
            section.getLong("refillIntervalMillis", 300_000L),
            section.getLong("lastRefillAt", System.currentTimeMillis()),
            Math.max(0.0D, section.getDouble("revenueTotal", 0.0D)),
            section.getString("displayEntityUuid", ""),
            FrameType.fromString(section.getString("frameType", "SHOP")),
            BlockReference.parse(section.getString("linkedChest", "")),
            readPurchaseProfiles(section.getConfigurationSection("profiles"))
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
        section.set("stock", this.stock);
        section.set("maxStock", this.maxStock);
        section.set("autoRefill", this.autoRefill);
        section.set("refillIntervalMillis", this.refillIntervalMillis);
        section.set("lastRefillAt", this.lastRefillAt);
        section.set("revenueTotal", this.revenueTotal);
        section.set("displayEntityUuid", this.displayEntityUuid == null ? "" : this.displayEntityUuid);
        section.set("frameType", this.frameType.name());
        section.set("linkedChest", this.linkedChest == null ? "" : this.linkedChest.serialize());
        section.set("profiles", null);
        ConfigurationSection profilesSection = section.createSection("profiles");
        int index = 0;
        for (PurchaseProfile profile : this.purchaseProfiles) {
            ConfigurationSection entry = profilesSection.createSection(String.valueOf(index++));
            entry.set("slot", profile.getSlot());
            entry.set("amount", profile.getAmount());
            entry.set("price", profile.getPrice());
            entry.set("displayName", profile.getDisplayName());
        }
    }

    public boolean applyAutoRefillIfDue(long now) {
        if (!this.autoRefill || this.refillIntervalMillis <= 0L) {
            return false;
        }

        if (now - this.lastRefillAt < this.refillIntervalMillis) {
            return false;
        }

        this.stock = this.maxStock;
        this.lastRefillAt = now;
        return true;
    }

    public boolean consumeStock(int amount) {
        int required = Math.max(1, amount);
        if (this.stock < required) {
            return false;
        }

        this.stock -= required;
        return true;
    }

    public void addRevenue(double amount) {
        if (amount > 0.0D) {
            this.revenueTotal += amount;
        }
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
        this.price = Math.max(0.0D, price);
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

    public int getStock() {
        return this.stock;
    }

    public void setStock(int stock) {
        this.stock = Math.max(0, Math.min(stock, this.maxStock));
    }

    public int getMaxStock() {
        return this.maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = Math.max(1, maxStock);
        if (this.stock > this.maxStock) {
            this.stock = this.maxStock;
        }
    }

    public boolean isAutoRefill() {
        return this.autoRefill;
    }

    public void setAutoRefill(boolean autoRefill) {
        this.autoRefill = autoRefill;
    }

    public long getRefillIntervalMillis() {
        return this.refillIntervalMillis;
    }

    public void setRefillIntervalMillis(long refillIntervalMillis) {
        this.refillIntervalMillis = Math.max(0L, refillIntervalMillis);
    }

    public long getLastRefillAt() {
        return this.lastRefillAt;
    }

    public void setLastRefillAt(long lastRefillAt) {
        this.lastRefillAt = Math.max(0L, lastRefillAt);
    }

    public double getRevenueTotal() {
        return this.revenueTotal;
    }

    public void setRevenueTotal(double revenueTotal) {
        this.revenueTotal = Math.max(0.0D, revenueTotal);
    }

    public String getDisplayEntityUuid() {
        return this.displayEntityUuid;
    }

    public void setDisplayEntityUuid(String displayEntityUuid) {
        this.displayEntityUuid = displayEntityUuid == null ? "" : displayEntityUuid;
    }

    public FrameType getFrameType() {
        return this.frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType == null ? FrameType.SHOP : frameType;
    }

    public BlockReference getLinkedChest() {
        return this.linkedChest;
    }

    public void setLinkedChest(BlockReference linkedChest) {
        this.linkedChest = linkedChest;
    }

    public List<PurchaseProfile> getPurchaseProfiles() {
        return Collections.unmodifiableList(this.purchaseProfiles);
    }

    public void setPurchaseProfiles(List<PurchaseProfile> profiles) {
        this.purchaseProfiles.clear();
        if (profiles != null) {
            this.purchaseProfiles.addAll(profiles);
        }
        Collections.sort(this.purchaseProfiles, new Comparator<PurchaseProfile>() {
            @Override
            public int compare(PurchaseProfile left, PurchaseProfile right) {
                return Integer.compare(left.getSlot(), right.getSlot());
            }
        });
    }

    public PurchaseProfile findProfileBySlot(int slot) {
        for (PurchaseProfile profile : this.purchaseProfiles) {
            if (profile.getSlot() == slot) {
                return profile;
            }
        }
        return null;
    }

    public boolean isOwnedBy(String uuid) {
        return uuid != null && uuid.equalsIgnoreCase(this.ownerUuid);
    }

    private static List<PurchaseProfile> readPurchaseProfiles(ConfigurationSection section) {
        List<PurchaseProfile> profiles = new ArrayList<PurchaseProfile>();
        if (section == null) {
            return profiles;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection profileSection = section.getConfigurationSection(key);
            if (profileSection == null) {
                continue;
            }
            profiles.add(new PurchaseProfile(
                profileSection.getInt("slot", 0),
                profileSection.getInt("amount", 1),
                profileSection.getDouble("price", 0.0D),
                profileSection.getString("displayName", "")
            ));
        }
        return profiles;
    }
}
