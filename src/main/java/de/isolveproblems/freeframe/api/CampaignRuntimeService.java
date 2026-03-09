package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;

public interface CampaignRuntimeService {
    CampaignEffect resolve(FreeFrameData frameData, long nowEpochMillis);

    double applyPrice(FreeFrameData frameData, double basePrice, long nowEpochMillis);
}
