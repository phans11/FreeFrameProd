package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FrameRegistry {
    private static final String FRAMES_DATA_PATH = "freeframe.framesData";
    private static final String LEGACY_FRAMES_LIST_PATH = "freeframe.frames";

    private final FreeFrame freeframe;
    private final Map<String, FreeFrameData> framesById = new HashMap<String, FreeFrameData>();
    private final Map<FrameReference, String> frameIdByReference = new HashMap<FrameReference, String>();

    public FrameRegistry(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public synchronized void loadFromConfig() {
        this.framesById.clear();
        this.frameIdByReference.clear();

        ConfigurationSection section = this.freeframe.getPluginConfig().getConfigurationSection(FRAMES_DATA_PATH);
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            FreeFrameData data = FreeFrameData.fromSection(id, section.getConfigurationSection(id));
            if (data == null) {
                continue;
            }
            this.framesById.put(id, data);
            this.frameIdByReference.put(data.getReference(), id);
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

            String id = this.generateId();
            FreeFrameData data = new FreeFrameData(
                id,
                reference,
                "unknown",
                "unknown",
                System.currentTimeMillis(),
                "UNKNOWN",
                this.defaultPrice(),
                this.defaultCurrency(),
                true
            );
            this.framesById.put(id, data);
            this.frameIdByReference.put(reference, id);
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
                if (itemStack != null && itemStack.getType() != null) {
                    existing.setItemType(itemStack.getType().name());
                }
                if (existing.getReference() == null) {
                    existing.setReference(reference);
                }
                this.framesById.put(existingId, existing);
                this.frameIdByReference.put(reference, existingId);
                this.saveToConfig();
                return existing;
            }
        }

        String ownerUuid = owner == null ? "unknown" : owner.getUniqueId().toString();
        String ownerName = owner == null ? "unknown" : owner.getName();
        String itemType = (itemStack == null || itemStack.getType() == null) ? "UNKNOWN" : itemStack.getType().name();

        String id = this.generateId();
        FreeFrameData created = new FreeFrameData(
            id,
            reference,
            ownerUuid,
            ownerName,
            System.currentTimeMillis(),
            itemType,
            this.defaultPrice(),
            this.defaultCurrency(),
            true
        );

        this.framesById.put(id, created);
        this.frameIdByReference.put(reference, id);
        this.saveToConfig();
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
        return this.framesById.get(id.toLowerCase());
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

    public synchronized boolean removeById(String id) {
        if (id == null) {
            return false;
        }

        String normalizedId = id.toLowerCase();
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
        this.freeframe.getPluginConfig().set(FRAMES_DATA_PATH, null);
        ConfigurationSection root = this.freeframe.getPluginConfig().createSection(FRAMES_DATA_PATH);

        List<FreeFrameData> ordered = this.listFrames();
        for (FreeFrameData data : ordered) {
            ConfigurationSection frameSection = root.createSection(data.getId());
            data.writeToSection(frameSection);
        }

        this.freeframe.getConfigHandler().getConfigApi().saveConfig();
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

        return changed;
    }

    private boolean removeInternal(String id) {
        FreeFrameData removed = this.framesById.remove(id);
        if (removed == null) {
            return false;
        }

        this.frameIdByReference.remove(removed.getReference());
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
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toLowerCase();
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
