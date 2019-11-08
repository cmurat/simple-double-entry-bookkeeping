package per.cmurat.other.revolut.core.accounting.service;

import io.vavr.control.Try;
import per.cmurat.other.revolut.core.LockUtils;
import per.cmurat.other.revolut.core.accounting.exception.AccountNotFoundException;
import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.AssetAccountRepository;
import per.cmurat.other.revolut.core.accounting.model.Transaction;
import per.cmurat.other.revolut.core.accounting.model.TransactionRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static per.cmurat.other.revolut.core.AssertionUtils.checkNotNull;

/**
 * This service is used for managing accounts doing transactions between them.
 *
 * Validation can be done by calling {@link #validate(long, long, BigDecimal)}, before executing the
 * transaction by calling {@link #transfer(long, long, BigDecimal)}. However, if there are other operations
 * (by the caller or a third party) between validation and execution, the validation results might be stale.
 */
public class AccountingService {

    @Inject
    private AssetAccountRepository accountRepository;

    @Inject
    private TransactionRepository transactionRepository;

    @Inject
    private AccountingLockService lockService;

    /**
     * Creates an account with the given balance. Caller should keep the account ID if
     * they wish to make transfers in the future using the account.
     *
     * @param balance Initial balance of the account
     * @return Newly created and stored account object.
     */
    public AssetAccount createAccount(BigDecimal balance) {
        checkNotNull(balance, "Balance cannot be null");

        if (balance.signum() < 0) {
            throw new IllegalArgumentException("Balance must be non-negative");
        }

        AssetAccount account = new AssetAccount();
        account.setBalance(balance);

        return accountRepository.store(account);
    }

    public AssetAccount getAccount(final long id) {
        final AssetAccount account = accountRepository.findById(id);
        if (account == null) {
            throw new AccountNotFoundException("Account not found. Account id: " + id);
        }
        return account;
    }

    /**
     * Validates a transaction by checking the existence of accounts. If the accounts exist,
     * balances of accounts are checked to make sure transaction is doable.
     */
    public void validate(final long creditAccountId, final long debitAccountId, final BigDecimal amount) throws Throwable {
        Try t = LockUtils.tryInLock(
                lockService.getLock(getLockNameForAccount(creditAccountId)),
                () -> LockUtils.tryInLock(
                        lockService.getLock(getLockNameForAccount(debitAccountId)),
                        () -> doValidate(creditAccountId, debitAccountId, amount)
                )
        ).get();

        if (t.isFailure()) {
            throw t.getCause();
        }
    }

    /**
     * Ideally, this method should be called after acquiring locks for both of the accounts.
     */
    private void doValidate(final long creditAccountId, final long debitAccountId, final BigDecimal amount) {
        final AssetAccount creditAccount = getAccount(creditAccountId);
        final AssetAccount debitAccount = getAccount(debitAccountId);

        creditAccount.credit(amount);
        debitAccount.debit(amount);
    }

    /**
     * Create and finalize a transaction in a single step. Debit and credit accounts can be the same account.
     * Before calling this method, the transfer should be validated by calling {@link #validate(long, long, BigDecimal)}.
     * There can be other operations between validation and execution, which might make the validation result stale.
     *
     * @param creditAccountId Account that the money will be taken from
     * @param debitAccountId Account that will receive the money
     * @param amount Amount of money to transfer
     * @return The resulting transaction
     */
    public Transaction transfer(final long creditAccountId, final long debitAccountId, final BigDecimal amount) {
        Try<Transaction> t = LockUtils.tryInLock(
                lockService.getLock(getLockNameForAccount(creditAccountId)),
                () -> (Try<Transaction>) LockUtils.tryInLock(
                        lockService.getLock(getLockNameForAccount(debitAccountId)),
                        () -> doTransfer(creditAccountId, debitAccountId, amount)
                )
        ).get();

        if (t.isFailure()) {
            throw new RuntimeException(t.getCause());
        }

        return t.get();
    }

    /**
     * Do not call this method without acquiring locks for both of the accounts.
     * Otherwise there might be concurrency problems.
     */
    private Transaction doTransfer(final long creditAccountId, final long debitAccountId, final BigDecimal amount) {
        final AssetAccount creditAccount = getAccount(creditAccountId);
        final AssetAccount debitAccount = getAccount(debitAccountId);

        final Transaction transaction = new Transaction();
        transaction.setCreditAccount(creditAccount);
        transaction.setDebitAccount(debitAccount);
        transaction.setAmount(amount);
        transaction.setDateTime(LocalDateTime.now());

        creditAccount.credit(amount);
        debitAccount.debit(amount);

        //Normally this part should be transactional. But since we are using in-memory db, we will ignore this.
        transactionRepository.store(transaction);
        accountRepository.store(creditAccount);
        accountRepository.store(debitAccount);

        return transaction;
    }

    //Visible for testing
    String getLockNameForAccount(long accountId) {
        return "Account-" + accountId;
    }
}
