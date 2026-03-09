package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;

public interface BrandingService {
    String resolveDisplayTemplate(FreeFrameData frameData);

    String resolveDisplayTemplate(FreeFrameData frameData, String forcedThemeId);

    String resolveThemeId(FreeFrameData frameData);
}
