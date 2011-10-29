package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1LtoL extends Fn1<Long, Long> {
    public abstract long call(long a1);

    public Long call(Long a1) {
        return call(a1.longValue());
    }
}
