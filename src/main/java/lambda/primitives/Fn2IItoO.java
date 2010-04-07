package lambda.primitives;

import lambda.Fn2;

@SuppressWarnings("serial")
public abstract class Fn2IItoO<R> extends Fn2<Integer, Integer, R> {
    public abstract R call(int a1, int a2);

    public R call(Integer a1, Integer a2) {
        return call(a1.intValue(), a2.intValue());
    }
}
