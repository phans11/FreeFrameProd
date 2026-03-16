package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PlatformSupportService {
    private final FreeFrame freeframe;

    public PlatformSupportService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public String detectRuntime() {
        Server server = Bukkit.getServer();
        if (server == null) {
            return "unknown";
        }
        String name = safe(server.getName()) + " " + safe(server.getVersion()) + " " + safe(server.getClass().getName());
        String lowered = name.toLowerCase(Locale.ENGLISH);
        if (lowered.contains("paper")) {
            return "paper";
        }
        if (lowered.contains("purpur")) {
            return "purpur";
        }
        if (lowered.contains("cauldron") || lowered.contains("kcauldron") || lowered.contains("mohist")) {
            return "cauldron";
        }
        if (lowered.contains("spigot")) {
            return "spigot";
        }
        if (lowered.contains("craftbukkit") || lowered.contains("bukkit")) {
            return "bukkit";
        }
        return "unknown";
    }

    public Map<String, String> describeCapabilities() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        String runtime = this.detectRuntime();
        values.put("runtime", runtime);
        values.put("paperApi", String.valueOf("paper".equals(runtime) || "purpur".equals(runtime)));
        values.put("cauldronCompat", String.valueOf("cauldron".equals(runtime)));
        values.put("proxyBungeeEnabled", String.valueOf(this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_PROXY_BUNGEECORD_ENABLED)));
        values.put("proxyVelocityEnabled", String.valueOf(this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_PROXY_VELOCITY_ENABLED)));
        values.put("networkSyncMode", this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_NETWORKSYNC_MODE));
        return values;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
