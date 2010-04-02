package lambda;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

public class Lambda {
    @LambdaParameter
    public static Integer n;
    @LambdaParameter
    public static Integer m;
    @LambdaParameter
    public static Integer i;
    @LambdaParameter
    public static Integer idx;
    @LambdaParameter
    public static String s;
    @LambdaParameter
    public static String t;
    @LambdaParameter
    public static Double d;
    @LambdaParameter
    public static Character c;
    @LambdaParameter
    public static Collection<?> col;
    @LambdaParameter
    public static Object obj;
    @LambdaParameter
    public static Object _;

    @NewLambda
    public static <A1, R> Fn1<A1, R> λ(A1 a1, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, R> Fn1<A1, R> lambda(A1 a1, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, R> Fn1<A1, R> fn(A1 a1, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> λ(A1 a1, A2 a2, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> lambda(A1 a1, A2 a2, R block) {
        throw new UnsupportedOperationException();
    }

    @NewLambda
    public static <A1, A2, R> Fn2<A1, A2, R> fn(A1 a1, A2 a2, R block) {
        throw new UnsupportedOperationException();
    }

    public static <A1, A2, R> Fn1<A2, R> partial(final Fn2<A1, A2, R> f, final A1 a1) {
        return new Fn1<A2, R>() {
            public R call(A2 a2) {
                return f.call(a1, a2);
            }
        };
    }

    public static <A1, R> Fn0<R> partial(final Fn1<A1, R> f, final A1 a1) {
        return new Fn0<R>() {
            public R call() {
                return f.call(a1);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <I> I as(Class<I> anInterface, final Fn2 f) {
        return (I) Proxy.newProxyInstance(Lambda.class.getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return f.call(args[0], args[1]);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <I> I as(Class<I> anInterface, final Fn1 f) {
        return (I) Proxy.newProxyInstance(Lambda.class.getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (args == null)
                    return f.call();
                return f.call(args[0]);
            }
        });
    }
}
