package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InteractionLimiterTest {

    @Test
    void shouldApplyPlayerCooldown() {
        InteractionLimiter limiter = new InteractionLimiter();
        UUID playerId = UUID.randomUUID();

        assertEquals(
            InteractionLimiter.LimitResult.ALLOWED,
            limiter.checkAndMark(playerId, "frame-a", 1000L, 0L)
        );

        assertEquals(
            InteractionLimiter.LimitResult.PLAYER_COOLDOWN,
            limiter.checkAndMark(playerId, "frame-b", 1000L, 0L)
        );
    }

    @Test
    void shouldApplyFrameRateLimit() {
        InteractionLimiter limiter = new InteractionLimiter();

        assertEquals(
            InteractionLimiter.LimitResult.ALLOWED,
            limiter.checkAndMark(UUID.randomUUID(), "frame-a", 0L, 1000L)
        );

        assertEquals(
            InteractionLimiter.LimitResult.FRAME_RATE_LIMIT,
            limiter.checkAndMark(UUID.randomUUID(), "frame-a", 0L, 1000L)
        );
    }

    @Test
    void shouldAllowWhenCooldownsDisabled() {
        InteractionLimiter limiter = new InteractionLimiter();

        assertEquals(
            InteractionLimiter.LimitResult.ALLOWED,
            limiter.checkAndMark(UUID.randomUUID(), "frame-a", 0L, 0L)
        );

        assertEquals(
            InteractionLimiter.LimitResult.ALLOWED,
            limiter.checkAndMark(UUID.randomUUID(), "frame-a", 0L, 0L)
        );
    }
}
