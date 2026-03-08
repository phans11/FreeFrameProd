package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseWindowLimiterTest {

    @Test
    void shouldDenyWhenWindowLimitExceeded() {
        PurchaseWindowLimiter limiter = new PurchaseWindowLimiter();
        UUID playerId = UUID.randomUUID();

        PurchaseWindowLimiter.LimitState first = limiter.checkAndConsume(playerId, 32, 64, 60000L);
        PurchaseWindowLimiter.LimitState second = limiter.checkAndConsume(playerId, 16, 64, 60000L);
        PurchaseWindowLimiter.LimitState third = limiter.checkAndConsume(playerId, 24, 64, 60000L);

        assertTrue(first.isAllowed());
        assertTrue(second.isAllowed());
        assertFalse(third.isAllowed());
        assertEquals(16, third.getRemaining());
    }

    @Test
    void shouldAllowWhenLimitDisabledByValues() {
        PurchaseWindowLimiter limiter = new PurchaseWindowLimiter();
        PurchaseWindowLimiter.LimitState state = limiter.checkAndConsume(UUID.randomUUID(), 64, 0, 0L);

        assertTrue(state.isAllowed());
    }
}
