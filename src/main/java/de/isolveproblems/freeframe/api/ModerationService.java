package de.isolveproblems.freeframe.api;

import de.isolveproblems.freeframe.utils.FreeFrameData;

import java.util.List;

public interface ModerationService {
    boolean isFrameFrozen(String frameId);

    boolean isPlayerRestricted(String playerId);

    String frameRestrictionReason(String frameId);

    String playerRestrictionReason(String playerId);

    void freezeFrame(FreeFrameData frameData, String actor, String reason);

    void unfreezeFrame(String frameId, String actor);

    void restrictPlayer(String playerId, String actor, String reason);

    void unrestrictPlayer(String playerId, String actor);

    List<String> tailLog(int limit);
}
