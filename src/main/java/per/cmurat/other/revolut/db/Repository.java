package per.cmurat.other.revolut.db;

import io.vavr.control.Try;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static per.cmurat.other.revolut.LockUtils.tryInLock;

public abstract class Repository<T extends Entity> {

    private final Map<Long, T> store = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();
    private AtomicLong idCounter = new AtomicLong(0);

    public T store(T t) {
        assignId(t);
        store.put(t.getId(), t);
        return t;
    }

    public final T findById(long id) {
        return store.get(id);
    }

    private void assignId(T t) {
        if (t.getId() == null) {
            Try result = tryInLock(lock, () -> {
                t.setId(idCounter.incrementAndGet());
            });

            if (result.isFailure()) {
                throw new RuntimeException(result.getCause());
            }
        }
    }
}
