package per.cmurat.other.revolut.core.accounting.exception;

public class InsufficientBalanceException extends IllegalArgumentException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
