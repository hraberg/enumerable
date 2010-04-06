package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn2DDtoD extends Fn2DDtoX<Double> {
    public abstract int call(int a1, int a2);

    public Double call(Double a1, Double a2) {
        return call(a1.doubleValue(), a2.doubleValue());
    }
}
