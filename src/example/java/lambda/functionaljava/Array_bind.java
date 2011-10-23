package lambda.functionaljava;

import static fj.Show.*;
import static fj.data.Array.*;
import lambda.annotation.LambdaParameter;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.weaving.LambdaLoader;
import fj.Show;
import fj.data.Array;
import fj.data.Stream;

public final class Array_bind {
    @LambdaParameter
    static Integer i;
    
    @LambdaParameter
    static Object any;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final Array<Integer> b = a.bind(λ(i, array(500, i)));

        arrayShow(intShow()).println(b);
        // {500,97,500,44,500,67,500,3,500,22,500,90,500,1,500,77,500,98,500,1078,500,6,500,64,500,6,500,79,500,42}
    }

    public static final Show<Integer> intShow() {
        return anyShow();
    }

    @SuppressWarnings("unchecked")
    public static <T> Show<T> anyShow() {
        return (Show<T>) show(λ(any, Stream.fromString(any.toString())));
    }
}
