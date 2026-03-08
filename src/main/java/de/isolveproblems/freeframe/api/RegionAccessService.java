package de.isolveproblems.freeframe.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionAccessService {
    boolean canUse(Location location, Player player);
}
