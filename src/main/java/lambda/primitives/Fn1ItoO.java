package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1ItoO<R> extends Fn1ItoX<R> {
    public abstract R call(int a1);

    public R call(Integer a1) {
        return call(a1.intValue());
    }
}
