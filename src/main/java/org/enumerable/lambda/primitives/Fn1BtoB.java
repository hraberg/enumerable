package org.enumerable.lambda.primitives;

import org.enumerable.lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1BtoB extends Fn1<Boolean, Boolean> {
    public abstract boolean call(boolean a1);

    public Boolean call(Boolean a1) {
        return call(a1.booleanValue());
    }
}
