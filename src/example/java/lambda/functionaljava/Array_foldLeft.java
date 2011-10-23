package lambda.functionaljava;

import static fj.data.Array.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.Array;

public final class Array_foldLeft {
    @LambdaParameter
    static Integer i1, i2;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final int b = a.foldLeft(λ(i1, i2, i1 + i2), 0);
        System.out.println(b); // 1774
    }
}
