package per.cmurat.other.revolut.core.accounting.exception;

public class InsufficientBalanceException extends IllegalArgumentException {
    public InsufficientBalanceException(final String message) {
        super(message);
    }
}
