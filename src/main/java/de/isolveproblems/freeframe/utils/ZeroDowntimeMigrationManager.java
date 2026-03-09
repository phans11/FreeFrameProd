package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.FreeFrame;
import de.isolveproblems.freeframe.api.MigrationPreview;
import de.isolveproblems.freeframe.api.ZeroDowntimeMigrationService;

import java.util.List;

public class ZeroDowntimeMigrationManager implements ZeroDowntimeMigrationService {
    private final FreeFrame freeframe;

    public ZeroDowntimeMigrationManager(FreeFrame freeframe) {
        this.freeframe = freeframe;
    }

    @Override
    public MigrationPreview preview() {
        List<FreeFrameData> frames = this.freeframe.getFrameRegistry().listFrames();
        int needsBranding = 0;
        int needsCampaign = 0;
        int needsNormalization = 0;

        for (FreeFrameData frameData : frames) {
            if (frameData.getBrandingId() == null || frameData.getBrandingId().trim().isEmpty()) {
                needsBranding++;
            }
            if (frameData.getCampaignId() == null || frameData.getCampaignId().trim().isEmpty()) {
                needsCampaign++;
            }
            if (frameData.getMaxStock() < 1 || frameData.getStock() < 0 || frameData.getStock() > frameData.getMaxStock()) {
                needsNormalization++;
            }
        }
        return new MigrationPreview(frames.size(), needsBranding, needsCampaign, needsNormalization);
    }

    @Override
    public MigrationPreview apply() {
        List<FreeFrameData> frames = this.freeframe.getFrameRegistry().listFrames();
        String defaultTheme = this.freeframe.getPluginConfig().getString("freeframe.branding.defaultTheme", "classic");
        String defaultCampaign = this.freeframe.getPluginConfig().getString("freeframe.campaigns.defaultRule", "");

        for (FreeFrameData frameData : frames) {
            if (frameData.getBrandingId() == null || frameData.getBrandingId().trim().isEmpty()) {
                frameData.setBrandingId(defaultTheme);
            }
            if (frameData.getCampaignId() == null || frameData.getCampaignId().trim().isEmpty()) {
                frameData.setCampaignId(defaultCampaign);
            }
            if (frameData.getMaxStock() < 1) {
                frameData.setMaxStock(1);
            }
            if (frameData.getStock() < 0) {
                frameData.setStock(0);
            }
            if (frameData.getStock() > frameData.getMaxStock()) {
                frameData.setStock(frameData.getMaxStock());
            }
        }

        this.freeframe.getFrameRegistry().saveToConfig();
        this.freeframe.getDisplayService().refreshAll(frames);
        return this.preview();
    }
}
