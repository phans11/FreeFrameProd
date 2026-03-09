package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.entity.Player;

public interface BuyerReputationService {
    BuyerRiskProfile evaluate(Player player, FreeFrameData frameData, double expectedGrossPrice);

    void recordPurchaseSuccess(Player player, FreeFrameData frameData, double grossPrice);

    void recordPurchaseFailure(Player player, FreeFrameData frameData, String reason);

    void setManualRiskScore(String playerId, double score, String actor, String reason);

    void clearManualRiskScore(String playerId, String actor);

    BuyerRiskProfile inspect(String playerId);
}
