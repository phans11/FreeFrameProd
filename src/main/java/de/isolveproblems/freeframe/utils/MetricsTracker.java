package de.isolveproblems.freeframe.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsTracker {
    private final AtomicLong framesCreated = new AtomicLong();
    private final AtomicLong purchases = new AtomicLong();
    private final AtomicLong deniedAccess = new AtomicLong();
    private final AtomicLong cooldownHits = new AtomicLong();
    private final AtomicLong frameRateLimitHits = new AtomicLong();
    private final AtomicLong purchaseLimitHits = new AtomicLong();
    private final AtomicLong stockOutHits = new AtomicLong();
    private final AtomicLong ownerPayouts = new AtomicLong();
    private final AtomicLong migrations = new AtomicLong();
    private final AtomicLong repairs = new AtomicLong();
    private final AtomicLong adminRemovals = new AtomicLong();
    private final AtomicLong reputationBlocks = new AtomicLong();
    private final AtomicLong moderationActions = new AtomicLong();
    private final AtomicLong syncPublishes = new AtomicLong();

    public void incrementFramesCreated() {
        this.framesCreated.incrementAndGet();
    }

    public void incrementPurchases() {
        this.purchases.incrementAndGet();
    }

    public void incrementDeniedAccess() {
        this.deniedAccess.incrementAndGet();
    }

    public void incrementCooldownHits() {
        this.cooldownHits.incrementAndGet();
    }

    public void incrementFrameRateLimitHits() {
        this.frameRateLimitHits.incrementAndGet();
    }

    public void incrementPurchaseLimitHits() {
        this.purchaseLimitHits.incrementAndGet();
    }

    public void incrementStockOutHits() {
        this.stockOutHits.incrementAndGet();
    }

    public void incrementOwnerPayouts() {
        this.ownerPayouts.incrementAndGet();
    }

    public void incrementMigrations() {
        this.migrations.incrementAndGet();
    }

    public void incrementRepairs() {
        this.repairs.incrementAndGet();
    }

    public void incrementAdminRemovals() {
        this.adminRemovals.incrementAndGet();
    }

    public void incrementReputationBlocks() {
        this.reputationBlocks.incrementAndGet();
    }

    public void incrementModerationActions() {
        this.moderationActions.incrementAndGet();
    }

    public void incrementSyncPublishes() {
        this.syncPublishes.incrementAndGet();
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> values = new LinkedHashMap<String, Long>();
        values.put("framesCreated", this.framesCreated.get());
        values.put("purchases", this.purchases.get());
        values.put("deniedAccess", this.deniedAccess.get());
        values.put("cooldownHits", this.cooldownHits.get());
        values.put("frameRateLimitHits", this.frameRateLimitHits.get());
        values.put("purchaseLimitHits", this.purchaseLimitHits.get());
        values.put("stockOutHits", this.stockOutHits.get());
        values.put("ownerPayouts", this.ownerPayouts.get());
        values.put("migrations", this.migrations.get());
        values.put("repairs", this.repairs.get());
        values.put("adminRemovals", this.adminRemovals.get());
        values.put("reputationBlocks", this.reputationBlocks.get());
        values.put("moderationActions", this.moderationActions.get());
        values.put("syncPublishes", this.syncPublishes.get());
        return values;
    }
}
