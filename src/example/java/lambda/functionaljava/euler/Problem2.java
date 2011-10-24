package lambda.functionaljava.euler;

import static fj.Ord.*;
import static fj.data.Stream.*;
import static fj.function.Integers.*;
import static java.lang.System.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.F2;
import fj.data.Stream;

/**
 * Find the sum of all the even-valued terms in the Fibonacci sequence which do
 * not exceed four million.
 */
public class Problem2 {
    @LambdaParameter
    static Integer a, b;
    
    static F2<Integer, Integer, Stream<Integer>> fib;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Stream<Integer> fibs = (fib = λ(a, b, cons(a, fib.curry().f(b).lazy().f(a + b)))).f(1, 2);
        out.println(sum(fibs.filter(λ(a, a % 2 == 0)).takeWhile(intOrd.isLessThan(4000001)).toList()));
    }
}
