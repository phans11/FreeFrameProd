package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface WebhookExportService {
    void sendPurchase(Player player, FreeFrameData frameData, int amount, double price, String result);

    void sendAdminAction(CommandSender sender, String action, String details);
}
