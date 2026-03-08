package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.api.StatisticsService;
import de.isolveproblems.freeframe.api.ConfigAPI;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocalStatisticsService implements StatisticsService {
    private final ConfigAPI configApi;

    public LocalStatisticsService(java.io.File dataFolder) {
        this.configApi = new ConfigAPI(dataFolder, "stats.yml");
    }

    @Override
    public synchronized void recordPurchase(Player player, FreeFrameData frameData, int amount, double price) {
        if (frameData == null) {
            return;
        }

        String framePath = "frames." + frameData.getId();
        this.increment(framePath + ".purchases", 1L);
        this.increment(framePath + ".items", amount);
        this.increment(framePath + ".revenueCents", Math.round(price * 100.0D));

        if (player != null) {
            String playerPath = "players." + player.getUniqueId().toString();
            this.increment(playerPath + ".purchases", 1L);
            this.increment(playerPath + ".items", amount);
            this.increment(playerPath + ".spentCents", Math.round(price * 100.0D));
        }
        this.save();
    }

    @Override
    public synchronized Map<String, Long> getFrameStats(String frameId) {
        return this.readStats("frames." + frameId);
    }

    @Override
    public synchronized Map<String, Long> getPlayerStats(String playerId) {
        return this.readStats("players." + playerId);
    }

    @Override
    public synchronized void save() {
        this.configApi.saveConfig();
    }

    private void increment(String path, long delta) {
        long current = this.configApi.getConfig().getLong(path, 0L);
        this.configApi.getConfig().set(path, current + delta);
    }

    private Map<String, Long> readStats(String basePath) {
        Map<String, Long> values = new LinkedHashMap<String, Long>();
        values.put("purchases", this.configApi.getConfig().getLong(basePath + ".purchases", 0L));
        values.put("items", this.configApi.getConfig().getLong(basePath + ".items", 0L));
        values.put("moneyCents", this.configApi.getConfig().getLong(basePath + ".revenueCents", this.configApi.getConfig().getLong(basePath + ".spentCents", 0L)));
        return values;
    }
}
