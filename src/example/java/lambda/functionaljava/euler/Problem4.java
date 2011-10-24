package lambda.functionaljava.euler;

import static fj.Equal.*;
import static fj.Function.*;
import static fj.Ord.*;
import static fj.Show.*;
import static fj.data.Stream.*;
import static fj.function.Integers.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.Stream;

/**
 * Find the largest palindrome made from the product of two 3-digit numbers.
 */
public class Problem4 {
    @LambdaParameter
    static Integer i;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Stream<Integer> tdl = iterate(flip(subtract).f(1), 999).takeWhile(intOrd.isGreaterThan(99));
        intShow.println(tdl.tails().bind(tdl.zipWith(multiply)).filter(λ(i, streamEqual(charEqual).eq(intShow.show(i).reverse().take(3), intShow.show(i).take(3)))).foldLeft1(intOrd.max));
    }
}
