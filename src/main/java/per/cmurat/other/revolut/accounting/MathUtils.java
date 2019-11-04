package per.cmurat.other.revolut.accounting;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Collection;

public class MathUtils {
    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b, MATH_CONTEXT);
    }

    public static BigDecimal sub(BigDecimal a, BigDecimal b) {
        return a.subtract(b, MATH_CONTEXT);
    }
}
