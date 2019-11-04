package per.cmurat.other.revolut.accounting.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AccountingLockService {
    private final Map<String, ReentrantLock> keyLockMap = new HashMap<>();

    public synchronized ReentrantLock getLock(String key) {
        return keyLockMap.compute(key, (k, v) -> {
            if (v == null) {
                v = new ReentrantLock();
            }

            return v;
        });
    }
}
