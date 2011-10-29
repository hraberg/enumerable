package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn2;

@SuppressWarnings("serial")
public abstract class Fn2LLtoO<R> extends Fn2<Long, Long, R> {
    public abstract R call(long a1, long a2);

    public R call(Long a1, Long a2) {
        return call(a1.longValue(), a2.longValue());
    }
}
