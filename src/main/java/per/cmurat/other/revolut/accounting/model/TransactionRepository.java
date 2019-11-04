package per.cmurat.other.revolut.accounting.model;

import per.cmurat.other.revolut.db.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionRepository extends Repository<Transaction> {
    private final Map<Long, List<Transaction>> storeByDebitAccount = new ConcurrentHashMap<>();
    private final Map<Long, List<Transaction>> storeByCreditAccount = new ConcurrentHashMap<>();

    @Override
    public Transaction store(final Transaction transaction) {
        final Transaction stored = super.store(transaction);
        store(transaction.getDebitAccount().getId(), transaction, storeByDebitAccount);
        store(transaction.getCreditAccount().getId(), transaction, storeByCreditAccount);

        return stored;
    }

    private void store(final long key, final Transaction value, final Map<Long, List<Transaction>> store) {
        store.compute(key, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(value);

            return v;
        });
    }

    public List<Transaction> findByDebitAccountId(long debitAccountId) {
        return storeByDebitAccount.get(debitAccountId);
    }

    public List<Transaction> findByCreditAccountId(long creditAccountId) {
        return storeByCreditAccount.get(creditAccountId);
    }
}
