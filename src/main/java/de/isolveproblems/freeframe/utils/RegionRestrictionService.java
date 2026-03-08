package de.isolveproblems.freeframe.utils;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RegionRestrictionService {

    private static class CuboidRegion {
        private final String world;
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;

        private CuboidRegion(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.world = world;
            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.maxZ = Math.max(minZ, maxZ);
        }

        private boolean contains(Location location) {
            if (location == null || location.getWorld() == null) {
                return false;
            }
            if (!location.getWorld().getName().equalsIgnoreCase(this.world)) {
                return false;
            }

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= this.minX && x <= this.maxX
                && y >= this.minY && y <= this.maxY
                && z >= this.minZ && z <= this.maxZ;
        }
    }

    public boolean isAllowed(FileConfiguration config, Location location) {
        return this.isWorldAllowed(config, location) && this.isRegionAllowed(config, location);
    }

    private boolean isWorldAllowed(FileConfiguration config, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        if (!config.getBoolean("freeframe.restrictions.worlds.enabled", false)) {
            return true;
        }

        String worldName = location.getWorld().getName();
        String mode = config.getString("freeframe.restrictions.worlds.mode", "whitelist");
        Set<String> worlds = new HashSet<String>();
        for (String world : config.getStringList("freeframe.restrictions.worlds.list")) {
            if (world != null && !world.trim().isEmpty()) {
                worlds.add(world.trim().toLowerCase(Locale.ENGLISH));
            }
        }

        boolean contains = worlds.contains(worldName.toLowerCase(Locale.ENGLISH));
        if ("blacklist".equalsIgnoreCase(mode)) {
            return !contains;
        }
        return contains;
    }

    private boolean isRegionAllowed(FileConfiguration config, Location location) {
        if (!config.getBoolean("freeframe.restrictions.regions.enabled", false)) {
            return true;
        }

        List<CuboidRegion> regions = this.parseRegions(config.getStringList("freeframe.restrictions.regions.list"));
        if (regions.isEmpty()) {
            return true;
        }

        for (CuboidRegion region : regions) {
            if (region.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private List<CuboidRegion> parseRegions(List<String> lines) {
        List<CuboidRegion> regions = new ArrayList<CuboidRegion>();
        if (lines == null) {
            return regions;
        }

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            try {
                // Format: world:minX,minY,minZ:maxX,maxY,maxZ
                String[] worldAndRange = line.split(":", 3);
                if (worldAndRange.length != 3) {
                    continue;
                }
                String world = worldAndRange[0].trim();
                String[] minParts = worldAndRange[1].split(",", 3);
                String[] maxParts = worldAndRange[2].split(",", 3);
                if (minParts.length != 3 || maxParts.length != 3 || world.isEmpty()) {
                    continue;
                }

                int minX = Integer.parseInt(minParts[0].trim());
                int minY = Integer.parseInt(minParts[1].trim());
                int minZ = Integer.parseInt(minParts[2].trim());
                int maxX = Integer.parseInt(maxParts[0].trim());
                int maxY = Integer.parseInt(maxParts[1].trim());
                int maxZ = Integer.parseInt(maxParts[2].trim());
                regions.add(new CuboidRegion(world, minX, minY, minZ, maxX, maxY, maxZ));
            } catch (Exception ignored) {
                // Invalid region config line is ignored to keep startup resilient.
            }
        }
        return regions;
    }
}
