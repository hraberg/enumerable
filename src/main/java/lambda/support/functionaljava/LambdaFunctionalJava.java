package lambda.support.functionaljava;

import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import fj.F;
import fj.F2;
import fj.F3;

/**
 * This is class is similar {@link lambda.Lambda}, but instead of creating
 * lambdas inheriting from {@link lambda.Fn0} it creates lambdas implementing
 * the {@link F}, {@link F2} and {@link F3 }from FunctionalJava.
 */
public class LambdaFunctionalJava {
    @NewLambda
    public static <A, B> F<A, B> f(A a, B b) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A, B, C> F2<A, B, C> f(A a, B b, C c) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A, B, C, D> F3<A, B, C, D> f(A a, B b, C c, D d) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A, B> F<A, B> λ(A a, B b) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A, B, C> F2<A, B, C> λ(A a, B b, C c) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A, B, C, D> F3<A, B, C, D> λ(A a, B b, C c, D d) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link F} in a {@link Fn1}.
     */
    @SuppressWarnings("serial")
    public static <A, B> Fn1<A, B> toFn1(final F<A, B> f) {
        return new Fn1<A, B>() {
            public B call(A a) {
                return f.f(a);
            }
        };
    }

    /**
     * Wraps the {@link F2} in a {@link Fn2}.
     */
    @SuppressWarnings("serial")
    public static <A, B, C> Fn2<A, B, C> toFn2(final F2<A, B, C> f) {
        return new Fn2<A, B, C>() {
            public C call(A a, B b) {
                return f.f(a, b);
            }
        };
    }

    /**
     * Wraps the {@link F3} in a {@link Fn3}.
     */
    @SuppressWarnings("serial")
    public static <A, B, C, D> Fn3<A, B, C, D> toFn3(final F3<A, B, C, D> f) {
        return new Fn3<A, B, C, D>() {
            public D call(A a, B b, C c) {
                return f.f(a, b, c);
            }
        };
    }
    
    /**
     * Wraps the {@link Fn1} in a {@link F}.
     */
    public static <A, B> F<A, B> toF(final Fn1<A, B> f) {
        return new F<A, B>() {
            public B f(A a) {
                return f.call(a);
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in a {@link F2}.
     */
    public static <A, B, C> F2<A, B, C> toF2(final Fn2<A, B, C> f) {
        return new F2<A, B, C>() {
            public C f(A a, B b) {
                return f.call(a, b);
            }
        };
    }

    /**
     * Wraps the {@link Fn3} in a {@link F3}.
     */
    public static <A, B, C, D> F3<A, B, C, D> toF3(final Fn3<A, B, C, D> f) {
        return new F3<A, B, C, D>() {
            public D f(A a, B b, C c) {
                return f.call(a, b, c);
            }
        };
    }
}
