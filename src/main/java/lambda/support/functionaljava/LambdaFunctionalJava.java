package lambda.support.functionaljava;

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
}
