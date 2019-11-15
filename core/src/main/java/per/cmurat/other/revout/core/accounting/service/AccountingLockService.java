package per.cmurat.other.revolut.core.accounting.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stores locks for the given key. It is intended to be used with {@link AccountingService}.
 */
class AccountingLockService {
    private final Map<String, ReentrantLock> keyLockMap = new HashMap<>();

    synchronized ReentrantLock getLock(String key) {
        return keyLockMap.compute(key, (k, v) -> {
            if (v == null) {
                v = new ReentrantLock();
            }
            return v;
        });
    }
}
