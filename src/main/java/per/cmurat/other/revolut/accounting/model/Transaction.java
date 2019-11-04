package per.cmurat.other.revolut.accounting.model;

import per.cmurat.other.revolut.db.Entity;

import java.math.BigDecimal;

public class Transaction extends Entity {
    private AssetAccount debitAccount;
    private AssetAccount creditAccount;
    private BigDecimal amount;

    public AssetAccount getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(final AssetAccount debitAccount) {
        this.debitAccount = debitAccount;
    }

    public AssetAccount getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(final AssetAccount creditAccount) {
        this.creditAccount = creditAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }
}
