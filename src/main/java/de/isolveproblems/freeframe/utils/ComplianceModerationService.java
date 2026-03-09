package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import de.isolveproblems.freeframe.api.ModerationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplianceModerationService implements ModerationService {
    private final ConfigAPI configApi;

    public ComplianceModerationService(FreeFrame freeframe) {
        this.configApi = new ConfigAPI(freeframe.getDataFolder(), "moderation.yml");
    }

    @Override
    public synchronized boolean isFrameFrozen(String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return false;
        }
        return this.configApi.getConfig().getBoolean("frames." + frameId.toLowerCase(Locale.ENGLISH) + ".frozen", false);
    }

    @Override
    public synchronized boolean isPlayerRestricted(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return false;
        }
        return this.configApi.getConfig().getBoolean("players." + playerId + ".restricted", false);
    }

    @Override
    public synchronized String frameRestrictionReason(String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return "";
        }
        return this.configApi.getConfig().getString("frames." + frameId.toLowerCase(Locale.ENGLISH) + ".reason", "");
    }

    @Override
    public synchronized String playerRestrictionReason(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return "";
        }
        return this.configApi.getConfig().getString("players." + playerId + ".reason", "");
    }

    @Override
    public synchronized void freezeFrame(FreeFrameData frameData, String actor, String reason) {
        if (frameData == null) {
            return;
        }
        String frameId = frameData.getId().toLowerCase(Locale.ENGLISH);
        this.configApi.getConfig().set("frames." + frameId + ".frozen", true);
        this.configApi.getConfig().set("frames." + frameId + ".reason", safe(reason));
        this.configApi.getConfig().set("frames." + frameId + ".actor", safe(actor));
        this.configApi.getConfig().set("frames." + frameId + ".at", now());
        this.appendLog("freeze-frame", "frame=" + frameId + " actor=" + safe(actor) + " reason=" + safe(reason));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized void unfreezeFrame(String frameId, String actor) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return;
        }
        String normalized = frameId.toLowerCase(Locale.ENGLISH);
        this.configApi.getConfig().set("frames." + normalized + ".frozen", false);
        this.configApi.getConfig().set("frames." + normalized + ".reason", "");
        this.configApi.getConfig().set("frames." + normalized + ".actor", safe(actor));
        this.configApi.getConfig().set("frames." + normalized + ".at", now());
        this.appendLog("unfreeze-frame", "frame=" + normalized + " actor=" + safe(actor));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized void restrictPlayer(String playerId, String actor, String reason) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return;
        }
        this.configApi.getConfig().set("players." + playerId + ".restricted", true);
        this.configApi.getConfig().set("players." + playerId + ".reason", safe(reason));
        this.configApi.getConfig().set("players." + playerId + ".actor", safe(actor));
        this.configApi.getConfig().set("players." + playerId + ".at", now());
        this.appendLog("restrict-player", "player=" + playerId + " actor=" + safe(actor) + " reason=" + safe(reason));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized void unrestrictPlayer(String playerId, String actor) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return;
        }
        this.configApi.getConfig().set("players." + playerId + ".restricted", false);
        this.configApi.getConfig().set("players." + playerId + ".reason", "");
        this.configApi.getConfig().set("players." + playerId + ".actor", safe(actor));
        this.configApi.getConfig().set("players." + playerId + ".at", now());
        this.appendLog("unrestrict-player", "player=" + playerId + " actor=" + safe(actor));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized List<String> tailLog(int limit) {
        int safeLimit = Math.max(1, limit);
        List<String> keys = new ArrayList<String>();
        if (this.configApi.getConfig().getConfigurationSection("log") != null) {
            keys.addAll(this.configApi.getConfig().getConfigurationSection("log").getKeys(false));
        }
        Collections.sort(keys, Collections.reverseOrder());

        List<String> lines = new ArrayList<String>();
        for (String key : keys) {
            if (lines.size() >= safeLimit) {
                break;
            }
            String line = this.configApi.getConfig().getString("log." + key, "");
            if (line != null && !line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void appendLog(String type, String details) {
        String key = String.valueOf(System.currentTimeMillis()) + "-" + ((int) (Math.random() * 9999.0D));
        this.configApi.getConfig().set("log." + key, now() + " " + safe(type) + " " + safe(details));
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\n", " ").replace("\r", " ");
    }
}
