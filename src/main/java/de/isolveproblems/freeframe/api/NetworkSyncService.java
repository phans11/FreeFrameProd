package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;

public interface NetworkSyncService {
    void start();

    void stop();

    void publishFrameUpdate(FreeFrameData frameData, String reason);

    String describeMode();
}
