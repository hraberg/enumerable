package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1ItoI extends Fn1ItoX<Integer> {
    public abstract int call(int a1);

    public Integer call(Integer a1) {
        return call(a1.intValue());
    }
}
