package per.cmurat.other.revolut.accounting.exception;

public class AccountNotFoundException extends IllegalArgumentException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
