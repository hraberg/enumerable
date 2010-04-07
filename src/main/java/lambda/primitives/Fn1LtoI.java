package lambda.primitives;

import lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1LtoI extends Fn1<Long, Integer> {
    public abstract int call(long a1);

    public Integer call(Long a1) {
        return call(a1.longValue());
    }
}
