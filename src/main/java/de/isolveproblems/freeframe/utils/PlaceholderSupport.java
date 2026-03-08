package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;

public class PlaceholderSupport {
    private final FreeFrame freeframe;

    public PlaceholderSupport(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public String apply(Player player, String input) {
        if (input == null) {
            return "";
        }

        String replaced = this.applyInternalPlaceholders(player, input);
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.placeholderapi.enabled", true)) {
            return replaced;
        }

        if (!this.freeframe.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return replaced;
        }

        try {
            Class<?> placeholderApi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method setPlaceholders = placeholderApi.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
            Object result = setPlaceholders.invoke(null, player, replaced);
            return result == null ? replaced : String.valueOf(result);
        } catch (Throwable ignored) {
            return replaced;
        }
    }

    private String applyInternalPlaceholders(Player player, String input) {
        String output = input;
        output = output.replace("%freeframe_tracked_frames%", String.valueOf(this.freeframe.getFrameRegistry().size()));

        Map<String, Long> metrics = this.freeframe.getMetricsTracker().snapshot();
        for (Map.Entry<String, Long> entry : metrics.entrySet()) {
            output = output.replace("%freeframe_metric_" + entry.getKey() + "%", String.valueOf(entry.getValue()));
        }

        if (player != null) {
            output = output.replace("%freeframe_player_name%", player.getName());
            output = output.replace("%freeframe_player_uuid%", player.getUniqueId().toString());
        }

        return output;
    }
}
