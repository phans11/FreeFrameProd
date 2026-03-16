package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.util.HashMap;
import java.util.Map;

public class AlertService {
    private final FreeFrame freeframe;
    private final Map<String, Long> cooldownByKey = new HashMap<String, Long>();

    public AlertService(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    public void alertLowStock(FreeFrameData frameData) {
        if (frameData == null) {
            return;
        }
        int threshold = Math.max(0, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_ALERTS_LOWSTOCKTHRESHOLD));
        if (frameData.getStock() > threshold) {
            return;
        }
        this.alert("low-stock:" + frameData.getId(), "Low stock on frame " + frameData.getId() + ": " + frameData.getStock());
    }

    public void alertPurchaseFailure(String frameId, String reason) {
        this.alert("purchase-fail:" + frameId, "Purchase failure on frame " + frameId + ": " + reason);
    }

    public void alertAuctionIssue(String frameId, String reason) {
        this.alert("auction:" + frameId, "Auction issue on frame " + frameId + ": " + reason);
    }

    private void alert(String key, String message) {
        if (!this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_ALERTS_ENABLED)) {
            return;
        }

        long now = System.currentTimeMillis();
        long cooldown = Math.max(1000L, this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_ALERTS_COOLDOWNMILLIS));
        Long last = this.cooldownByKey.get(key);
        if (last != null && now - last.longValue() < cooldown) {
            return;
        }
        this.cooldownByKey.put(key, Long.valueOf(now));

        this.freeframe.getLogger().warning("[Alert] " + message);
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        this.freeframe.getWebhookExportService().sendAdminAction(console, "alert", message);
        if (this.freeframe.getTransactionJournalService() != null) {
            this.freeframe.getTransactionJournalService().logAlert(key, message);
        }
    }
}
