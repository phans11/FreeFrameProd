package de.isolveproblems.freeframe.api;

public interface TransactionGuard {
    boolean tryAcquire(String key);

    void release(String key);
}
