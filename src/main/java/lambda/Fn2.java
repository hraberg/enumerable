package lambda;

public abstract class Fn2<A1, A2, R> extends Fn1<A1, R> {
    public abstract R call(A1 a1, A2 a2);

    public R call(A1 a1) {
        return call(a1, null);
    }
}