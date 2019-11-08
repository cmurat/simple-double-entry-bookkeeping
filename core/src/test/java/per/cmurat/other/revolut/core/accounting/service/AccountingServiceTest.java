package per.cmurat.other.revolut.core.accounting.service;

import com.jayway.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import per.cmurat.other.revolut.core.accounting.exception.AccountNotFoundException;
import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.AssetAccountRepository;
import per.cmurat.other.revolut.core.accounting.model.Transaction;
import per.cmurat.other.revolut.core.accounting.model.TransactionRepository;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {

    @Mock
    private AssetAccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountingLockService lockService;

    @InjectMocks
    private AccountingService tested;

    @Test
    void getNonExistentAccountShouldThrowException() {
        long accountId = 99L;
        when(accountRepository.findById(eq(accountId))).thenReturn(null);

        assertThrows(AccountNotFoundException.class, () -> tested.getAccount(accountId));

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountShouldReturnCorrectValue() {
        final long accountId = 99L;
        final AssetAccount expected = new AssetAccount();
        when(accountRepository.findById(eq(accountId))).thenReturn(expected);

        final AssetAccount actual = tested.getAccount(accountId);

        assertEquals(expected, actual);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void createAccountShouldSucceed() {
        final long id = 1L;
        final BigDecimal balance = new BigDecimal("123.124");
        final AssetAccount expected = new AssetAccount();
        expected.setBalance(balance);
        expected.setId(id);

        when(accountRepository.store(isA(AssetAccount.class))).thenReturn(expected);

        final AssetAccount actual = tested.createAccount(balance);

        verify(accountRepository, times(1)).store(isA(AssetAccount.class));

        assertEquals(expected.getBalance(), actual.getBalance());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected, actual);
    }

    @Test
    void createAccountShouldSucceedWhenBalanceIsZero() {
        final long id = 1L;
        final BigDecimal balance = new BigDecimal("0");
        final AssetAccount expected = new AssetAccount();
        expected.setBalance(balance);
        expected.setId(id);

        when(accountRepository.store(isA(AssetAccount.class))).thenReturn(expected);

        final AssetAccount actual = tested.createAccount(balance);

        verify(accountRepository, times(1)).store(isA(AssetAccount.class));

        assertEquals(expected.getBalance(), actual.getBalance());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected, actual);
    }

    @Test
    void createAccountShouldThrowWhenBalanceIsNegative() {
        final BigDecimal balance = new BigDecimal("-5.21");

        assertThrows(IllegalArgumentException.class, () -> tested.createAccount(balance));

        verify(accountRepository, times(0)).store(isA(AssetAccount.class));
    }

    @Test
    void validateTransferShouldSucceed() throws Throwable {
        final long creditAccountId = 1L;
        final AssetAccount creditAccount = mock(AssetAccount.class);

        final long debitAccountId = 2L;
        final AssetAccount debitAccount = mock(AssetAccount.class);

        final BigDecimal amount = new BigDecimal("123.124");

        lenient().when(accountRepository.findById(creditAccountId)).thenReturn(creditAccount);
        lenient().when(accountRepository.findById(debitAccountId)).thenReturn(debitAccount);

        lenient().when(lockService.getLock(anyString())).thenReturn(new ReentrantLock());
        lenient().when(lockService.getLock(anyString())).thenReturn(new ReentrantLock());

        when(creditAccount.clone()).thenReturn(creditAccount);
        when(debitAccount.clone()).thenReturn(debitAccount);

        tested.validate(creditAccountId, debitAccountId, amount);

        verify(accountRepository, times(1)).findById(creditAccountId);
        verify(accountRepository, times(1)).findById(debitAccountId);

        verify(creditAccount, times(1)).credit(amount);
        verify(debitAccount, times(1)).debit(amount);
    }

    @Test
    void validateTransferShouldThrowWhenAccountIsNonexistent() {
        final long creditAccountId = 1L;
        final AssetAccount creditAccount = mock(AssetAccount.class);

        final long debitAccountId = 2L;
        final AssetAccount debitAccount = mock(AssetAccount.class);

        final BigDecimal amount = new BigDecimal("123.124");

        lenient().when(accountRepository.findById(creditAccountId)).thenReturn(creditAccount);
        lenient().when(accountRepository.findById(debitAccountId)).thenReturn(null);

        lenient().when(lockService.getLock(anyString())).thenReturn(new ReentrantLock());

        assertThrows(AccountNotFoundException.class, () -> tested.validate(creditAccountId, debitAccountId, amount));

        verify(accountRepository, times(1)).findById(creditAccountId);
        verify(accountRepository, times(1)).findById(debitAccountId);

        verify(lockService, times(2)).getLock(anyString());
    }

    @Test
    void transferShouldSucceed() {
        final long creditAccountId = 1L;
        final AssetAccount creditAccount = mock(AssetAccount.class);

        final long debitAccountId = 2L;
        final AssetAccount debitAccount = mock(AssetAccount.class);

        final BigDecimal amount = new BigDecimal("123.124");

        lenient().when(accountRepository.findById(creditAccountId)).thenReturn(creditAccount);
        lenient().when(accountRepository.findById(debitAccountId)).thenReturn(debitAccount);

        lenient().when(lockService.getLock(anyString())).thenReturn(new ReentrantLock());
        lenient().when(lockService.getLock(anyString())).thenReturn(new ReentrantLock());

        final Transaction actual = tested.transfer(creditAccountId, debitAccountId, amount);

        verify(accountRepository, times(1)).findById(creditAccountId);
        verify(accountRepository, times(1)).findById(debitAccountId);

        verify(creditAccount, times(1)).credit(amount);
        verify(debitAccount, times(1)).debit(amount);

        assertEquals(creditAccount, actual.getCreditAccount());
        assertEquals(debitAccount, actual.getDebitAccount());
        assertEquals(amount, actual.getAmount());
        assertNotNull(actual.getDateTime());
    }

    @Test
    void validateTransferShouldBlockForSameAccount() throws Throwable {
        testTransferFunctionShouldBlockOnSameAccount(tested::validate);
    }

    @Test
    void transferShouldBlockForSameAccount() throws Throwable {
        testTransferFunctionShouldBlockOnSameAccount((creditAccountId, debitAccountId, amount) ->
                tested.transfer(creditAccountId, debitAccountId, amount));
    }

    //The pass method is testes whether it is acquiring locks for the given accounts and thus blocking any subsequent calls with the same accounts from another thread.
    private void testTransferFunctionShouldBlockOnSameAccount(final TransferFunction f) throws InterruptedException, ExecutionException, TimeoutException {
        final BigDecimal balance = new BigDecimal("10");

        final long creditAccountId = 1L;
        final AssetAccount creditAccount = new AssetAccount();
        creditAccount.setBalance(balance);

        //This latch is for making the second thread wait until the first thread can acquire the locks.
        final CountDownLatch startSecondFunctionLatch = new CountDownLatch(1);

        //This latch is for asserting that the locks are acquired and
        //the seconds thread is waiting for the first thread to release the locks.
        final CountDownLatch finishFirstFunctionLatch = new CountDownLatch(1);

        //This account is used in the first thread. We assume that the debit method call is made after the locks are acquired.
        //This means that once the debit method is called, the seconds thread can start.
        //It is also used to suspend the first thread, to assert the state of the locks.
        final long debitAccountId1 = 2L;
        final AssetAccount debitAccount1 = new AssetAccount() {
            @Override
            public void debit(final BigDecimal amount) {
                try {
                    startSecondFunctionLatch.countDown();
                    finishFirstFunctionLatch.await();
                } catch (InterruptedException ignored) {
                }
                super.debit(amount);
            }

            @Override
            public AssetAccount clone() {
                return this;
            }
        };
        debitAccount1.setBalance(balance);

        final long debitAccountId2 = 3L;
        final AssetAccount debitAccount2 = new AssetAccount();
        debitAccount2.setBalance(balance);

        lenient().when(accountRepository.findById(creditAccountId)).thenReturn(creditAccount);
        lenient().when(accountRepository.findById(debitAccountId1)).thenReturn(debitAccount1);
        lenient().when(accountRepository.findById(debitAccountId2)).thenReturn(debitAccount2);

        final ReentrantLock creditAccountLock = new ReentrantLock();
        lenient().when(lockService.getLock(tested.getLockNameForAccount(creditAccountId))).thenReturn(creditAccountLock);
        lenient().when(lockService.getLock(tested.getLockNameForAccount(debitAccountId1))).thenReturn(new ReentrantLock());
        lenient().when(lockService.getLock(tested.getLockNameForAccount(debitAccountId2))).thenReturn(new ReentrantLock());

        final BigDecimal amount = new BigDecimal("1");

        Future first = newSingleThreadExecutor().submit(() -> {
            try {
                f.transaction(creditAccountId, debitAccountId1, amount);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });

        Future second = newSingleThreadExecutor().submit(() -> {
            try {
                startSecondFunctionLatch.await();
                f.transaction(creditAccountId, debitAccountId2, amount);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });

        //Wait until the second thread is waiting on the lock.
        Awaitility
                .given()
                .pollInterval(100L, TimeUnit.MILLISECONDS)
                .atMost(2L, TimeUnit.SECONDS)
                .until(creditAccountLock::hasQueuedThreads);

        assertFalse(first.isDone());
        assertFalse(second.isDone());

        //The first thread should have the lock and the second thread should be queued.
        assertTrue(creditAccountLock.isLocked());

        //Assertions are done, we can release the first thread.
        finishFirstFunctionLatch.countDown();

        //Make sure the both threads finish their job.
        final long timeoutSeconds = 2L;
        first.get(timeoutSeconds, TimeUnit.SECONDS);
        second.get(timeoutSeconds, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface TransferFunction {
        void transaction(final long creditAccountId, final long debitAccountId2, final BigDecimal amount) throws Throwable;
    }
}



















