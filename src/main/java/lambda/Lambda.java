package lambda;

import java.util.Collection;

public class Lambda {
    @LambdaParameter
    public static Integer n;
    @LambdaParameter
    public static Integer m;
    @LambdaParameter
    public static Integer i;
    @LambdaParameter
    public static Integer idx;
    @LambdaParameter
    public static String s;
    @LambdaParameter
    public static String t;
    @LambdaParameter
    public static Double d;
    @LambdaParameter
    public static Character c;
    @LambdaParameter
    public static Collection<?> col;
    @LambdaParameter
    public static Object obj;
    @LambdaParameter
    public static Object _;

    @NewLambda
    public static <A1, R> Fn1<A1, R> λ(A1 a1, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, R> Fn1<A1, R> lambda(A1 a1, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, R> Fn1<A1, R> fn(A1 a1, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> λ(A1 a1, A2 a2, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> lambda(A1 a1, A2 a2, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> fn(A1 a1, A2 a2, R block) {
        throw new UnsupportedOperationException();
    }
}
