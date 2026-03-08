package de.isolveproblems.freeframe.utils;

public class FrameRepairReport {
    private final int removedInvalidFrames;
    private final int removedDuplicates;
    private final int normalizedFrames;

    public FrameRepairReport(int removedInvalidFrames, int removedDuplicates, int normalizedFrames) {
        this.removedInvalidFrames = removedInvalidFrames;
        this.removedDuplicates = removedDuplicates;
        this.normalizedFrames = normalizedFrames;
    }

    public int getRemovedInvalidFrames() {
        return this.removedInvalidFrames;
    }

    public int getRemovedDuplicates() {
        return this.removedDuplicates;
    }

    public int getNormalizedFrames() {
        return this.normalizedFrames;
    }
}
