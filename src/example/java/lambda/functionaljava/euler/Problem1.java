package lambda.functionaljava.euler;

import static fj.data.List.*;
import static fj.function.Integers.*;
import static java.lang.System.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;

/**
 * Add all the natural numbers below one thousand that are multiples of 3 or 5.
 */
public class Problem1 {
    @LambdaParameter
    static Integer a;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        out.println(sum(range(0, 1000).filter(λ(a, a % 3 == 0 || a % 5 == 0))));
    }
}
