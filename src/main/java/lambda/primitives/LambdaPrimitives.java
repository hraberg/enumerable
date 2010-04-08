package lambda.primitives;

import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;

public class LambdaPrimitives {
    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1BtoB fn(boolean a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoB fn(double a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoD fn(double a1, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static Fn2DDtoD fn(double a1, double a2, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static <R> Fn2DDtoO<R> fn(double a1, double a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoI fn(double a1, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoL fn(double a1, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static <R> Fn1DtoO<R> fn(double a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoB fn(int a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoD fn(int a1, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoI fn(int a1, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static Fn2IItoI fn(int a1, int a2, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static <R> Fn2IItoO<R> fn(int a1, int a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoL fn(int a1, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static <R> Fn1ItoO<R> fn(int a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoB fn(long a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoD fn(long a1, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoI fn(long a1, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoL fn(long a1, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static Fn2LLtoL fn(long a1, long a2, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static <R> Fn2LLtoO<R> fn(long a1, long a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static <R> Fn1LtoO<R> fn(long a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1BtoB λ(boolean a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();

    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoB λ(double a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoD λ(double a1, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static Fn2DDtoD λ(double a1, double a2, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static <R> Fn2DDtoO<R> λ(double a1, double a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoI λ(double a1, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1DtoL λ(double a1, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static <R> Fn1DtoO<R> λ(double a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoB λ(int a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoD λ(int a1, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoI λ(int a1, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static Fn2IItoI λ(int a1, int a2, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static <R> Fn2IItoO<R> λ(int a1, int a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1ItoL λ(int a1, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static <R> Fn1ItoO<R> λ(int a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoB λ(long a1, boolean block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoD λ(long a1, double block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoI λ(long a1, int block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static Fn1LtoL λ(long a1, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static Fn2LLtoL λ(long a1, long a2, long block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object, Object)
     */
    @NewLambda
    public static <R> Fn2LLtoO<R> λ(long a1, long a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * @see lambda.Lambda#fn(Object, Object)
     */
    @NewLambda
    public static <R> Fn1LtoO<R> λ(long a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }
}