package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FrameRegistry {
    private static final String CONFIG_PATH = "freeframe.frames";

    private final FreeFrame freeframe;
    private final Set<FrameReference> trackedFrames = new HashSet<FrameReference>();

    public FrameRegistry(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void loadFromConfig() {
        this.trackedFrames.clear();

        List<String> values = this.freeframe.getPluginConfig().getStringList(CONFIG_PATH);
        for (String value : values) {
            FrameReference reference = FrameReference.parse(value);
            if (reference != null) {
                this.trackedFrames.add(reference);
            }
        }
    }

    public int cleanupInvalidReferences() {
        int removed = 0;
        Iterator<FrameReference> iterator = this.trackedFrames.iterator();
        while (iterator.hasNext()) {
            FrameReference reference = iterator.next();
            if (!this.existsOnServer(reference)) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            this.saveToConfig();
        }

        return removed;
    }

    public boolean isTracked(ItemFrame frame) {
        FrameReference reference = FrameReference.fromItemFrame(frame);
        return reference != null && this.trackedFrames.contains(reference);
    }

    public void track(ItemFrame frame) {
        FrameReference reference = FrameReference.fromItemFrame(frame);
        if (reference != null && this.trackedFrames.add(reference)) {
            this.saveToConfig();
        }
    }

    public void untrack(ItemFrame frame) {
        FrameReference reference = FrameReference.fromItemFrame(frame);
        if (reference != null && this.trackedFrames.remove(reference)) {
            this.saveToConfig();
        }
    }

    public void saveToConfig() {
        List<String> serialized = new ArrayList<String>();
        for (FrameReference reference : this.trackedFrames) {
            serialized.add(reference.serialize());
        }
        Collections.sort(serialized);

        this.freeframe.getPluginConfig().set(CONFIG_PATH, serialized);
        this.freeframe.getConfigHandler().getConfigApi().saveConfig();
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
