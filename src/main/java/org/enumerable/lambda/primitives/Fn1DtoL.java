package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1DtoL extends Fn1<Double, Long> {
    public abstract long call(double a1);

    public Long call(Double a1) {
        return call(a1.doubleValue());
    }
}
