package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.ConfigAPI;
import de.isolveproblems.freeframe.api.PurchaseProfile;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

public class PurchaseSecurityService {
    private final FreeFrame freeframe;
    private final ConfigAPI idempotencyConfig;
    private final String secret;

    public PurchaseSecurityService(FreeFrame freeframe) {
        this.freeframe = freeframe;
        this.idempotencyConfig = new ConfigAPI(freeframe.getDataFolder(), "idempotency.yml");
        this.secret = this.resolveSecret();
    }

    public SignedPurchaseToken createToken(Player player, FreeFrameData frameData, PurchaseProfile profile, double price) {
        long bucketMillis = Math.max(250L, this.freeframe.getPluginConfig().getLong("freeframe.security.idempotencyBucketMillis", 1500L));
        long bucket = System.currentTimeMillis() / bucketMillis;

        String playerPart = player == null ? "unknown" : player.getUniqueId().toString();
        String framePart = frameData == null ? "unknown" : frameData.getId();
        String profilePart = profile == null ? "0:0" : (profile.getSlot() + ":" + profile.getAmount());
        String pricePart = String.valueOf(Math.round(Math.max(0.0D, price) * 100.0D));
        String fingerprint = playerPart + "|" + framePart + "|" + profilePart + "|" + pricePart + "|" + bucket;

        String txId = this.sha256("tx|" + fingerprint).substring(0, 24).toLowerCase(Locale.ENGLISH);
        String signature = this.sha256(this.secret + "|" + txId + "|" + fingerprint);
        return new SignedPurchaseToken(txId, signature, fingerprint);
    }

    public boolean verify(SignedPurchaseToken token) {
        if (token == null || token.getTxId() == null || token.getSignature() == null || token.getFingerprint() == null) {
            return false;
        }
        String expected = this.sha256(this.secret + "|" + token.getTxId() + "|" + token.getFingerprint());
        return expected.equals(token.getSignature());
    }

    public synchronized boolean isCommitted(String txId) {
        if (txId == null || txId.trim().isEmpty()) {
            return false;
        }
        return this.idempotencyConfig.getConfig().contains("committed." + txId);
    }

    public synchronized void markCommitted(String txId) {
        if (txId == null || txId.trim().isEmpty()) {
            return;
        }
        this.idempotencyConfig.getConfig().set("committed." + txId, System.currentTimeMillis());
        this.idempotencyConfig.saveConfig();
    }

    private String resolveSecret() {
        String configured = this.freeframe.getPluginConfig().getString("freeframe.security.secret", "");
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        String generated = UUID.randomUUID().toString().replace("-", "");
        this.freeframe.getPluginConfig().set("freeframe.security.secret", generated);
        this.freeframe.getConfigHandler().getConfigApi().saveConfig();
        return generated;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format(Locale.ENGLISH, "%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }
}
