package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
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

        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ENABLED)) {
            this.remove(frameData);
            return;
        }

        if (this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ONLYWHENINSTOCK) && frameData.getStock() <= 0) {
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
        stand.setVisible(this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ARMORSTAND_VISIBLE));
        stand.setGravity(this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ARMORSTAND_GRAVITY));
        stand.setCustomNameVisible(this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ARMORSTAND_CUSTOMNAMEVISIBLE));
        stand.setCustomName(this.buildDisplayName(frameData));
        stand.setSmall(this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ARMORSTAND_SMALL));
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
        boolean loadChunk = this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_LOADCHUNK);
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
        long now = System.currentTimeMillis();
        double projectedPrice = frameData.getPrice();
        projectedPrice = this.freeframe.getSeasonalRulesService().applyPriceMultiplier(frameData, projectedPrice, now);
        projectedPrice = this.freeframe.getDynamicPricingService().apply(
            frameData,
            projectedPrice,
            Math.max(0, frameData.getStock()),
            Math.max(1, frameData.getMaxStock()),
            now
        );
        projectedPrice = this.freeframe.getCampaignRuntimeService().applyPrice(frameData, projectedPrice, now);

        String forcedTheme = this.freeframe.getCampaignRuntimeService().resolve(frameData, now).getBrandingOverrideId();
        String template = this.freeframe.getBrandingService().resolveDisplayTemplate(frameData, forcedTheme);
        if (template == null || template.trim().isEmpty()) {
            template = "&e%item% &7| &6%currency%%price% &7| &bStock: %stock%";
        }

        String replaced = template
            .replace("%item%", frameData.getItemType())
            .replace("%currency%", frameData.getCurrency())
            .replace("%price%", String.format(Locale.ENGLISH, "%.2f", projectedPrice))
            .replace("%stock%", String.valueOf(frameData.getStock()))
            .replace("%owner%", frameData.getOwnerName())
            .replace("%network%", frameData.getNetworkId())
            .replace("%campaign%", frameData.getCampaignId())
            .replace("%brand%", frameData.getBrandingId());

        return this.freeframe.colorize(this.freeframe.getPlaceholderSupport().apply(null, replaced));
    }

    private void trySetMarker(ArmorStand stand) {
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_DISPLAY_ARMORSTAND_MARKER)) {
            return;
        }

        try {
            stand.getClass().getMethod("setMarker", boolean.class).invoke(stand, true);
        } catch (Throwable ignored) {
            // not available on some old API variants
        }
    }

    private Location resolveDisplayLocation(Location frameLocation) {
        double offsetX = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_DISPLAY_OFFSET_X);
        double offsetY = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_DISPLAY_OFFSET_Y);
        double offsetZ = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_DISPLAY_OFFSET_Z);
        return frameLocation.clone().add(offsetX, offsetY, offsetZ);
    }
}
