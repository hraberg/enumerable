package lambda.primitives;

import lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1DtoB extends Fn1<Double, Boolean> {
    public abstract boolean call(double a1);

    public Boolean call(Double a1) {
        return call(a1.doubleValue());
    }
}
