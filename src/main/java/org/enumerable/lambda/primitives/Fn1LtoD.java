package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1LtoD extends Fn1<Long, Double> {
    public abstract double call(long a1);

    public Double call(Long a1) {
        return call(a1.longValue());
    }
}
