package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1DtoD extends Fn1DtoX<Double> {
    public abstract double call(double a1);

    public Double call(Double a1) {
        return call(a1.doubleValue());
    }
}
