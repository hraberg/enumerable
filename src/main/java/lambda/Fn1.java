package lambda;

public abstract class Fn1<A, R> extends Fn0<R> {
    public abstract R call(A a1);

    public R call() {
        return call(null);
    }
}
