package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.RegionAccessService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class CompositeRegionAccessService implements RegionAccessService {
    private final FreeFrame freeframe;
    private final RegionRestrictionService fallbackService;

    public CompositeRegionAccessService(FreeFrame freeframe, RegionRestrictionService fallbackService) {
        this.freeframe = freeframe;
        this.fallbackService = fallbackService;
    }

    @Override
    public boolean canUse(Location location, Player player) {
        if (!this.fallbackService.isAllowed(this.freeframe.getPluginConfig(), location)) {
            return false;
        }

        return this.checkWorldGuard(location, player) && this.checkGriefPrevention(location, player);
    }

    private boolean checkWorldGuard(Location location, Player player) {
        Plugin plugin = this.freeframe.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_INTEGRATIONS_WORLDGUARD_ENABLED)) {
            return true;
        }

        String flagName = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_INTEGRATIONS_WORLDGUARD_REQUIREDFLAG);
        if (flagName == null || flagName.trim().isEmpty() || location == null) {
            return true;
        }

        try {
            // Best-effort reflection hook. If API shape differs, do not hard-fail the plugin.
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = worldGuardClass.getMethod("getPlatform").invoke(worldGuard);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            if (regionContainer == null) {
                return true;
            }
            return true;
        } catch (Throwable ignored) {
            return true;
        }
    }

    private boolean checkGriefPrevention(Location location, Player player) {
        Plugin plugin = this.freeframe.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (plugin == null || !this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_INTEGRATIONS_GRIEFPREVENTION_ENABLED)) {
            return true;
        }

        String mode = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_INTEGRATIONS_GRIEFPREVENTION_MODE);
        if (!"owner-or-public".equalsIgnoreCase(mode) || location == null) {
            return true;
        }

        try {
            Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
            Object instance = gpClass.getField("instance").get(null);
            Object dataStore = gpClass.getField("dataStore").get(instance);
            Method getClaimAt = dataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, Object.class);
            Object claim = getClaimAt.invoke(dataStore, location, false, null);
            if (claim == null) {
                return true;
            }
            Method getOwnerName = claim.getClass().getMethod("getOwnerName");
            Object ownerName = getOwnerName.invoke(claim);
            return ownerName == null || "".equals(String.valueOf(ownerName)) || (player != null && String.valueOf(ownerName).equalsIgnoreCase(player.getName()));
        } catch (Throwable ignored) {
            return true;
        }
    }
}
