package de.isolveproblems.freeframe.api;

public class MigrationPreview {
    private final int scannedFrames;
    private final int needsBranding;
    private final int needsCampaign;
    private final int needsNormalization;

    public MigrationPreview(int scannedFrames, int needsBranding, int needsCampaign, int needsNormalization) {
        this.scannedFrames = Math.max(0, scannedFrames);
        this.needsBranding = Math.max(0, needsBranding);
        this.needsCampaign = Math.max(0, needsCampaign);
        this.needsNormalization = Math.max(0, needsNormalization);
    }

    public int getScannedFrames() {
        return this.scannedFrames;
    }

    public int getNeedsBranding() {
        return this.needsBranding;
    }

    public int getNeedsCampaign() {
        return this.needsCampaign;
    }

    public int getNeedsNormalization() {
        return this.needsNormalization;
    }
}
