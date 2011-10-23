package lambda.functionaljava;

import static fj.Show.*;
import static fj.data.List.*;
import static lambda.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.List;

public final class List_map {
    @LambdaParameter
    static Integer i1, i2;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final List<Integer> a = list(1, 2, 3);
        final List<Integer> b = a.map(λ(i1, i2, i1 + i2).f(42));
        listShow(intShow()).println(b); // [43,44,45]
    }
}
