package de.isolveproblems.freeframe.api;

public class BuyerRiskProfile {
    private final String playerId;
    private final double score;
    private final boolean blocked;
    private final String reason;

    public BuyerRiskProfile(String playerId, double score, boolean blocked, String reason) {
        this.playerId = playerId == null ? "unknown" : playerId;
        this.score = score;
        this.blocked = blocked;
        this.reason = reason == null ? "" : reason;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public double getScore() {
        return this.score;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public String getReason() {
        return this.reason;
    }
}
