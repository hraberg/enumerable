package lambda;

/**
 * A function that takes three arguments.
 */
public abstract class Fn3<A1, A2, A3, R> extends Fn2<A1, A2, R> {
    public abstract R call(A1 a1, A2 a2, A3 a3);

    public R call(A1 a1, A2 a2) {
        return call(a1, a2, null);
    }

    @SuppressWarnings("unchecked")
    public R apply(Object... args) {
        if (args.length > 2)
            return call((A1) args[0], (A2) args[1], (A3) args[2]);
        return super.apply(args);
    }

    /**
     * Partial application, returns a {@link Fn2} which calls this function with
     * a1 as it's first argument.
     */
    public Fn2<A2, A3, R> partial(final A1 a1) {
        return new Fn2<A2, A3, R>() {
            public R call(A2 a2, A3 a3) {
                return Fn3.this.call(a1, a2, a3);
            }
        };
    }
}