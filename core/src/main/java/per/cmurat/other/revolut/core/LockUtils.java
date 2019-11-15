package per.cmurat.other.revolut.core;

import io.vavr.control.Try;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public class LockUtils {
    public static <T> Try<T> tryInLock(final ReentrantLock lock, final Callable<T> c) {
        try {
            lock.lock();
            return Try.ofCallable(c);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public static Try tryInLock(final ReentrantLock lock, final Runnable r) {
        try {
            lock.lock();
            return Try.runRunnable(r);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
