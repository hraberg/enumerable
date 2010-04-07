package lambda.primitives;

import lambda.Fn2;

@SuppressWarnings("serial")
public abstract class Fn2LLtoL extends Fn2<Long, Long, Long> {
    public abstract long call(long a1, long a2);

    public Long call(Long a1, Long a2) {
        return call(a1.longValue(), a2.longValue());
    }
}
