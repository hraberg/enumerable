package lambda.support.groovy;

import groovy.lang.Closure;
import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import sun.org.mozilla.javascript.internal.Function;

/**
 * This is class is similar {@link lambda.Lambda}, but instead of creating
 * lambdas inheriting from {@link lambda.Fn0} it creates lambdas extending
 * {@link Function} to be used together with Groovy.
 */
@SuppressWarnings("serial")
public class LambdaGroovy {
    public static abstract class ClosureFn0 extends Closure {
        public ClosureFn0() {
            super(LambdaGroovy.class);
            Fn0.getAndCheckArityForMethod(getImplementingClass(), getMethod());
        }

        Class<?> getImplementingClass() {
            return getClass();
        }

        String getMethod() {
            return "doCall";
        }

        public abstract Object doCall();
    }

    public static abstract class ClosureFn1 extends ClosureFn0 {
        public Object doCall() {
            return doCall(default$1());
        }

        protected Object default$1() {
            return null;
        }

        public abstract Object doCall(Object a1);
    }

    public static abstract class ClosureFn2 extends ClosureFn1 {
        public Object doCall(Object a1) {
            return doCall(a1, default$2());
        }

        protected Object default$2() {
            return null;
        }

        public abstract Object doCall(Object a1, Object a2);
    }

    public static abstract class ClosureFn3 extends ClosureFn2 {
        public Object doCall(Object a1, Object a2) {
            return doCall(a1, a2, default$3());
        }

        protected Object default$3() {
            return null;
        }

        public abstract Object doCall(Object a1, Object a2, Object a3);
    }

    /**
     * Creates a new function implementing {@link Function} taking no arguments.
     */
    @NewLambda
    public static ClosureFn0 closure(Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new function implementing {@link Function} taking one argument.
     */
    @NewLambda
    public static ClosureFn1 closure(Object a1, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new function implementing {@link Function} taking two
     * arguments.
     */
    @NewLambda
    public static ClosureFn2 closure(Object a1, Object a2, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new function implementing {@link Function} taking three
     * arguments.
     */
    @NewLambda
    public static ClosureFn3 closure(Object a1, Object a2, Object a3, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link Function} in a {@link Fn0}.
     */
    public static Fn0<Object> toFn0(final Closure closure) {
        return new Fn0<Object>() {
            public Object call() {
                return closure.call();
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn1}.
     */
    public static Fn1<Object, Object> toFn1(final Closure closure) {
        return new Fn1<Object, Object>() {
            public Object call(Object a1) {
                return closure.call(new Object[] { a1 });
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn2}.
     */
    public static Fn2<Object, Object, Object> toFn2(final Closure closure) {
        return new Fn2<Object, Object, Object>() {
            public Object call(Object a1, Object a2) {
                return closure.call(new Object[] { a1, a2 });
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn3}.
     */
    public static Fn3<Object, Object, Object, Object> toFn3(final Closure closure) {
        return new Fn3<Object, Object, Object, Object>() {
            public Object call(Object a1, Object a2, Object a3) {
                return closure.call(new Object[] { a1, a2, a3 });
            }
        };
    }

    /**
     * Wraps the {@link Fn0} in a {@link Closure}.
     */
    @SuppressWarnings("rawtypes")
    public static ClosureFn0 toClosure(final Fn0 fn) {
        return new ClosureFn0() {
            public Object doCall() {
                return fn.call();
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link Closure}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ClosureFn1 toClosure(final Fn1 fn) {
        return new ClosureFn1() {
            public Object doCall() {
                return fn.call();
            }

            public Object doCall(Object a1) {
                return fn.call(a1);
            }

            Class<?> getImplementingClass() {
                return fn.getClass();
            }

            String getMethod() {
                return "call";
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in a {@link Closure}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ClosureFn2 toClosure(final Fn2 fn) {
        return new ClosureFn2() {
            public Object doCall() {
                return fn.call();
            }

            public Object doCall(Object a1) {
                return fn.call(a1);
            }

            public Object doCall(Object a1, Object a2) {
                return fn.call(a1, a2);
            }

            Class<?> getImplementingClass() {
                return fn.getClass();
            }

            String getMethod() {
                return "call";
            }
        };
    }

    /**
     * Wraps the {@link Fn3} in a {@link Closure}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ClosureFn3 toClosure(final Fn3 fn) {
        return new ClosureFn3() {
            public Object doCall() {
                return fn.call();
            }

            public Object doCall(Object a1) {
                return fn.call(a1);
            }

            public Object doCall(Object a1, Object a2) {
                return fn.call(a1, a2);
            }

            public Object doCall(Object a1, Object a2, Object a3) {
                return fn.call(a1, a2, a3);
            }

            Class<?> getImplementingClass() {
                return fn.getClass();
            }

            String getMethod() {
                return "call";
            }
        };
    }
}
