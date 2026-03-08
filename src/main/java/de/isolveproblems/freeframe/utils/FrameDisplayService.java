package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public class FrameDisplayService {
    private final FreeFrame freeframe;

    public FrameDisplayService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void refreshAll(Collection<FreeFrameData> frames) {
        if (frames == null) {
            return;
        }

        for (FreeFrameData frame : frames) {
            this.refresh(frame);
        }
    }

    public void refresh(FreeFrameData frameData) {
        if (frameData == null) {
            return;
        }

        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.display.enabled", true)) {
            this.remove(frameData);
            return;
        }

        if (this.freeframe.getPluginConfig().getBoolean("freeframe.display.onlyWhenInStock", false) && frameData.getStock() <= 0) {
            this.remove(frameData);
            return;
        }

        ItemFrame frame = this.findItemFrame(frameData.getReference());
        if (frame == null) {
            this.remove(frameData);
            return;
        }

        Location displayLocation = this.resolveDisplayLocation(frame.getLocation());
        ArmorStand stand = this.resolveDisplay(frameData, frame.getWorld());
        if (stand == null) {
            Entity created = frame.getWorld().spawnEntity(displayLocation, EntityType.ARMOR_STAND);
            if (!(created instanceof ArmorStand)) {
                return;
            }
            stand = (ArmorStand) created;
        }

        stand.teleport(displayLocation);
        stand.setVisible(this.freeframe.getPluginConfig().getBoolean("freeframe.display.armorStand.visible", false));
        stand.setGravity(this.freeframe.getPluginConfig().getBoolean("freeframe.display.armorStand.gravity", false));
        stand.setCustomNameVisible(this.freeframe.getPluginConfig().getBoolean("freeframe.display.armorStand.customNameVisible", true));
        stand.setCustomName(this.buildDisplayName(frameData));
        stand.setSmall(this.freeframe.getPluginConfig().getBoolean("freeframe.display.armorStand.small", true));
        this.trySetMarker(stand);

        frameData.setDisplayEntityUuid(stand.getUniqueId().toString());
    }

    public void remove(FreeFrameData frameData) {
        if (frameData == null || frameData.getDisplayEntityUuid() == null || frameData.getDisplayEntityUuid().trim().isEmpty()) {
            return;
        }

        try {
            UUID uuid = UUID.fromString(frameData.getDisplayEntityUuid());
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (uuid.equals(entity.getUniqueId())) {
                        entity.remove();
                    }
                }
            }
        } catch (Exception ignored) {
            // malformed UUID or unsupported world lookup
        }

        frameData.setDisplayEntityUuid("");
    }

    private ArmorStand resolveDisplay(FreeFrameData frameData, World world) {
        if (world == null) {
            return null;
        }

        String displayUuid = frameData.getDisplayEntityUuid();
        if (displayUuid != null && !displayUuid.trim().isEmpty()) {
            try {
                UUID uuid = UUID.fromString(displayUuid);
                for (Entity entity : world.getEntities()) {
                    if (uuid.equals(entity.getUniqueId()) && entity instanceof ArmorStand) {
                        return (ArmorStand) entity;
                    }
                }
            } catch (Exception ignored) {
                // malformed UUID or no entity in world
            }
        }
        return null;
    }

    private ItemFrame findItemFrame(FrameReference reference) {
        if (reference == null) {
            return null;
        }

        World world = Bukkit.getWorld(reference.getWorldName());
        if (world == null) {
            return null;
        }

        int chunkX = reference.getX() >> 4;
        int chunkZ = reference.getZ() >> 4;
        boolean loadChunk = this.freeframe.getPluginConfig().getBoolean("freeframe.display.loadChunk", false);
        if (!world.isChunkLoaded(chunkX, chunkZ) && (!loadChunk || !world.loadChunk(chunkX, chunkZ, false))) {
            return null;
        }

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ItemFrame && reference.matches((ItemFrame) entity)) {
                return (ItemFrame) entity;
            }
        }
        return null;
    }

    private String buildDisplayName(FreeFrameData frameData) {
        String template = this.freeframe.getPluginConfig().getString(
            "freeframe.display.template",
            "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%"
        );

        if (template == null) {
            template = "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%";
        }

        String replaced = template
            .replace("%item%", frameData.getItemType())
            .replace("%currency%", frameData.getCurrency())
            .replace("%price%", String.format(Locale.ENGLISH, "%.2f", frameData.getPrice()))
            .replace("%stock%", String.valueOf(frameData.getStock()))
            .replace("%owner%", frameData.getOwnerName());

        return this.freeframe.colorize(this.freeframe.getPlaceholderSupport().apply(null, replaced));
    }

    private void trySetMarker(ArmorStand stand) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.display.armorStand.marker", true)) {
            return;
        }

        try {
            stand.getClass().getMethod("setMarker", boolean.class).invoke(stand, true);
        } catch (Throwable ignored) {
            // not available on some old API variants
        }
    }

    private Location resolveDisplayLocation(Location frameLocation) {
        double offsetX = this.freeframe.getPluginConfig().getDouble("freeframe.display.offset.x", 0.5D);
        double offsetY = this.freeframe.getPluginConfig().getDouble("freeframe.display.offset.y", 0.45D);
        double offsetZ = this.freeframe.getPluginConfig().getDouble("freeframe.display.offset.z", 0.5D);
        return frameLocation.clone().add(offsetX, offsetY, offsetZ);
    }
}
