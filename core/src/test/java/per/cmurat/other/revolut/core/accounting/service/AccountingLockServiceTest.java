package per.cmurat.other.revolut.core.accounting.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AccountingLockServiceTest {

    private AccountingLockService tested;

    @BeforeEach
    void before() {
        tested = new AccountingLockService();
    }

    @Test
    void shouldReturnSameLockWithSameKey() {
        final ReentrantLock lock1 = tested.getLock("lock");
        final ReentrantLock lock2 = tested.getLock("lock");

        assertEquals(lock1, lock2);
    }

    @Test
    void shouldReturnDifferentLockWithDifferentKet() {
        final ReentrantLock lock1 = tested.getLock("lock1");
        final ReentrantLock lock2 = tested.getLock("lock2");

        assertNotEquals(lock1, lock2);
    }
}
