package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.entity.Player;

import java.util.Map;

public interface StatisticsService {
    void recordPurchase(Player player, FreeFrameData frameData, int amount, double price);

    Map<String, Long> getFrameStats(String frameId);

    Map<String, Long> getPlayerStats(String playerId);

    void save();
}
