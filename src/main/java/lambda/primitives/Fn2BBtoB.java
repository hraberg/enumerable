package lambda.primitives;

import lambda.Fn2;

@SuppressWarnings("serial")
public abstract class Fn2BBtoB extends Fn2<Boolean, Boolean, Boolean> {
    public abstract boolean call(boolean a1, boolean a2);

    public Boolean call(Boolean a1, Boolean a2) {
        return call(a1.booleanValue(), a2.booleanValue());
    }
}
