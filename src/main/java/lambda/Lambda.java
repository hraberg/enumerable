package lambda;

import java.util.Collection;

import lambda.exception.LambdaWeavingNotEnabledException;

/**
 * This class acts as a placeholder during compile time and is used to direct
 * the transformation process in LambdaLoader. No fields or methods in this
 * class are meant to be used at runtime.
 */
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
    public static Boolean b;
    @LambdaParameter
    public static Collection<?> col;
    @LambdaParameter
    public static Object obj;

    /**
     * This LambdaParameter is a convention used to to create a lambda
     * {@link #fn(Object, Object)} that takes no parameters.
     * 
     * <p>
     * For example, this function will always return 1, ignoring the argument:
     * </p>
     * 
     * <pre>
     * fn(_, 1);
     * </pre>
     * 
     */
    @LambdaParameter
    public static Object _;

    /**
     * @see #fn(Object, Object)
     */
    @NewLambda
    public static <A1, R> Fn1<A1, R> λ(A1 a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Object, Object)
     */
    @NewLambda
    public static <A1, R> Fn1<A1, R> lambda(A1 a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda with one argument.
     */
    @NewLambda
    public static <A1, R> Fn1<A1, R> fn(A1 a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Object, Object, Object)
     */
    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> λ(A1 a1, A2 a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Object, Object, Object)
     */
    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> lambda(A1 a1, A2 a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda with two arguments.
     */
    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> fn(A1 a1, A2 a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Object, Object, Object, Object)
     */
    @NewLambda
    public static <A1, A2, A3, R> Fn3<A1, A2, A3, R> λ(A1 a1, A2 a2, A3 a3, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Object, Object, Object, Object)
     */
    @NewLambda
    public static <A1, A2, A3, R> Fn3<A1, A2, A3, R> lambda(A1 a1, A2 a2, A3 a3, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda with three arguments.
     */
    @NewLambda
    public static <A1, A2, A3, R> Fn3<A1, A2, A3, R> fn(A1 a1, A2 a2, A3 a3, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

}
