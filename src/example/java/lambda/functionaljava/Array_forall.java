package lambda.functionaljava;

import static fj.data.Array.*;
import static fj.data.List.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.data.Array;

public final class Array_forall {
    @LambdaParameter
    static String s;
    @LambdaParameter
    static Character ch;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Array<String> a = array("hello", "There", "what", "day", "is", "it");
        final boolean b = a.forall(λ(s, fromString(s).forall(λ(ch, Character.isLowerCase(ch)))));
        System.out.println(b); // false ("There" is a counter-example; try
                               // removing it)
    }
}
