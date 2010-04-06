package lambda.primitives;

import lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1DtoO<R> extends Fn1<Double, R> {
    public abstract R call(double a1);

    public R call(Double a1) {
        return call(a1.doubleValue());
    }
}
