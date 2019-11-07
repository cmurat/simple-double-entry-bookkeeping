package per.cmurat.other.revolut.core.accounting.model;

import per.cmurat.other.revolut.core.accounting.exception.InsufficientBalanceException;
import per.cmurat.other.revolut.core.db.Entity;

import java.math.BigDecimal;

import static per.cmurat.other.revolut.core.accounting.MathUtils.add;
import static per.cmurat.other.revolut.core.accounting.MathUtils.readableScale;
import static per.cmurat.other.revolut.core.accounting.MathUtils.sub;

public class AssetAccount extends Entity {
    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void debit(BigDecimal amount) {
        BigDecimal newBalance = add(this.getBalance(), amount);
        this.setBalance(newBalance);
    }

    public void credit(BigDecimal amount) {
        BigDecimal newBalance = sub(this.getBalance(), amount);

        if (newBalance.signum() == -1 && newBalance.signum() == -1) {
            throw new InsufficientBalanceException("Asset account doesn't have sufficient balance. Current balance: " + readableScale(balance).toPlainString());
        }

        this.setBalance(newBalance);
    }
}
