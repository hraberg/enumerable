package lambda.primitives;

import lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1LtoO<R> extends Fn1<Long, R> {
    public abstract R call(long a1);

    public R call(Long a1) {
        return call(a1.longValue());
    }
}
