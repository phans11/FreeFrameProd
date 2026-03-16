package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.config.FreeFrameConfigKey;
import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.BuyerReputationService;
import de.isolveproblems.freeframe.api.BuyerRiskProfile;
import de.isolveproblems.freeframe.api.ConfigAPI;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BuyerReputationManager implements BuyerReputationService {
    private final FreeFrame freeframe;
    private final ConfigAPI configApi;

    public BuyerReputationManager(FreeFrame freeframe) {
        this.freeframe = freeframe;
        this.configApi = new ConfigAPI(freeframe.getDataFolder(), "reputation.yml");
    }

    @Override
    public synchronized BuyerRiskProfile evaluate(Player player, FreeFrameData frameData, double expectedGrossPrice) {
        if (player == null) {
            return new BuyerRiskProfile("unknown", 0.0D, false, "");
        }

        String playerId = player.getUniqueId().toString();
        double score = this.computeScore(playerId, Math.max(0.0D, expectedGrossPrice));
        double threshold = Math.max(0.0D, this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_BLOCKTHRESHOLD));
        boolean enabled = this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_REPUTATION_ENABLED);
        boolean blocked = enabled && score >= threshold;
        String reason = blocked
            ? "risk-score=" + String.format(Locale.ENGLISH, "%.2f", score) + " threshold=" + String.format(Locale.ENGLISH, "%.2f", threshold)
            : "";
        return new BuyerRiskProfile(playerId, score, blocked, reason);
    }

    @Override
    public synchronized void recordPurchaseSuccess(Player player, FreeFrameData frameData, double grossPrice) {
        if (player == null) {
            return;
        }

        String playerId = player.getUniqueId().toString();
        String base = this.playerBasePath(playerId);
        this.increment(base + ".success", 1L);
        this.increment(base + ".spentCents", Math.round(Math.max(0.0D, grossPrice) * 100.0D));
        this.appendEvent(playerId, "success", "gross=" + this.format(grossPrice));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized void recordPurchaseFailure(Player player, FreeFrameData frameData, String reason) {
        if (player == null) {
            return;
        }

        String playerId = player.getUniqueId().toString();
        String base = this.playerBasePath(playerId);
        this.increment(base + ".failure", 1L);
        String safeReason = reason == null || reason.trim().isEmpty() ? "unknown" : reason.trim().toLowerCase(Locale.ENGLISH);
        this.increment(base + ".reasons." + safeReason.replaceAll("[^a-z0-9_-]", "_"), 1L);
        this.appendEvent(playerId, "failure", safeReason);
        this.configApi.saveConfig();
    }

    @Override
    public synchronized void setManualRiskScore(String playerId, double score, String actor, String reason) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return;
        }
        this.configApi.getConfig().set(this.playerBasePath(playerId) + ".manualScore", Math.max(0.0D, score));
        this.appendEvent(playerId, "manual-set", "actor=" + safe(actor) + " reason=" + safe(reason) + " score=" + this.format(score));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized void clearManualRiskScore(String playerId, String actor) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return;
        }
        this.configApi.getConfig().set(this.playerBasePath(playerId) + ".manualScore", 0.0D);
        this.appendEvent(playerId, "manual-clear", "actor=" + safe(actor));
        this.configApi.saveConfig();
    }

    @Override
    public synchronized BuyerRiskProfile inspect(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return new BuyerRiskProfile("unknown", 0.0D, false, "");
        }
        double score = this.computeScore(playerId, 0.0D);
        double threshold = Math.max(0.0D, this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_BLOCKTHRESHOLD));
        boolean blocked = this.freeframe.cfgBoolean(FreeFrameConfigKey.FREEFRAME_REPUTATION_ENABLED) && score >= threshold;
        return new BuyerRiskProfile(playerId, score, blocked, blocked ? "threshold reached" : "");
    }

    private double computeScore(String playerId, double expectedGrossPrice) {
        String base = this.playerBasePath(playerId);
        double manual = Math.max(0.0D, this.configApi.getConfig().getDouble(base + ".manualScore", 0.0D));
        long success = Math.max(0L, this.configApi.getConfig().getLong(base + ".success", 0L));
        long failure = Math.max(0L, this.configApi.getConfig().getLong(base + ".failure", 0L));
        long duplicate = Math.max(0L, this.configApi.getConfig().getLong(base + ".reasons.duplicate", 0L));
        long invalidSig = Math.max(0L, this.configApi.getConfig().getLong(base + ".reasons.invalid-signature", 0L));

        double failureWeight = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_WEIGHTS_FAILURE);
        double duplicateWeight = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_WEIGHTS_DUPLICATE);
        double invalidSigWeight = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_WEIGHTS_INVALIDSIGNATURE);
        double successDecay = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_WEIGHTS_SUCCESSDECAY);
        double highValueWeight = this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_WEIGHTS_HIGHVALUEPURCHASE);
        double highValueThreshold = Math.max(0.0D, this.freeframe.cfgDouble(FreeFrameConfigKey.FREEFRAME_REPUTATION_HIGHVALUETHRESHOLD));

        double score = manual;
        score += failure * Math.max(0.0D, failureWeight);
        score += duplicate * Math.max(0.0D, duplicateWeight);
        score += invalidSig * Math.max(0.0D, invalidSigWeight);
        score -= success * Math.max(0.0D, successDecay);
        if (expectedGrossPrice >= highValueThreshold) {
            score += Math.max(0.0D, highValueWeight);
        }
        return Math.max(0.0D, score);
    }

    private void appendEvent(String playerId, String type, String details) {
        String base = "history." + playerId + "." + System.currentTimeMillis();
        this.configApi.getConfig().set(base + ".at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date()));
        this.configApi.getConfig().set(base + ".type", safe(type));
        this.configApi.getConfig().set(base + ".details", safe(details));
    }

    private void increment(String path, long delta) {
        long current = this.configApi.getConfig().getLong(path, 0L);
        this.configApi.getConfig().set(path, current + delta);
    }

    private String playerBasePath(String playerId) {
        return "players." + playerId;
    }

    private static String format(double value) {
        return String.format(Locale.ENGLISH, "%.2f", Math.max(0.0D, value));
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\n", " ").replace("\r", " ");
    }
}
