package de.isolveproblems.freeframe.utils;

public class SignedPurchaseToken {
    private final String txId;
    private final String signature;
    private final String fingerprint;

    public SignedPurchaseToken(String txId, String signature, String fingerprint) {
        this.txId = txId;
        this.signature = signature;
        this.fingerprint = fingerprint;
    }

    public String getTxId() {
        return this.txId;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getFingerprint() {
        return this.fingerprint;
    }
}
