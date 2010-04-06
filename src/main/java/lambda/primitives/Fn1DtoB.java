package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1DtoB extends Fn1ItoX<Boolean> {
    public abstract boolean call(double a1);

    public Boolean call(Double a1) {
        return call(a1.doubleValue());
    }
}
