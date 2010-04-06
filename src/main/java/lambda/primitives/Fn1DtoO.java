package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1DtoO<R> extends Fn1DtoX<R> {
    public abstract R call(double a1);

    public R call(Double a1) {
        return call(a1.doubleValue());
    }
}
