package per.cmurat.other.revolut.accounting.exception;

public class InsufficientBalanceException extends IllegalArgumentException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
