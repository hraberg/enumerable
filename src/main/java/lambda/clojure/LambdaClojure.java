package lambda.clojure;

import static clojure.lang.Compiler.*;
import static clojure.lang.RT.*;
import static java.util.Arrays.*;
import static lambda.exception.UncheckedException.*;

import java.io.StringReader;

import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LispReader;
import clojure.lang.PersistentList;
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
        ClojureSeqs.init();
    }

    /**
     * Defines a {@link IFn} from a lambda in the current namespace, like this:
     * 
     * <pre>
     * Var square = defn(&quot;square&quot;, fn(n, n * n));
     * </pre>
     */
    public static Var defn(String name, IFn body) {
        try {
            return var(CURRENT_NS.get().toString(), name, body);
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
     * @see #eval(Object...)
     */
    @SuppressWarnings("unchecked")
    public static <R> R _(Object... forms) {
        return (R) eval(forms);
    }

    /**
     * Evaluates the code using {@link clojure.lang.Compiler}. s
     */
    @SuppressWarnings("unchecked")
    public static <R> R eval(String code) {
        try {
            return (R) load(new StringReader(code));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * @see #eval(String)
     */
    @SuppressWarnings("unchecked")
    public static <R> R _(String code) {
        try {
            return (R) eval(code);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static abstract class AFn0<R> extends AFunction {
        public abstract R invoke() throws Exception;
    }

    public static abstract class AFn1<R> extends AFunction {
        public abstract R invoke(Object arg1) throws Exception;
    }

    public static abstract class AFn2<R> extends AFunction {
        public abstract R invoke(Object arg1, Object arg2) throws Exception;
    }

    public static abstract class AFn3<R> extends AFunction {
        public abstract R invoke(Object arg1, Object arg2, Object arg3) throws Exception;
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking no arguments.
     */
    @NewLambda
    public static <R> AFn0<R> fn(R body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking one argument.
     */
    @NewLambda
    public static <A1, R> AFn1<R> fn(A1 a1, R body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking two arguments.
     */
    @NewLambda
    public static <A1, A2, R> AFn2<R> fn(A1 a1, A2 a2, R body) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda implementing {@link IFn} taking three arguments.
     */
    @NewLambda
    public static <A1, A2, A3, R> AFn3<R> fn(A1 a1, A2 a2, A3 a3, R body) {
        throw new LambdaWeavingNotEnabledException();
    }
}
