package per.cmurat.other.revolut.core.rest;

import per.cmurat.other.revolut.core.accounting.model.AssetAccount;
import per.cmurat.other.revolut.core.accounting.model.Transaction;
import per.cmurat.other.revolut.core.accounting.service.AccountingService;
import spark.utils.StringUtils;

import javax.inject.Inject;
import java.math.BigDecimal;

public class AccountingController {

    @Inject
    private AccountingService accountingService;

    public AssetAccount createAccount(final BigDecimal balance) {
        checkBalanceParameter(balance);
        return accountingService.createAccount(balance);
    }

    public AssetAccount getAccount(final String accountId) {
        return accountingService.getAccount(checkAndParseAccountId(accountId));
    }

    public void validateTransfer(final long sendingAccountId, final long receivingAccountId, BigDecimal amount) {
        checkAmountParameter(amount);
        try {
            accountingService.validate(sendingAccountId, receivingAccountId, amount);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Transaction processTransfer(final long sendingAccountId, final long receivingAccountId, BigDecimal amount) {
        checkAmountParameter(amount);
        return accountingService.transfer(sendingAccountId, receivingAccountId, amount);
    }

    private void checkBalanceParameter(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }

        if (balance.signum() < 0) {
            throw new IllegalArgumentException("Balance must be non-negative");
        }
    }

    private void checkAmountParameter(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.signum() < 1) {
            throw new IllegalArgumentException("Amount must be bigger than 0");
        }
    }

    private long checkAndParseAccountId(String accountIdStr) {
        if (StringUtils.isEmpty(accountIdStr)) {
            throw new IllegalArgumentException("Account ID must not be empty.");
        }

        final long accountId;
        try {
            accountId = Long.parseLong(accountIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Account ID must be a valid long value.", e);
        }

        return accountId;
    }
}
