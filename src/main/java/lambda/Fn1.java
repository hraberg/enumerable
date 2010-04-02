package lambda;

public abstract class Fn1<A1, R> extends Fn0<R> {
    public abstract R call(A1 a1);

    public R call() {
        return call(null);
    }

    @SuppressWarnings("unchecked")
    public R apply(Object... args) {
        if (args.length > 0)
            return call((A1) args[0]);
        return super.apply(args);
    }

    public Fn0<R> partial(final A1 a1) {
        return new Fn0<R>() {
            public R call() {
                return Fn1.this.call(a1);
            }
        };
    }
}
