package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.WebhookExportService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class WebhookNotifier implements WebhookExportService {
    private final FreeFrame freeframe;
    private final Object queueLock = new Object();
    private final Deque<WebhookJob> queue = new ArrayDeque<WebhookJob>();
    private boolean workerActive;

    public WebhookNotifier(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public void sendPurchase(Player player, FreeFrameData frameData, int amount, double price, String result) {
        if (!this.isEnabled()) {
            return;
        }
        String eventId = this.eventId();
        String payload = "{"
            + "\"schema\":\"" + this.escape(this.schemaVersion()) + "\","
            + "\"eventId\":\"" + this.escape(eventId) + "\","
            + "\"type\":\"purchase\","
            + "\"player\":\"" + this.escape(player == null ? "unknown" : player.getName()) + "\","
            + "\"frame\":\"" + this.escape(frameData == null ? "unknown" : frameData.getId()) + "\","
            + "\"amount\":" + amount + ","
            + "\"price\":" + this.format(price) + ","
            + "\"result\":\"" + this.escape(result) + "\""
            + "}";
        this.enqueue(new WebhookJob(eventId, payload, 0, System.currentTimeMillis()));
    }

    @Override
    public void sendAdminAction(CommandSender sender, String action, String details) {
        if (!this.isEnabled()) {
            return;
        }
        String eventId = this.eventId();
        String payload = "{"
            + "\"schema\":\"" + this.escape(this.schemaVersion()) + "\","
            + "\"eventId\":\"" + this.escape(eventId) + "\","
            + "\"type\":\"admin\","
            + "\"sender\":\"" + this.escape(sender == null ? "unknown" : sender.getName()) + "\","
            + "\"action\":\"" + this.escape(action) + "\","
            + "\"details\":\"" + this.escape(details) + "\""
            + "}";
        this.enqueue(new WebhookJob(eventId, payload, 0, System.currentTimeMillis()));
    }

    private void enqueue(WebhookJob job) {
        synchronized (this.queueLock) {
            this.queue.addLast(job);
            if (this.workerActive) {
                return;
            }
            this.workerActive = true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.freeframe, new Runnable() {
            @Override
            public void run() {
                processQueue();
            }
        });
    }

    private void processQueue() {
        while (true) {
            WebhookJob job;
            long waitMillis = 0L;

            synchronized (this.queueLock) {
                job = this.queue.peekFirst();
                if (job == null) {
                    this.workerActive = false;
                    return;
                }
                waitMillis = job.nextAttemptAt - System.currentTimeMillis();
                if (waitMillis <= 0L) {
                    this.queue.pollFirst();
                } else {
                    job = null;
                }
            }

            if (job == null) {
                this.sleep(Math.min(waitMillis, 1000L));
                continue;
            }

            boolean delivered = this.deliver(job.eventId, job.payload);
            if (delivered) {
                continue;
            }

            int maxRetries = Math.max(0, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_MAXRETRIES));
            if (job.attempt >= maxRetries) {
                continue;
            }

            long baseDelay = Math.max(100L, this.freeframe.cfgLong(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_RETRYDELAYMILLIS));
            int nextAttempt = job.attempt + 1;
            long nextAt = System.currentTimeMillis() + (baseDelay * Math.max(1, nextAttempt));
            synchronized (this.queueLock) {
                this.queue.addLast(new WebhookJob(job.eventId, job.payload, nextAttempt, nextAt));
            }
        }
    }

    private boolean deliver(String eventId, String payload) {
        List<String> endpoints = this.resolveEndpoints();
        if (endpoints.isEmpty()) {
            return true;
        }

        boolean allSuccessful = true;
        for (String endpoint : endpoints) {
            if (!this.post(endpoint, eventId, payload)) {
                allSuccessful = false;
            }
        }
        return allSuccessful;
    }

    private boolean post(String endpoint, String eventId, String payload) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            int timeoutMillis = Math.max(100, this.freeframe.cfgInt(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_TIMEOUTMILLIS));
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("X-FreeFrame-Schema", this.schemaVersion());
            connection.setRequestProperty("X-FreeFrame-Event", eventId);
            connection.setRequestProperty("X-FreeFrame-Signature", this.sign(payload));

            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();

            int status = connection.getResponseCode();
            return status >= 200 && status < 300;
        } catch (Exception exception) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<String> resolveEndpoints() {
        List<String> endpoints = new ArrayList<String>();
        for (String endpoint : this.freeframe.cfgStringList(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_ENDPOINTS)) {
            if (endpoint != null && !endpoint.trim().isEmpty()) {
                endpoints.add(endpoint.trim());
            }
        }
        String discord = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_DISCORDURL);
        if (discord != null && !discord.trim().isEmpty()) {
            endpoints.add(discord.trim());
        }
        return endpoints;
    }

    private String sign(String payload) {
        String secret = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_SECRET);
        if (secret == null || secret.trim().isEmpty()) {
            secret = this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_SECURITY_SECRET);
        }
        if (secret == null) {
            secret = "";
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signed = mac.doFinal((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            return "sha256=" + this.hex(signed);
        } catch (Exception exception) {
            return "sha256=disabled";
        }
    }

    private String hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.ENGLISH, "%02x", value));
        }
        return builder.toString();
    }

    private boolean isEnabled() {
        return this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_ENABLED);
    }

    private String schemaVersion() {
        return this.freeframe.cfgString(FreeFrameConfigKey.FREEFRAME_WEBHOOKS_SCHEMAVERSION);
    }

    private String eventId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void sleep(long millis) {
        if (millis <= 0L) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private String format(double value) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, value));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class WebhookJob {
        private final String eventId;
        private final String payload;
        private final int attempt;
        private final long nextAttemptAt;

        private WebhookJob(String eventId, String payload, int attempt, long nextAttemptAt) {
            this.eventId = eventId;
            this.payload = payload;
            this.attempt = attempt;
            this.nextAttemptAt = nextAttemptAt;
        }
    }
}
