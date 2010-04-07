package lambda.primitives;

import lambda.Fn1;

@SuppressWarnings("serial")
public abstract class Fn1ItoL extends Fn1<Integer, Long> {
    public abstract long call(int a1);

    public Long call(Integer a1) {
        return call(a1.intValue());
    }
}
