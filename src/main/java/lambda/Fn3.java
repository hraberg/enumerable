package lambda;

/**
 * A function that takes three arguments.
 */
@SuppressWarnings("serial")
public abstract class Fn3<A1, A2, A3, R> extends Fn2<A1, A2, R> {
    public final int arity = 3;

    public abstract R call(A1 a1, A2 a2, A3 a3);

    protected A3 default$3;

    public R call(A1 a1, A2 a2) {
        return call(a1, a2, default$3 == null ? default$3 = default$3() : default$3);
    }

    protected A3 default$3() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public R apply(Object... args) {
        if (args.length >= arity)
            return call((A1) args[0], (A2) args[1], (A3) args[2]);
        return super.apply(args);
    }

    /**
     * Partial application, returns a {@link Fn2} which calls this function with
     * a1 as the first argument.
     */
    public Fn2<A2, A3, R> partial(final A1 a1) {
        return new Fn2<A2, A3, R>() {
            public R call(A2 a2, A3 a3) {
                return Fn3.this.call(a1, a2, a3);
            }
        };
    }

    /**
     * Currying, returns a {@link Fn1} which takes a1 as its argument and
     * returns another Fn1 which takes a2, which returns another Fn1 which takes
     * a3 which invokes this function with a1, a2 and a3 when called.
     */
    public Fn1<A1, Fn1<A2, Fn1<A3, R>>> curry3() {
        return new Fn1<A1, Fn1<A2, Fn1<A3, R>>>() {
            public Fn1<A2, Fn1<A3, R>> call(final A1 a1) {
                return new Fn1<A2, Fn1<A3, R>>() {
                    public Fn1<A3, R> call(final A2 a2) {
                        return new Fn1<A3, R>() {
                            public R call(A3 a3) {
                                return Fn3.this.call(a1, a2, a3);
                            }
                        };
                    }
                };
            }
        };
    }

    public Fn3<A1, A2, A3, Boolean> complement() {
        return new Fn3<A1, A2, A3, Boolean>() {
            public Boolean call(A1 a1, A2 a2, A3 a3) {
                return isFalseOrNull(Fn3.this.call(a1, a2, a3));
            }
        };
    }
}