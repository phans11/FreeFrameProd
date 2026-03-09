package de.isolveproblems.freeframe.utils;

public class JournalReplayReport {
    private final int totalEntries;
    private final int purchaseCommits;
    private final int duplicateCommits;
    private final int rebuiltIdempotencyKeys;

    public JournalReplayReport(int totalEntries, int purchaseCommits, int duplicateCommits, int rebuiltIdempotencyKeys) {
        this.totalEntries = totalEntries;
        this.purchaseCommits = purchaseCommits;
        this.duplicateCommits = duplicateCommits;
        this.rebuiltIdempotencyKeys = rebuiltIdempotencyKeys;
    }

    public int getTotalEntries() {
        return this.totalEntries;
    }

    public int getPurchaseCommits() {
        return this.purchaseCommits;
    }

    public int getDuplicateCommits() {
        return this.duplicateCommits;
    }

    public int getRebuiltIdempotencyKeys() {
        return this.rebuiltIdempotencyKeys;
    }
}
