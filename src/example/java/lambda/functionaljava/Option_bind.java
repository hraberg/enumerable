package lambda.functionaljava;

import static fj.Show.*;
import static fj.data.Option.*;
import static lambda.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.Option;

public final class Option_bind {
    @LambdaParameter
    static Integer i;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Option<Integer> o1 = some(7);
        final Option<Integer> o2 = some(8);
        final Option<Integer> o3 = none();
        
        final Option<Integer> p1 = o1.bind(λ(i, i % 2 == 0 ? some(i * 3) : Option.<Integer> none()));
        final Option<Integer> p2 = o2.bind(λ(i, i % 2 == 0 ? some(i * 3) : Option.<Integer> none()));
        final Option<Integer> p3 = o3.bind(λ(i, i % 2 == 0 ? some(i * 3) : Option.<Integer> none()));

        optionShow(intShow()).println(p1); // None
        optionShow(intShow()).println(p2); // Some(24)
        optionShow(intShow()).println(p3); // None
    }
}
