package per.cmurat.other.revolut.core;

public class AssertionUtils {
    public static <T> T checkNotNull(final T object, final String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}
