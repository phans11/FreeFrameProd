package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.FrameType;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import de.isolveproblems.freeframe.api.SaleMode;
import de.isolveproblems.freeframe.api.ShopOwnerType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FrameRegistry {
    private static final String LEGACY_FRAMES_LIST_PATH = "freeframe.frames";

    private final FreeFrame freeframe;
    private final FrameStorageService storageService;
    private final Map<String, FreeFrameData> framesById = new HashMap<String, FreeFrameData>();
    private final Map<FrameReference, String> frameIdByReference = new HashMap<FrameReference, String>();

    public FrameRegistry(FreeFrame freeframe) {
        this.freeframe = freeframe;
        this.storageService = new FrameStorageService(freeframe);
    }

    public synchronized void loadFromConfig() {
        this.framesById.clear();
        this.frameIdByReference.clear();

        Map<String, FreeFrameData> loaded = this.storageService.loadFrames();
        boolean normalized = false;
        for (Map.Entry<String, FreeFrameData> entry : loaded.entrySet()) {
            FreeFrameData data = entry.getValue();
            if (data == null || data.getReference() == null) {
                continue;
            }

            if (this.normalize(data)) {
                normalized = true;
            }

            String id = data.getId().toLowerCase(Locale.ENGLISH);
            this.framesById.put(id, data);
            this.frameIdByReference.put(data.getReference(), id);
        }

        if (normalized) {
            this.saveToConfig();
        }
    }

    public synchronized int migrateLegacyFrames() {
        Object legacyRaw = this.freeframe.getPluginConfig().get(LEGACY_FRAMES_LIST_PATH);
        if (!(legacyRaw instanceof List)) {
            return 0;
        }

        List<String> legacyFrames = this.freeframe.getPluginConfig().getStringList(LEGACY_FRAMES_LIST_PATH);
        int migrated = 0;
        for (String serialized : legacyFrames) {
            FrameReference reference = FrameReference.parse(serialized);
            if (reference == null || this.frameIdByReference.containsKey(reference)) {
                continue;
            }

            FreeFrameData data = this.createDefaultData(this.generateId(), reference, null, null);
            this.framesById.put(data.getId(), data);
            this.frameIdByReference.put(reference, data.getId());
            this.freeframe.getDisplayService().refresh(data);
            migrated++;
        }

        if (migrated > 0) {
            this.freeframe.getPluginConfig().set(LEGACY_FRAMES_LIST_PATH, null);
            this.saveToConfig();
            this.freeframe.getMetricsTracker().incrementMigrations();
        }

        return migrated;
    }

    public synchronized FrameRepairReport repairFrames() {
        int removedInvalid = 0;
        int removedDuplicates = 0;
        int normalized = 0;

        Set<FrameReference> seenReferences = new HashSet<FrameReference>();
        List<String> ids = new ArrayList<String>(this.framesById.keySet());
        for (String id : ids) {
            FreeFrameData data = this.framesById.get(id);
            if (data == null || data.getReference() == null) {
                this.removeInternal(id);
                removedInvalid++;
                continue;
            }

            if (!seenReferences.add(data.getReference())) {
                this.removeInternal(id);
                removedDuplicates++;
                continue;
            }

            if (!this.existsOnServer(data.getReference())) {
                this.removeInternal(id);
                removedInvalid++;
                continue;
            }

            if (this.normalize(data)) {
                normalized++;
            }
        }

        if (removedInvalid > 0 || removedDuplicates > 0 || normalized > 0) {
            this.rebuildReferenceMap();
            this.saveToConfig();
            this.freeframe.getMetricsTracker().incrementRepairs();
        }

        return new FrameRepairReport(removedInvalid, removedDuplicates, normalized);
    }

    public synchronized int cleanupInvalidReferences() {
        return this.repairFrames().getRemovedInvalidFrames();
    }

    public synchronized FreeFrameData getOrCreate(ItemFrame frame, Player owner, ItemStack itemStack) {
        FrameReference reference = FrameReference.fromItemFrame(frame);
        if (reference == null) {
            return null;
        }

        String existingId = this.frameIdByReference.get(reference);
        if (existingId != null) {
            FreeFrameData existing = this.framesById.get(existingId);
            if (existing != null) {
                boolean changed = false;

                if (itemStack != null && itemStack.getType() != null) {
                    String itemType = itemStack.getType().name();
                    if (!itemType.equalsIgnoreCase(existing.getItemType())) {
                        existing.setItemType(itemType);
                        changed = true;
                    }
                }

                if (("unknown".equalsIgnoreCase(existing.getOwnerUuid()) || "unknown".equalsIgnoreCase(existing.getOwnerName())) && owner != null) {
                    existing.setOwnerUuid(owner.getUniqueId().toString());
                    existing.setOwnerName(owner.getName());
                    changed = true;
                }

                if (existing.getReference() == null) {
                    existing.setReference(reference);
                    changed = true;
                }

                if (existing.applyAutoRefillIfDue(System.currentTimeMillis())) {
                    changed = true;
                }

                if (this.normalize(existing)) {
                    changed = true;
                }

                if (changed) {
                    this.framesById.put(existingId, existing);
                    this.frameIdByReference.put(reference, existingId);
                    this.saveToConfig();
                }

                this.freeframe.getDisplayService().refresh(existing);
                return existing;
            }
        }

        FreeFrameData created = this.createDefaultData(this.generateId(), reference, owner, itemStack);
        this.framesById.put(created.getId(), created);
        this.frameIdByReference.put(reference, created.getId());
        this.saveToConfig();
        this.freeframe.getDisplayService().refresh(created);
        this.freeframe.getMetricsTracker().incrementFramesCreated();
        return created;
    }

    public synchronized boolean isTracked(ItemFrame frame) {
        return this.findByFrame(frame) != null;
    }

    public synchronized FreeFrameData findByFrame(ItemFrame frame) {
        FrameReference reference = FrameReference.fromItemFrame(frame);
        if (reference == null) {
            return null;
        }

        String id = this.frameIdByReference.get(reference);
        if (id == null) {
            return null;
        }
        return this.framesById.get(id);
    }

    public synchronized FreeFrameData findById(String id) {
        if (id == null) {
            return null;
        }
        return this.framesById.get(id.toLowerCase(Locale.ENGLISH));
    }

    public synchronized List<FreeFrameData> listFrames() {
        List<FreeFrameData> values = new ArrayList<FreeFrameData>(this.framesById.values());
        Collections.sort(values, new Comparator<FreeFrameData>() {
            @Override
            public int compare(FreeFrameData left, FreeFrameData right) {
                return Long.compare(right.getCreatedAt(), left.getCreatedAt());
            }
        });
        return values;
    }

    public synchronized int size() {
        return this.framesById.size();
    }

    public synchronized List<FreeFrameData> listByNetwork(String networkId) {
        List<FreeFrameData> matches = new ArrayList<FreeFrameData>();
        if (networkId == null || networkId.trim().isEmpty()) {
            return matches;
        }

        String normalized = networkId.trim().toLowerCase(Locale.ENGLISH);
        for (FreeFrameData data : this.framesById.values()) {
            if (normalized.equals(data.getNetworkId())) {
                matches.add(data);
            }
        }
        return matches;
    }

    public synchronized void replaceAll(List<FreeFrameData> frames) {
        this.framesById.clear();
        this.frameIdByReference.clear();

        if (frames != null) {
            for (FreeFrameData data : frames) {
                if (data == null || data.getReference() == null) {
                    continue;
                }

                this.normalize(data);
                String id = data.getId().toLowerCase(Locale.ENGLISH);
                this.framesById.put(id, data);
                this.frameIdByReference.put(data.getReference(), id);
            }
        }

        this.saveToConfig();
    }

    public synchronized boolean removeById(String id) {
        if (id == null) {
            return false;
        }

        String normalizedId = id.toLowerCase(Locale.ENGLISH);
        boolean removed = this.removeInternal(normalizedId);
        if (removed) {
            this.saveToConfig();
        }
        return removed;
    }

    public synchronized boolean updatePrice(String id, double price, String currency) {
        FreeFrameData data = this.findById(id);
        if (data == null) {
            return false;
        }

        data.setPrice(Math.max(0.0D, price));
        if (currency != null && !currency.trim().isEmpty()) {
            data.setCurrency(currency.trim());
        }

        this.freeframe.getDisplayService().refresh(data);
        this.saveToConfig();
        return true;
    }

    public synchronized void untrack(ItemFrame frame) {
        FreeFrameData data = this.findByFrame(frame);
        if (data != null) {
            this.removeById(data.getId());
        }
    }

    public synchronized void saveToConfig() {
        this.storageService.saveFrames(this.listFrames());
    }

    public synchronized void flushStorage() {
        this.storageService.flushAndShutdown();
    }

    public synchronized String getActiveStorageBackend() {
        return this.storageService.resolveType().name().toLowerCase(Locale.ENGLISH);
    }

    private FreeFrameData createDefaultData(String id, FrameReference reference, Player owner, ItemStack itemStack) {
        String ownerUuid = owner == null ? "unknown" : owner.getUniqueId().toString();
        String ownerName = owner == null ? "unknown" : owner.getName();
        String itemType = (itemStack == null || itemStack.getType() == null) ? "UNKNOWN" : itemStack.getType().name();

        int maxStock = Math.max(1, this.freeframe.getPluginConfig().getInt("freeframe.stock.defaultMax", 64));
        int stock = Math.max(0, Math.min(maxStock, this.freeframe.getPluginConfig().getInt("freeframe.stock.default", maxStock)));
        ShopOwnerType ownerType = ShopOwnerType.USER;
        if (owner != null && owner.hasPermission(this.freeframe.getConfigHandler().getAdminPermissionNode())) {
            ownerType = ShopOwnerType.ADMIN;
        }

        return new FreeFrameData(
            id,
            reference,
            ownerUuid,
            ownerName,
            System.currentTimeMillis(),
            itemType,
            this.defaultPrice(),
            this.defaultCurrency(),
            true,
            stock,
            maxStock,
            this.freeframe.getPluginConfig().getBoolean("freeframe.stock.autoRefill.defaultEnabled", false),
            Math.max(0L, this.freeframe.getPluginConfig().getLong("freeframe.stock.autoRefill.defaultIntervalMillis", 300_000L)),
            System.currentTimeMillis(),
            0.0D,
            "",
            FrameType.fromString(this.freeframe.getPluginConfig().getString("freeframe.types.default", "SHOP")),
            null,
            this.defaultPurchaseProfiles(),
            ownerType,
            "",
            "",
            SaleMode.fromString(this.freeframe.getPluginConfig().getString("freeframe.saleMode.default", "INSTANT")),
            0L,
            0.0D,
            0.0D,
            "",
            "",
            0.0D
        );
    }

    private boolean normalize(FreeFrameData data) {
        boolean changed = false;

        if (data.getOwnerUuid() == null || data.getOwnerUuid().trim().isEmpty()) {
            data.setOwnerUuid("unknown");
            changed = true;
        }

        if (data.getOwnerName() == null || data.getOwnerName().trim().isEmpty()) {
            data.setOwnerName("unknown");
            changed = true;
        }

        if (data.getCreatedAt() <= 0L) {
            data.setCreatedAt(System.currentTimeMillis());
            changed = true;
        }

        if (data.getItemType() == null || data.getItemType().trim().isEmpty()) {
            data.setItemType("UNKNOWN");
            changed = true;
        }

        if (data.getCurrency() == null || data.getCurrency().trim().isEmpty()) {
            data.setCurrency(this.defaultCurrency());
            changed = true;
        }

        if (data.getPrice() < 0.0D) {
            data.setPrice(0.0D);
            changed = true;
        }

        if (data.getMaxStock() < 1) {
            data.setMaxStock(Math.max(1, this.freeframe.getPluginConfig().getInt("freeframe.stock.defaultMax", 64)));
            changed = true;
        }

        if (data.getStock() < 0 || data.getStock() > data.getMaxStock()) {
            data.setStock(Math.min(data.getMaxStock(), Math.max(0, data.getStock())));
            changed = true;
        }

        if (data.getRefillIntervalMillis() < 0L) {
            data.setRefillIntervalMillis(0L);
            changed = true;
        }

        if (data.getLastRefillAt() <= 0L) {
            data.setLastRefillAt(System.currentTimeMillis());
            changed = true;
        }

        if (data.getRevenueTotal() < 0.0D) {
            data.setRevenueTotal(0.0D);
            changed = true;
        }

        if (data.getFrameType() == null) {
            data.setFrameType(FrameType.fromString(this.freeframe.getPluginConfig().getString("freeframe.types.default", "SHOP")));
            changed = true;
        }

        if (data.getPurchaseProfiles().isEmpty()) {
            data.setPurchaseProfiles(this.defaultPurchaseProfiles());
            changed = true;
        }

        if (data.getShopOwnerType() == null) {
            data.setShopOwnerType(ShopOwnerType.USER);
            changed = true;
        }

        if (data.getNetworkId() == null) {
            data.setNetworkId("");
            changed = true;
        }

        if (data.getSeasonRuleId() == null) {
            data.setSeasonRuleId("");
            changed = true;
        }

        if (data.getSaleMode() == null) {
            data.setSaleMode(SaleMode.INSTANT);
            changed = true;
        }

        if (data.getAuctionEndAt() < 0L) {
            data.setAuctionEndAt(0L);
            changed = true;
        }

        if (data.getAuctionMinBid() < 0.0D) {
            data.setAuctionMinBid(0.0D);
            changed = true;
        }

        if (data.getHighestBid() < 0.0D) {
            data.setHighestBid(0.0D);
            changed = true;
        }

        if (data.getHighestBidderUuid() == null) {
            data.setHighestBidderUuid("");
            changed = true;
        }

        if (data.getHighestBidderName() == null) {
            data.setHighestBidderName("");
            changed = true;
        }

        if (data.getCollectedTaxTotal() < 0.0D) {
            data.setCollectedTaxTotal(0.0D);
            changed = true;
        }

        return changed;
    }

    private boolean removeInternal(String id) {
        FreeFrameData removed = this.framesById.remove(id);
        if (removed == null) {
            return false;
        }

        this.frameIdByReference.remove(removed.getReference());
        this.freeframe.getDisplayService().remove(removed);
        return true;
    }

    private void rebuildReferenceMap() {
        this.frameIdByReference.clear();
        for (Map.Entry<String, FreeFrameData> entry : this.framesById.entrySet()) {
            FreeFrameData data = entry.getValue();
            if (data != null && data.getReference() != null) {
                this.frameIdByReference.put(data.getReference(), entry.getKey());
            }
        }
    }

    private String generateId() {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toLowerCase(Locale.ENGLISH);
        } while (this.framesById.containsKey(id));
        return id;
    }

    private double defaultPrice() {
        return Math.max(0.0D, this.freeframe.getPluginConfig().getDouble("freeframe.default.price", 0.0D));
    }

    private String defaultCurrency() {
        String currency = this.freeframe.getPluginConfig().getString("freeframe.default.currency", "$");
        return currency == null || currency.trim().isEmpty() ? "$" : currency.trim();
    }

    private List<PurchaseProfile> defaultPurchaseProfiles() {
        return this.freeframe.getDefaultPurchaseProfiles(this.defaultPrice());
    }

    private boolean existsOnServer(FrameReference reference) {
        World world = Bukkit.getWorld(reference.getWorldName());
        if (world == null) {
            return false;
        }

        int chunkX = reference.getX() >> 4;
        int chunkZ = reference.getZ() >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ) && !world.loadChunk(chunkX, chunkZ, false)) {
            return false;
        }

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ItemFrame && reference.matches((ItemFrame) entity)) {
                return true;
            }
        }
        return false;
    }
}
