package de.isolveproblems.freeframe.utils;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class BlockReference {
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;

    public BlockReference(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockReference fromLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return new BlockReference(
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }

    public static BlockReference parse(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            return null;
        }

        String[] parts = serialized.split("\\|", -1);
        if (parts.length != 4) {
            return null;
        }

        try {
            return new BlockReference(
                parts[0],
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
            );
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public String serialize() {
        return this.worldName + "|" + this.x + "|" + this.y + "|" + this.z;
    }

    public Location toLocation(World world) {
        if (world == null) {
            return null;
        }
        return new Location(world, this.x, this.y, this.z);
    }

    public String getWorldName() {
        return this.worldName;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BlockReference)) {
            return false;
        }

        BlockReference that = (BlockReference) other;
        return this.x == that.x
            && this.y == that.y
            && this.z == that.z
            && Objects.equals(this.worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldName, this.x, this.y, this.z);
    }
}
