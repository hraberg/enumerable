package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1LtoB extends Fn1<Long, Boolean> {
    public abstract boolean call(long a1);

    public Boolean call(Long a1) {
        return call(a1.longValue());
    }
}
