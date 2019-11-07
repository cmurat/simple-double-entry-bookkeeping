package per.cmurat.other.revolut.accounting.model;

import per.cmurat.other.revolut.accounting.exception.InsufficientBalanceException;
import per.cmurat.other.revolut.db.Entity;

import java.math.BigDecimal;

import static per.cmurat.other.revolut.accounting.MathUtils.add;
import static per.cmurat.other.revolut.accounting.MathUtils.readableScale;
import static per.cmurat.other.revolut.accounting.MathUtils.sub;

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
            throw new InsufficientBalanceException("Asset account doesn't have sufficient balance. Current balance: " + readableScale(amount).toPlainString());
        }

        this.setBalance(newBalance);
    }
}
