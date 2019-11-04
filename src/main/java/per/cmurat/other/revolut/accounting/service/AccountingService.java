package per.cmurat.other.revolut.accounting.service;

import io.vavr.control.Try;
import per.cmurat.other.revolut.LockUtils;
import per.cmurat.other.revolut.accounting.model.AssetAccount;
import per.cmurat.other.revolut.accounting.model.AssetAccountRepository;
import per.cmurat.other.revolut.accounting.model.Transaction;
import per.cmurat.other.revolut.accounting.model.TransactionRepository;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.sun.tools.javac.util.Assert.checkNonNull;

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
        AssetAccount account = new AssetAccount();
        account.setBalance(balance);

        return accountRepository.store(account);
    }

    /**
     * Validates a transaction by checking the existence of accounts. If the accounts exist,
     * balances of accounts are checked to make sure transaction is doable.
     */
    public Try validate(final long creditAccountId, final long debitAccountId, final BigDecimal amount) {
        return LockUtils.tryInLock(
                lockService.getLock(getLockNameForAccount(creditAccountId)),
                () -> LockUtils.tryInLock(
                        lockService.getLock(getLockNameForAccount(debitAccountId)),
                        () -> doValidate(creditAccountId, debitAccountId, amount)
                )
        ).get();
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

        Transaction transaction = new Transaction();
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

    public AssetAccount getAccount(final long id) {
        return checkNonNull(accountRepository.findById(id), "Account not found. Account id: " + id);
    }

    private String getLockNameForAccount(long accountId) {
        return "Account-" + accountId;
    }
}
