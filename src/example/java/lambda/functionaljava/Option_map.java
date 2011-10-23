package lambda.functionaljava;

import static fj.Show.*;
import static fj.data.Option.*;
import static lambda.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.Option;

public final class Option_map {
    @LambdaParameter
    static Integer i1, i2;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Option<Integer> o1 = some(7);
        final Option<Integer> o2 = none();
        final Option<Integer> p1 = o1.map(λ(i1, i2, i1 + i2).f(42));
        final Option<Integer> p2 = o2.map(λ(i1, i2, i1 + i2).f(42));
        optionShow(intShow()).println(p1); // Some(49)
        optionShow(intShow()).println(p2); // None
    }
}
