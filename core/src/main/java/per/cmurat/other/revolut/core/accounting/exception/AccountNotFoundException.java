package per.cmurat.other.revolut.core.accounting.exception;

public class AccountNotFoundException extends IllegalArgumentException {
    public AccountNotFoundException(final String message) {
        super(message);
    }
}
