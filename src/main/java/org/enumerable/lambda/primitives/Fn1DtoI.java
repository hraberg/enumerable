package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1DtoI extends Fn1<Double, Integer> {
    public abstract int call(double a1);

    public Integer call(Double a1) {
        return call(a1.doubleValue());
    }
}
