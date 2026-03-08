package de.isolveproblems.freeframe.api;

import org.bukkit.entity.Player;

public interface LocalizationService {
    String resolveMessage(Player player, String path, String fallback);
}
