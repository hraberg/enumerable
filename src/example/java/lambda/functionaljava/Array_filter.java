package lambda.functionaljava;

import static fj.Show.*;
import static fj.data.Array.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import static lambda.functionaljava.Array_bind.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.Array;

public final class Array_filter {
    @LambdaParameter
    static Integer i;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final Array<Integer> b = a.filter(λ(i, i % 2 == 0));
        arrayShow(intShow()).println(b); // {44,22,90,98,1078,6,64,6,42}
    }
}
