package lambda.clojure;

import static clojure.lang.Compiler.*;
import static clojure.lang.RT.*;
import static java.util.Arrays.*;
import static lambda.exception.UncheckedException.*;

import java.io.StringReader;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LispReader;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 * This is class is similar {@link lambda.Lambda}, but instead of creating
 * lambdas inheriting from {@link lambda.Fn0} it creates lambdas implementing
 * the {@link clojure.lang.IFn} interface, and can used together with
 * {@link ClojureSeqs} and {@link clojure.lang.RT}.
 */
@SuppressWarnings("serial")
public class LambdaClojure {
    static {
        init();
    }

    static void init() {
        try {
            if (CURRENT_NS.get() == CLOJURE_NS)
                CURRENT_NS.doReset(Namespace.findOrCreate(Symbol.create("user")));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * Defines a {@link IFn} from a lambda in the current namespace, like this:
     * 
     * <pre>
     * Var square = defn(&quot;square&quot;, fn(n, n * n));
     * </pre>
     */
    public static Var defn(String name, IFn body) {
        return def(name, body);
    }

    /**
     * Defines a var in the current namespace.
     */
    public static Var def(String name, Object value) {
        try {
            return var(CURRENT_NS.get().toString(), name, value);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * Turns the array of forms into a {@link PersistentList} and evaluates it
     * using {@link clojure.lang.Compiler}.
     * <p>
     * The objects must be forms Clojure understands, basically anything the
     * {@link LispReader} can produce.
     * <p>
     * Note that if you send in an {@link IFn} which is not a {@link Var}, the
     * compiler will create a new instance of it, which may not be what you
     * want. If you define your lambdas with {@link #defn(String, IFn)} it
     * returns a {@link Var} which can be used here.
     */
    @SuppressWarnings("unchecked")
    public static <R> R eval(Object... forms) {
        try {
            return (R) clojure.lang.Compiler.eval(PersistentList.create(asList(forms)));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * Evaluates the code using {@link clojure.lang.Compiler}.
     */
    @SuppressWarnings("unchecked")
    public static <R> R eval(String code) {
        try {
            return (R) load(new StringReader(code));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    static abstract class AFnFnBase extends AFunction {
        int arity;

        public AFnFnBase() {
            arity = Fn0.getAndCheckArityForMethod(getImplementingClass(), getMethod());
        }

        String getMethod() {
            return "invoke";
        }

        Class<?> getImplementingClass() {
            return getClass();
        }
    }

    public static abstract class AFn0 extends AFnFnBase {
        public abstract Object invoke() throws Exception;
    }

    public static abstract class AFn1 extends AFn0 {
        public Object invoke() throws Exception {
            return invoke(default$1());
        }

        protected Object default$1() {
            throwArity();
            return null;
        }

        public abstract Object invoke(Object arg1) throws Exception;
    }

    public static abstract class AFn2 extends AFn1 {
        public Object invoke(Object arg1) throws Exception {
            return invoke(arg1, default$2());
        }

        protected Object default$2() {
            throwArity();
            return null;
        }

        public abstract Object invoke(Object arg1, Object arg2) throws Exception;
    }

    public static abstract class AFn3 extends AFn2 {
        public Object invoke(Object arg1, Object arg2) throws Exception {
            return invoke(arg1, arg2, default$3());
        }

        protected Object default$3() {
            throwArity();
            return null;
        }

        public abstract Object invoke(Object arg1, Object arg2, Object arg3) throws Exception;
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking no arguments.
     */
    @NewLambda
    public static AFn0 fn(Object body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking one argument.
     */
    @NewLambda
    public static AFn1 fn(Object a1, Object body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking two arguments.
     */
    @NewLambda
    public static AFn2 fn(Object a1, Object a2, Object body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking three arguments.
     */
    @NewLambda
    public static AFn3 fn(Object a1, Object a2, Object a3, Object body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link IFn} in a {@link Fn0}.
     */
    public static Fn0<Object> toFn0(final IFn f) {
        return new Fn0<Object>() {
            public Object call() {
                try {
                    return f.invoke();
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link IFn} in a {@link Fn1}.
     */
    public static Fn1<Object, Object> toFn1(final IFn f) {
        return new Fn1<Object, Object>() {
            public Object call() {
                try {
                    return f.invoke();
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }

            public Object call(Object a1) {
                try {
                    return f.invoke(a1);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link IFn} in a {@link Fn2}.
     */
    public static Fn2<Object, Object, Object> toFn2(final IFn f) {
        return new Fn2<Object, Object, Object>() {
            public Object call() {
                try {
                    return f.invoke();
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }

            public Object call(Object a1) {
                try {
                    return f.invoke(a1);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }

            public Object call(Object a1, Object a2) {
                try {
                    return f.invoke(a1, a2);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link IFn} in a {@link Fn3}.
     */
    public static Fn3<Object, Object, Object, Object> toFn3(final IFn f) {
        return new Fn3<Object, Object, Object, Object>() {
            public Object call() {
                try {
                    return f.invoke();
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }

            public Object call(Object a1) {
                try {
                    return f.invoke(a1);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }

            public Object call(Object a1, Object a2) {
                try {
                    return f.invoke(a1, a2);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }

            public Object call(Object a1, Object a2, Object a3) {
                try {
                    return f.invoke(a1, a2, a3);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link Fn0} in an {@link IFn}.
     */
    @SuppressWarnings("rawtypes")
    public static IFn toIFn(final Fn0 f) {
        return new AFn0() {
            public Object invoke() throws Exception {
                return f.call();
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in an {@link IFn}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IFn toIFn(final Fn1 f) {
        return new AFn1() {
            public Object invoke() throws Exception {
                if (arity > -1)
                    throwArity();
                return f.call();
            }

            public Object invoke(Object arg1) throws Exception {
                return f.call(arg1);
            }

            Class<?> getImplementingClass() {
                return f.getClass();
            }

            String getMethod() {
                return "call";
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in an {@link IFn}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IFn toIFn(final Fn2 f) {
        return new AFn2() {
            public Object invoke() throws Exception {
                if (arity > -1)
                    throwArity();
                return f.call();
            }

            public Object invoke(Object arg1) throws Exception {
                if (arity > -2)
                    throwArity();
                return f.call(arg1);
            }

            public Object invoke(Object arg1, Object arg2) throws Exception {
                return f.call(arg1, arg2);
            }

            Class<?> getImplementingClass() {
                return f.getClass();
            }

            String getMethod() {
                return "call";
            }
        };
    }

    /**
     * Wraps the {@link Fn3} in an {@link IFn}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IFn toIFn(final Fn3 f) {
        return new AFn3() {
            public Object invoke() throws Exception {
                if (arity > -1)
                    throwArity();
                return f.call();
            }

            public Object invoke(Object arg1) throws Exception {
                if (arity > -2)
                    throwArity();
                return f.call(arg1);
            }

            public Object invoke(Object arg1, Object arg2) throws Exception {
                if (arity > -3)
                    throwArity();
                return f.call(arg1, arg2);
            }

            public Object invoke(Object arg1, Object arg2, Object arg3) throws Exception {
                return f.call(arg1, arg2, arg3);
            }

            Class<?> getImplementingClass() {
                return f.getClass();
            }

            String getMethod() {
                return "call";
            }
        };
    }
}
