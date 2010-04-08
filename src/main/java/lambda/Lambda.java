package lambda;

import java.util.Collection;

import lambda.annotation.LambdaParameter;
import lambda.annotation.NewLambda;
import lambda.annotation.Unused;
import lambda.exception.LambdaWeavingNotEnabledException;

/**
 * This class acts as a placeholder during compile time and is used to direct
 * the transformation process in LambdaLoader. No fields or methods in this
 * class are meant to be used at runtime.
 * <p>
 * If you want to use the λ form you need to use UTF-8 encoding for your
 * sources. To easily insert it, you can use a Java Editor Template like this in
 * Eclipse and bind it to for example "cls" (for closure):
 * 
 * <pre>
 * λ(${impst:importStatic('lambda.Lambda.*')}${cursor})
 * </pre>
 */
public class Lambda {
    @LambdaParameter
    public static int n;
    @LambdaParameter
    public static int m;
    @LambdaParameter
    public static int i;
    @LambdaParameter
    public static int idx;
    @LambdaParameter
    public static long k;
    @LambdaParameter
    public static long l;
    @LambdaParameter
    public static double d;
    @LambdaParameter
    public static double x;
    @LambdaParameter
    public static double y;
    @LambdaParameter
    public static char c;
    @LambdaParameter
    public static boolean b;
    @LambdaParameter
    public static String s;
    @LambdaParameter
    public static String t;
    @LambdaParameter
    public static Collection<?> col;
    @LambdaParameter
    public static Object obj;

    /**
     * This LambdaParameter is a used to to create a lambda
     * {@link #fn(Unused, Object)} that takes no parameters.
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
    public static Unused _;

    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking three arguments. See {@link #delegate(Unused, Object)} for
     * an example.
     */
    @NewLambda
    static <A1, A2, A3, I> I delegate(A1 a1, A2 a2, A3 a3, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking two arguments. See {@link #delegate(Unused, Object)} for
     * an example.
     */
    @NewLambda
    static <A1, A2, I> I delegate(A1 a1, A2 a2, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking one argument. See {@link #delegate(Unused, Object)} for an
     * example.
     */
    @NewLambda
    static <A1, I> I delegate(A1 a1, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking no arguments.
     * 
     * <p>
     * Example:
     * </p>
     * 
     * <pre>
     * Runnable r = delegate(_, out.printf(&quot;hello\n&quot;));
     * </pre>
     * 
     * The real type of I is resolved during the transformation by inspecting
     * the bytecode.
     * 
     */
    @NewLambda
    static <I> I delegate(Unused _, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda with three arguments.
     */
    @NewLambda
    public static <A1, A2, A3, R> Fn3<A1, A2, A3, R> fn(A1 a1, A2 a2, A3 a3, R block) {
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
     * Creates a new lambda with one argument.
     */
    @NewLambda
    public static <A1, R> Fn1<A1, R> fn(A1 a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda with no arguments.
     */
    @NewLambda
    public static <R> Fn0<R> fn(Unused _, R block) {
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
     * @see #fn(Object, Object, Object)
     */
    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> λ(A1 a1, A2 a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Object, Object)
     */
    @NewLambda
    public static <A1, R> Fn1<A1, R> λ(A1 a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn(Unused, Object)
     */
    @NewLambda
    public static <R> Fn0<R> λ(Unused _, R block) {
        throw new LambdaWeavingNotEnabledException();
    }
}
