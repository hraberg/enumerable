package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1ItoD extends Fn1ItoX<Double> {
    public abstract double call(int a1);

    public Double call(Integer a1) {
        return call(a1.intValue());
    }
}
