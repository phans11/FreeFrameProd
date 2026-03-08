package de.isolveproblems.freeframe.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsTrackerTest {

    @Test
    void snapshotShouldContainUpdatedCounters() {
        MetricsTracker metrics = new MetricsTracker();

        metrics.incrementFramesCreated();
        metrics.incrementFramesCreated();
        metrics.incrementPurchases();
        metrics.incrementDeniedAccess();
        metrics.incrementCooldownHits();
        metrics.incrementFrameRateLimitHits();
        metrics.incrementMigrations();
        metrics.incrementRepairs();
        metrics.incrementAdminRemovals();

        Map<String, Long> snapshot = metrics.snapshot();

        assertEquals(2L, snapshot.get("framesCreated").longValue());
        assertEquals(1L, snapshot.get("purchases").longValue());
        assertEquals(1L, snapshot.get("deniedAccess").longValue());
        assertEquals(1L, snapshot.get("cooldownHits").longValue());
        assertEquals(1L, snapshot.get("frameRateLimitHits").longValue());
        assertEquals(1L, snapshot.get("migrations").longValue());
        assertEquals(1L, snapshot.get("repairs").longValue());
        assertEquals(1L, snapshot.get("adminRemovals").longValue());
    }
}
