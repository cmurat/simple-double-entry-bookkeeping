package per.cmurat.other.revolut.core.rest.dto;

import java.math.BigDecimal;

public class AssetAccountDto {
    private long id;
    private BigDecimal balance;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }
}
