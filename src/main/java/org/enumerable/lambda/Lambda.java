package org.enumerable.lambda;

import org.enumerable.lambda.annotation.NewLambda;
import org.enumerable.lambda.exception.LambdaWeavingNotEnabledException;

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
@SuppressWarnings("unused")
public class Lambda {
    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking three arguments. See {@link #delegate(Object)} for an
     * example.
     */
    @NewLambda
    public static <A1, A2, A3, I> I delegate(A1 a1, A2 a2, A3 a3, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking two arguments. See {@link #delegate(Object)} for an
     * example.
     */
    @NewLambda
    public static <A1, A2, I> I delegate(A1 a1, A2 a2, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing single abstract method interface or
     * class I taking one argument. See {@link #delegate(Object)} for an
     * example.
     */
    @NewLambda
    public static <A1, I> I delegate(A1 a1, Object block) {
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
     * Runnable r = delegate(out.printf(&quot;hello\n&quot;));
     * </pre>
     * 
     * The real type of I is resolved during the transformation by inspecting
     * the bytecode.
     * 
     */
    @NewLambda
    public static <I> I delegate(Object block) {
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
    public static <R> Fn0<R> fn(R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates an empty lambda expression returning null.
     */
    @NewLambda
    public static <R> Fn0<R> fn() {
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
     * @see #fn(Object)
     */
    @NewLambda
    public static <R> Fn0<R> λ(R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see #fn()
     */
    @NewLambda
    public static <R> Fn0<R> λ() {
        throw new LambdaWeavingNotEnabledException();
    }
}
