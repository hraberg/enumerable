package lambda.clojure;

import static clojure.lang.RT.*;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.Var;

/**
 * This is class is similar {@link lambda.Lambda}, but instead of creating
 * lambdas inheriting from {@link lambda.Fn0} it creates lambdas implementing
 * the {@link clojure.lang.IFn}, and can used together with {@link ClojureSeqs}.
 */
@SuppressWarnings("serial")
public class LambdaClojure {
    public static Var defn(String name, IFn body) throws Exception {
        return var(CURRENT_NS.ns.name.getName(), name, body);
    }

    public static abstract class AFn1<R> extends AFn {
        public abstract R invoke(Object arg1) throws Exception;
    }

    public static abstract class AFn2<R> extends AFn {
        public abstract R invoke(Object arg1, Object arg2) throws Exception;
    }

    public static abstract class AFn3<R> extends AFn {
        public abstract R invoke(Object arg1, Object arg2, Object arg3) throws Exception;
    }

    @NewLambda
    public static <A1, R> AFn1<R> fn(A1 a1, R fn) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A1, A2, R> AFn2<R> fn(A1 a1, A2 a2, R fn) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A1, A2, A3, R> AFn3<R> fn(A1 a1, A2 a2, A3 a3, R fn) {
        throw new LambdaWeavingNotEnabledException();
    }
}