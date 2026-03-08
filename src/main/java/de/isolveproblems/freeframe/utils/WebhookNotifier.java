package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.WebhookExportService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookNotifier implements WebhookExportService {
    private final FreeFrame freeframe;

    public WebhookNotifier(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public void sendPurchase(Player player, FreeFrameData frameData, int amount, double price, String result) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.webhooks.enabled", false)) {
            return;
        }
        String payload = "{\"type\":\"purchase\",\"player\":\"" + this.escape(player == null ? "unknown" : player.getName())
            + "\",\"frame\":\"" + this.escape(frameData == null ? "unknown" : frameData.getId())
            + "\",\"amount\":" + amount
            + ",\"price\":" + price
            + ",\"result\":\"" + this.escape(result) + "\"}";
        this.post(payload);
    }

    @Override
    public void sendAdminAction(CommandSender sender, String action, String details) {
        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.webhooks.enabled", false)) {
            return;
        }
        String payload = "{\"type\":\"admin\",\"sender\":\"" + this.escape(sender == null ? "unknown" : sender.getName())
            + "\",\"action\":\"" + this.escape(action)
            + "\",\"details\":\"" + this.escape(details) + "\"}";
        this.post(payload);
    }

    private void post(String payload) {
        String url = this.freeframe.getPluginConfig().getString("freeframe.webhooks.discordUrl", "");
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            connection.getResponseCode();
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Webhook post failed: " + exception.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
