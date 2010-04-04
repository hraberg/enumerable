package lambda;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A function that takes no arguments.
 */
public class Fn0<R> {
    public R call() {
        return null;
    }

    /**
     * Applies args to this function, padded with null if needed to match the
     * number of arguments this function takes.
     */
    public R apply(Object... args) {
        return call();
    }

    /**
     * Wraps this function in an interface using a {@link Proxy}, and forwards
     * all calls to {@link #apply(Object...)}.
     */
    @SuppressWarnings("unchecked")
    public <I> I as(Class<I> anInterface) {
        return (I) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return apply(args != null ? args : new Object[0]);
            }
        });
    }
}
