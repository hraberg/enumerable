package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1ItoI extends Fn1<Integer, Integer> {
    public abstract int call(int a1);

    public Integer call(Integer a1) {
        return call(a1.intValue());
    }
}
