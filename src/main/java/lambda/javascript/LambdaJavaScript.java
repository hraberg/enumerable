package lambda.javascript;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import sun.org.mozilla.javascript.internal.BaseFunction;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 * This is class is similar {@link lambda.Lambda}, but instead of creating
 * lambdas inheriting from {@link lambda.Fn0} it creates lambdas extending
 * {@link Function} to be used together with Rhino.
 */
@SuppressWarnings("serial")
public class LambdaJavaScript {
    public static abstract class BaseFunctionFn extends BaseFunction {
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            if (args.length != getArity())
                throw new IllegalArgumentException(
                        "Tried to call JavaScript BaseFunctionFn with wrong number of arguments, " + args.length
                                + " for " + getArity());

            Object result = null;
            if (args.length == 0)
                result = call();

            else if (args.length == 1)
                result = call(args[0]);

            else if (args.length == 2)
                result = call(args[0], args[1]);

            else if (args.length == 3)
                result = call(args[0], args[1], args[2]);

            if (result instanceof Number)
                return Context.toNumber(result);

            return Context.javaToJS(result, scope);
        }

        protected Object call() {
            throw new UnsupportedOperationException();
        }

        protected Object call(Object a1) {
            throw new UnsupportedOperationException();
        }

        protected Object call(Object a1, Object a2) {
            throw new UnsupportedOperationException();
        }

        protected Object call(Object a1, Object a2, Object a3) {
            throw new UnsupportedOperationException();
        }
    }

    public static abstract class FunctionFn0 extends BaseFunctionFn {
        public abstract Object call();

        public int getArity() {
            return 0;
        }
    }

    public static abstract class FunctionFn1 extends BaseFunctionFn {
        public abstract Object call(Object a1);

        public int getArity() {
            return 1;
        }
    }

    public static abstract class FunctionFn2 extends BaseFunctionFn {
        public abstract Object call(Object a1, Object a2);

        public int getArity() {
            return 2;
        }
    }

    public static abstract class FunctionFn3 extends BaseFunctionFn {
        public abstract Object call(Object a1, Object a2, Object a3);

        public int getArity() {
            return 3;
        }
    }

    /**
     * Creates a new function implementing {@link Function} taking no arguments.
     */
    @NewLambda
    public static FunctionFn0 function(Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new function implementing {@link Function} taking one argument.
     */
    @NewLambda
    public static FunctionFn1 function(Object a1, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new function implementing {@link Function} taking two
     * arguments.
     */
    @NewLambda
    public static FunctionFn2 function(Object a1, Object a2, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new function implementing {@link Function} taking three
     * arguments.
     */
    @NewLambda
    public static FunctionFn3 function(Object a1, Object a2, Object a3, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link Function} in a {@link Fn0}.
     */
    public static Fn0<Object> toFn0(final Function function) {
        return new Fn0<Object>() {
            public Object call() {
                Context.enter();
                try {
                    return function.call(Context.getCurrentContext(), function.getParentScope(), function
                            .getParentScope(), new Object[0]);
                } finally {
                    Context.exit();
                }
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn1}.
     */
    public static Fn1<Object, Object> toFn1(final Function function) {
        return new Fn1<Object, Object>() {
            public Object call(Object a1) {
                Context.enter();
                try {
                    return function.call(Context.getCurrentContext(), function.getParentScope(), function
                            .getParentScope(), new Object[] { a1 });
                } finally {
                    Context.exit();
                }
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn2}.
     */
    public static Fn2<Object, Object, Object> toFn2(final Function function) {
        return new Fn2<Object, Object, Object>() {
            public Object call(Object a1, Object a2) {
                Context.enter();
                try {
                    return function.call(Context.getCurrentContext(), function.getParentScope(), function
                            .getParentScope(), new Object[] { a1, a2 });
                } finally {
                    Context.exit();
                }
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn3}.
     */
    public static Fn3<Object, Object, Object, Object> toFn3(final Function function) {
        return new Fn3<Object, Object, Object, Object>() {
            public Object call(Object a1, Object a2, Object a3) {
                Context.enter();
                try {
                    return function.call(Context.getCurrentContext(), function.getParentScope(), function
                            .getParentScope(), new Object[] { a1, a2, a3 });
                } finally {
                    Context.exit();
                }
            }
        };
    }

    /**
     * Wraps the {@link Fn0} in a {@link Function}.
     */
    @SuppressWarnings("rawtypes")
    public static FunctionFn0 toFunction(final Fn0 fn) {
        return new FunctionFn0() {
            public Object call() {
                return fn.call();
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link Function}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static FunctionFn1 toFunction(final Fn1 fn) {
        return new FunctionFn1() {
            public Object call(Object a1) {
                return fn.call(a1);
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in a {@link Function}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static FunctionFn2 toFunction(final Fn2 fn) {
        return new FunctionFn2() {
            public Object call(Object a1, Object a2) {
                return fn.call(a1, a2);
            }
        };
    }

    /**
     * Wraps the {@link Fn3} in a {@link Function}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Function toFunction(final Fn3 fn) {
        return new FunctionFn3() {
            public Object call(Object a1, Object a2, Object a3) {
                return fn.call(a1, a2, a3);
            }
        };
    }
}