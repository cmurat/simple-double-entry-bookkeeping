package per.cmurat.other.revolut.core.db;

import io.vavr.control.Try;
import per.cmurat.other.revolut.core.LockUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Repository<T extends Entity> {

    private final Map<Long, T> store = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();
    private AtomicLong idCounter = new AtomicLong(0);

    public T store(T t) {
        assignId(t);
        store.put(t.getId(), t);
        return t;
    }

    public T findById(long id) {
        return store.get(id);
    }

    private void assignId(T t) {
        if (t.getId() == null) {
            Try result = LockUtils.tryInLock(lock, () -> {
                t.setId(idCounter.incrementAndGet());
            });

            if (result.isFailure()) {
                throw new RuntimeException(result.getCause());
            }
        }
    }
}
