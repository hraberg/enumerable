package lambda.functionaljava;

import static fj.Show.*;
import static fj.data.List.*;
import static lambda.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.F;
import fj.data.List;

public class List_apply {
    @LambdaParameter
    static Integer i1, i2;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final List<F<Integer, Integer>> fs = single(λ(i1, i2, i1 - i2).f(2)).cons(λ(i1, i2, i1 * i2).f(2)).cons(λ(i1, i2, i1 + i2).f(2));
        final List<Integer> three = list(3);
        listShow(intShow()).println(three.apply(fs)); // Prints out: <5,6,-1>
    }
}