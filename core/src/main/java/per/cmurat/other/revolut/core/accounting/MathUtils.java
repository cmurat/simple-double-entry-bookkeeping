package per.cmurat.other.revolut.core.accounting;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ROUND_FLOOR;

public class MathUtils {
    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b, MATH_CONTEXT);
    }

    public static BigDecimal sub(BigDecimal a, BigDecimal b) {
        return a.subtract(b, MATH_CONTEXT);
    }

    public static BigDecimal readableScale(BigDecimal bd) {
        return bd.setScale(2, ROUND_FLOOR);
    }
}
