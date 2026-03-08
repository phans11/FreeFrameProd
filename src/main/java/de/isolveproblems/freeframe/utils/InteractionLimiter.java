package de.isolveproblems.freeframe.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionLimiter {
    public enum LimitResult {
        ALLOWED,
        PLAYER_COOLDOWN,
        FRAME_RATE_LIMIT
    }

    private final Map<UUID, Long> lastPlayerInteraction = new HashMap<UUID, Long>();
    private final Map<String, Long> lastFrameInteraction = new HashMap<String, Long>();

    public synchronized LimitResult checkAndMark(UUID playerId, String frameId, long playerCooldownMillis, long frameCooldownMillis) {
        long now = System.currentTimeMillis();

        if (playerId != null && playerCooldownMillis > 0L) {
            Long previous = this.lastPlayerInteraction.get(playerId);
            if (previous != null && now - previous < playerCooldownMillis) {
                return LimitResult.PLAYER_COOLDOWN;
            }
        }

        if (frameId != null && frameCooldownMillis > 0L) {
            Long previous = this.lastFrameInteraction.get(frameId);
            if (previous != null && now - previous < frameCooldownMillis) {
                return LimitResult.FRAME_RATE_LIMIT;
            }
        }

        if (playerId != null) {
            this.lastPlayerInteraction.put(playerId, now);
        }
        if (frameId != null) {
            this.lastFrameInteraction.put(frameId, now);
        }

        this.cleanup(now, playerCooldownMillis, frameCooldownMillis);
        return LimitResult.ALLOWED;
    }

    private void cleanup(long now, long playerCooldownMillis, long frameCooldownMillis) {
        long playerRetention = Math.max(1000L, playerCooldownMillis * 4L);
        long frameRetention = Math.max(1000L, frameCooldownMillis * 4L);

        this.lastPlayerInteraction.entrySet().removeIf(entry -> now - entry.getValue() > playerRetention);
        this.lastFrameInteraction.entrySet().removeIf(entry -> now - entry.getValue() > frameRetention);
    }
}
