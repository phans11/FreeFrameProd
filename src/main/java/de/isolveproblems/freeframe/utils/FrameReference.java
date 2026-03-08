package de.isolveproblems.freeframe.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;

import java.util.Objects;
import java.util.regex.Pattern;

public final class FrameReference {
    private static final String DELIMITER = "|";

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final String attachedFace;

    public FrameReference(String worldName, int x, int y, int z, String attachedFace) {
        this.worldName = Objects.requireNonNull(worldName, "worldName");
        this.x = x;
        this.y = y;
        this.z = z;
        this.attachedFace = Objects.requireNonNull(attachedFace, "attachedFace");
    }

    public static FrameReference fromItemFrame(ItemFrame frame) {
        if (frame == null || frame.getWorld() == null) {
            return null;
        }

        Location location = frame.getLocation();
        BlockFace face = frame.getAttachedFace();
        String faceName = face == null ? "UNKNOWN" : face.name();
        return new FrameReference(
            frame.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            faceName
        );
    }

    public static FrameReference parse(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            return null;
        }

        String[] parts = serialized.split(Pattern.quote(DELIMITER), -1);
        if (parts.length != 5) {
            return null;
        }

        try {
            String world = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            String face = parts[4];
            if (world.isEmpty() || face.isEmpty()) {
                return null;
            }

            return new FrameReference(world, x, y, z, face);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public String serialize() {
        return this.worldName + DELIMITER + this.x + DELIMITER + this.y + DELIMITER + this.z + DELIMITER + this.attachedFace;
    }

    public boolean matches(ItemFrame frame) {
        FrameReference other = fromItemFrame(frame);
        return this.equals(other);
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

    public String getAttachedFace() {
        return this.attachedFace;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof FrameReference)) {
            return false;
        }

        FrameReference that = (FrameReference) object;
        return this.x == that.x
            && this.y == that.y
            && this.z == that.z
            && this.worldName.equals(that.worldName)
            && this.attachedFace.equals(that.attachedFace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldName, this.x, this.y, this.z, this.attachedFace);
    }
}
