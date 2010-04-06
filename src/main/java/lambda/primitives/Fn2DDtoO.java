package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn2DDtoO<R> extends Fn2DDtoX<R> {
    public abstract R call(double a1, double a2);

    public R call(Double a1, Double a2) {
        return call(a1.doubleValue(), a2.doubleValue());
    }
}
