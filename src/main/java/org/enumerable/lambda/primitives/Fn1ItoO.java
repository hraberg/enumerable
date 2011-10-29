package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1ItoO<R> extends Fn1<Integer, R> {
    public abstract R call(int a1);

    public R call(Integer a1) {
        return call(a1.intValue());
    }
}
