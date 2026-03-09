package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.NetworkSyncService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CrossServerSyncBridge implements NetworkSyncService, PluginMessageListener {
    private final FreeFrame freeframe;
    private final Map<String, Long> recentEventIds = new HashMap<String, Long>();
    private File syncFile;
    private long filePointer;
    private int fileTaskId = -1;
    private String runtimeMode = "none";
    private String bridgeChannel = "freeframe-sync";

    public CrossServerSyncBridge(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public synchronized void start() {
        this.stop();

        if (!this.freeframe.getPluginConfig().getBoolean("freeframe.networkSync.enabled", false)) {
            this.runtimeMode = "none";
            return;
        }

        this.runtimeMode = this.freeframe.getPluginConfig().getString("freeframe.networkSync.mode", "none").trim().toLowerCase(Locale.ENGLISH);
        this.bridgeChannel = this.freeframe.getPluginConfig().getString("freeframe.networkSync.bridgeChannel", "freeframe-sync");
        if (this.bridgeChannel == null || this.bridgeChannel.trim().isEmpty()) {
            this.bridgeChannel = "freeframe-sync";
        }

        if ("bungee".equals(this.runtimeMode)) {
            this.registerBungeeChannels();
        } else if ("velocity".equals(this.runtimeMode)) {
            this.registerVelocityChannels();
        } else if ("hybrid".equals(this.runtimeMode)) {
            this.registerBungeeChannels();
            this.registerFileBridge();
        } else if ("file".equals(this.runtimeMode)) {
            this.registerFileBridge();
        }
    }

    @Override
    public synchronized void stop() {
        try {
            this.freeframe.getServer().getMessenger().unregisterIncomingPluginChannel(this.freeframe);
            this.freeframe.getServer().getMessenger().unregisterOutgoingPluginChannel(this.freeframe);
        } catch (Exception ignored) {
            // Plugin messaging can fail on unusual server forks.
        }

        if (this.fileTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.fileTaskId);
            this.fileTaskId = -1;
        }
        this.filePointer = 0L;
        this.syncFile = null;
        this.runtimeMode = "none";
    }

    @Override
    public synchronized void publishFrameUpdate(FreeFrameData frameData, String reason) {
        if (frameData == null || !this.freeframe.getPluginConfig().getBoolean("freeframe.networkSync.enabled", false)) {
            return;
        }
        String eventId = UUID.randomUUID().toString().replace("-", "");
        String payload = this.encodePayload(eventId, frameData, reason);
        this.rememberEvent(eventId);
        this.freeframe.getMetricsTracker().incrementSyncPublishes();

        if ("bungee".equals(this.runtimeMode) || "hybrid".equals(this.runtimeMode)) {
            this.sendViaBungee(payload);
        }
        if ("velocity".equals(this.runtimeMode)) {
            this.sendViaVelocity(payload);
        }
        if ("file".equals(this.runtimeMode) || "hybrid".equals(this.runtimeMode)) {
            this.appendToFile(payload);
        }
    }

    @Override
    public synchronized String describeMode() {
        return this.runtimeMode;
    }

    @Override
    public synchronized void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel == null || message == null || message.length == 0) {
            return;
        }

        try {
            if ("BungeeCord".equalsIgnoreCase(channel)) {
                DataInputStream input = new DataInputStream(new ByteArrayInputStream(message));
                String subchannel = input.readUTF();
                if (!this.bridgeChannel.equalsIgnoreCase(subchannel)) {
                    return;
                }
                short length = input.readShort();
                byte[] data = new byte[Math.max(0, length)];
                input.readFully(data);
                this.applyPayload(new String(data, StandardCharsets.UTF_8));
                return;
            }

            String configuredVelocityChannel = this.resolveVelocityChannel();
            if (configuredVelocityChannel.equalsIgnoreCase(channel)) {
                this.applyPayload(new String(message, StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
            // Ignore malformed bridge messages.
        }
    }

    private void registerBungeeChannels() {
        try {
            this.freeframe.getServer().getMessenger().registerOutgoingPluginChannel(this.freeframe, "BungeeCord");
            this.freeframe.getServer().getMessenger().registerIncomingPluginChannel(this.freeframe, "BungeeCord", this);
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Could not register BungeeCord sync channel: " + exception.getMessage());
        }
    }

    private void registerVelocityChannels() {
        String channel = this.resolveVelocityChannel();
        try {
            this.freeframe.getServer().getMessenger().registerOutgoingPluginChannel(this.freeframe, channel);
            this.freeframe.getServer().getMessenger().registerIncomingPluginChannel(this.freeframe, channel, this);
        } catch (Exception exception) {
            this.freeframe.getLogger().warning("Could not register Velocity sync channel '" + channel + "': " + exception.getMessage());
        }
    }

    private void registerFileBridge() {
        File folder = new File(this.freeframe.getDataFolder(), "network-sync");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.syncFile = new File(folder, "events.log");
        if (!this.syncFile.exists()) {
            this.appendToFile("");
            this.syncFile.delete();
            this.syncFile = new File(folder, "events.log");
        }
        this.filePointer = 0L;

        long periodTicks = Math.max(20L, this.freeframe.getPluginConfig().getLong("freeframe.networkSync.filePollTicks", 100L));
        this.fileTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.freeframe, new Runnable() {
            @Override
            public void run() {
                pollFile();
            }
        }, periodTicks, periodTicks);
    }

    private void sendViaBungee(String payload) {
        Player player = this.pickPlayerTransport();
        if (player == null) {
            return;
        }

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF(this.bridgeChannel);
            byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
            out.writeShort(payloadBytes.length);
            out.write(payloadBytes);
            player.sendPluginMessage(this.freeframe, "BungeeCord", stream.toByteArray());
        } catch (Exception ignored) {
            // No-op: proxy bridge is optional.
        }
    }

    private void sendViaVelocity(String payload) {
        Player player = this.pickPlayerTransport();
        if (player == null) {
            return;
        }
        try {
            player.sendPluginMessage(this.freeframe, this.resolveVelocityChannel(), payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // No-op: proxy bridge is optional.
        }
    }

    private void applyPayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return;
        }
        String[] parts = payload.split("\\|", -1);
        if (parts.length < 8) {
            return;
        }

        String eventId = parts[0];
        if (this.isRecentEvent(eventId)) {
            return;
        }
        this.rememberEvent(eventId);

        FreeFrameData frameData = this.freeframe.getFrameRegistry().findById(parts[1]);
        if (frameData == null) {
            return;
        }

        try {
            int stock = Integer.parseInt(parts[2]);
            int maxStock = Integer.parseInt(parts[3]);
            double price = Double.parseDouble(parts[4]);
            double revenue = Double.parseDouble(parts[5]);

            if (this.freeframe.getPluginConfig().getBoolean("freeframe.networkSync.applyStock", true)) {
                frameData.setMaxStock(maxStock);
                frameData.setStock(stock);
            }
            if (this.freeframe.getPluginConfig().getBoolean("freeframe.networkSync.applyPrice", false)) {
                frameData.setPrice(price);
            }
            if (this.freeframe.getPluginConfig().getBoolean("freeframe.networkSync.applyRevenue", false)) {
                frameData.setRevenueTotal(Math.max(frameData.getRevenueTotal(), revenue));
            }
            this.freeframe.getDisplayService().refresh(frameData);
            this.freeframe.getFrameRegistry().saveToConfig();
        } catch (NumberFormatException ignored) {
            // ignore malformed numeric values
        }
    }

    private String encodePayload(String eventId, FreeFrameData frameData, String reason) {
        String normalizedReason = reason == null ? "" : reason.replace("|", "_");
        return eventId
            + "|" + frameData.getId()
            + "|" + frameData.getStock()
            + "|" + frameData.getMaxStock()
            + "|" + format(frameData.getPrice())
            + "|" + format(frameData.getRevenueTotal())
            + "|" + System.currentTimeMillis()
            + "|" + normalizedReason;
    }

    private void appendToFile(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return;
        }
        try {
            if (this.syncFile == null) {
                File folder = new File(this.freeframe.getDataFolder(), "network-sync");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                this.syncFile = new File(folder, "events.log");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.syncFile, true))) {
                writer.write(payload);
                writer.newLine();
            }
        } catch (Exception ignored) {
            // Optional bridge; failures are non-fatal.
        }
    }

    private void pollFile() {
        if (this.syncFile == null || !this.syncFile.isFile()) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(this.syncFile, "r")) {
            raf.seek(this.filePointer);
            String line;
            while ((line = raf.readLine()) != null) {
                this.applyPayload(new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            }
            this.filePointer = raf.getFilePointer();
        } catch (Exception ignored) {
            // Optional bridge; failures are non-fatal.
        }
    }

    private Player pickPlayerTransport() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                return player;
            }
        }
        return null;
    }

    private String resolveVelocityChannel() {
        String configured = this.freeframe.getPluginConfig().getString("freeframe.proxy.velocity.channel", "freeframe:sync");
        if (configured == null || configured.trim().isEmpty()) {
            return "freeframe:sync";
        }
        return configured.trim().toLowerCase(Locale.ENGLISH);
    }

    private void rememberEvent(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        this.recentEventIds.put(eventId, Long.valueOf(now));
        this.pruneEvents(now);
    }

    private boolean isRecentEvent(String eventId) {
        Long seenAt = this.recentEventIds.get(eventId);
        if (seenAt == null) {
            return false;
        }
        long ttl = Math.max(10000L, this.freeframe.getPluginConfig().getLong("freeframe.networkSync.eventTtlMillis", 180000L));
        return System.currentTimeMillis() - seenAt.longValue() <= ttl;
    }

    private void pruneEvents(long now) {
        long ttl = Math.max(10000L, this.freeframe.getPluginConfig().getLong("freeframe.networkSync.eventTtlMillis", 180000L));
        for (Map.Entry<String, Long> entry : new HashMap<String, Long>(this.recentEventIds).entrySet()) {
            if (now - entry.getValue().longValue() > ttl) {
                this.recentEventIds.remove(entry.getKey());
            }
        }
    }

    private static String format(double value) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, value));
    }
}
