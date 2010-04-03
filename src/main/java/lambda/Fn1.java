package lambda;

/**
 * A function that takes one argument.
 */
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

    /**
     * Partial application, returns a {@link Fn0} which calls this function with
     * a1.
     */
    public Fn0<R> partial(final A1 a1) {
        return new Fn0<R>() {
            public R call() {
                return Fn1.this.call(a1);
            }
        };
    }

    /**
     * Function composition, returns a {@link Fn1} that calls this function with
     * the result of f called with a1.
     */
    public <X> Fn1<X, R> compose(final Fn1<X, ? extends A1> f) {
        return new Fn1<X, R>() {
            public R call(X a1) {
                return Fn1.this.call(f.call(a1));
            }
        };
    }

    /**
     * Function composition, returns a {@link Fn2} that calls this function with
     * the result of f called with a1 and a2.
     */
    public <X, Y> Fn2<X, Y, R> compose(final Fn2<X, Y, ? extends A1> f) {
        return new Fn2<X, Y, R>() {
            public R call(X a1, Y a2) {
                return Fn1.this.call(f.call(a1, a2));
            }
        };
    }

    /**
     * Function composition, returns a {@link Fn3} that calls this function with
     * the result of f called with a1, a2 and a3.
     */
    public <X, Y, Z> Fn3<X, Y, Z, R> compose(final Fn3<X, Y, Z, ? extends A1> f) {
        return new Fn3<X, Y, Z, R>() {
            public R call(X a1, Y a2, Z a3) {
                return Fn1.this.call(f.call(a1, a2, a3));
            }
        };
    }
}
