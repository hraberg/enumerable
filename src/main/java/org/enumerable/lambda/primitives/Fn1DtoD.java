package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1DtoD extends Fn1<Double, Double> {
    public abstract double call(double a1);

    public Double call(Double a1) {
        return call(a1.doubleValue());
    }
}
