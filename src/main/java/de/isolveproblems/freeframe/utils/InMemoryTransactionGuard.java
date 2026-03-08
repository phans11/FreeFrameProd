package de.isolveproblems.freeframe.utils;

import de.isolveproblems.freeframe.api.TransactionGuard;

import java.util.HashSet;
import java.util.Set;

public class InMemoryTransactionGuard implements TransactionGuard {
    private final Set<String> activeTransactions = new HashSet<String>();

    @Override
    public synchronized boolean tryAcquire(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        return this.activeTransactions.add(key);
    }

    @Override
    public synchronized void release(String key) {
        if (key != null) {
            this.activeTransactions.remove(key);
        }
    }
}
