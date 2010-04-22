package lambda;

/**
 * A function that takes two arguments.
 */
@SuppressWarnings("serial")
public abstract class Fn2<A1, A2, R> extends Fn1<A1, R> {
    public final int arity = 2;

    public abstract R call(A1 a1, A2 a2);

    private A2 default$2;

    public R call(A1 a1) {
        return call(a1, default$2 == null ? default$2 = default$2() : default$2);
    }

    protected A2 default$2() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public R apply(Object... args) {
        if (args.length > 1)
            return call((A1) args[0], (A2) args[1]);
        return super.apply(args);
    }

    /**
     * Partial application, returns a {@link Fn1} which calls this function with
     * a1 as the first argument.
     */
    public Fn1<A2, R> partial(final A1 a1) {
        return new Fn1<A2, R>() {
            public R call(A2 a2) {
                return Fn2.this.call(a1, a2);
            }
        };
    }

    public Fn2<A1, A2, Boolean> complement() {
        return new Fn2<A1, A2, Boolean>() {
            public Boolean call(A1 a1, A2 a2) {
                return isFalseOrNull(Fn2.this.call(a1, a2));
            }
        };
    }
}