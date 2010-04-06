package lambda;

@SuppressWarnings("serial")
public abstract class Fn1DToD extends Fn1<Double, Double> {
    public abstract double call(double a1);

    public Double call(Double a1) {
        return call(a1.doubleValue());
    }
}
