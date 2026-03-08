package de.isolveproblems.freeframe.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PurchaseWindowLimiter {

    public static class LimitState {
        private final boolean allowed;
        private final int remaining;
        private final long waitMillis;

        public LimitState(boolean allowed, int remaining, long waitMillis) {
            this.allowed = allowed;
            this.remaining = remaining;
            this.waitMillis = waitMillis;
        }

        public boolean isAllowed() {
            return this.allowed;
        }

        public int getRemaining() {
            return this.remaining;
        }

        public long getWaitMillis() {
            return this.waitMillis;
        }
    }

    private static class WindowEntry {
        private long startedAt;
        private int consumedItems;

        private WindowEntry(long startedAt, int consumedItems) {
            this.startedAt = startedAt;
            this.consumedItems = consumedItems;
        }
    }

    private final Map<UUID, WindowEntry> entries = new HashMap<UUID, WindowEntry>();

    public synchronized LimitState checkAndConsume(UUID playerId, int itemAmount, int maxItems, long windowMillis) {
        if (playerId == null || itemAmount <= 0 || maxItems <= 0 || windowMillis <= 0L) {
            return new LimitState(true, Integer.MAX_VALUE, 0L);
        }

        long now = System.currentTimeMillis();
        WindowEntry entry = this.entries.get(playerId);
        if (entry == null || now - entry.startedAt >= windowMillis) {
            entry = new WindowEntry(now, 0);
            this.entries.put(playerId, entry);
        }

        if (entry.consumedItems + itemAmount > maxItems) {
            long wait = Math.max(0L, windowMillis - (now - entry.startedAt));
            int remaining = Math.max(0, maxItems - entry.consumedItems);
            return new LimitState(false, remaining, wait);
        }

        entry.consumedItems += itemAmount;
        int remaining = Math.max(0, maxItems - entry.consumedItems);
        this.cleanup(now, windowMillis);
        return new LimitState(true, remaining, 0L);
    }

    private void cleanup(long now, long windowMillis) {
        long retention = Math.max(windowMillis * 2L, 60_000L);
        this.entries.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= retention);
    }
}
