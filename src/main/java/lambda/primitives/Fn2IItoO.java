package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn2IItoO<R> extends Fn2IItoX<R> {
    public abstract R call(int a1, int a2);

    public R call(Integer a1, Integer a2) {
        return call(a1.intValue(), a2.intValue());
    }
}
