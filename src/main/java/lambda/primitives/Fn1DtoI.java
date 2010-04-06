package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1DtoI extends Fn1DtoX<Integer> {
    public abstract int call(double a1);

    public Integer call(Double a1) {
        return call(a1.doubleValue());
    }
}
