package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.entity.Player;

public interface DiscountService {
    double applyDiscount(Player player, FreeFrameData frameData, double basePrice);
}
