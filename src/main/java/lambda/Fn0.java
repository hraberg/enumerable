package lambda;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Pattern;

/**
 * A function that takes no arguments.
 */
@SuppressWarnings("serial")
public abstract class Fn0<R> implements Serializable {
    public abstract R call();

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
        return (I) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { anInterface },
                new ApplyMethodInvocationHandler(".*", new Class[0]));
    }

    /**
     * Wraps this function in an interface using a {@link Proxy}, and forwards
     * calls with names matching the regular expression and arguments matching
     * the parameter types to {@link #apply(Object...)}.
     */
    @SuppressWarnings("unchecked")
    public <I> I as(Class<I> anInterface, String regex, Class... parameterTypes) {
        return (I) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { anInterface },
                new ApplyMethodInvocationHandler(regex, parameterTypes));
    }

    class ApplyMethodInvocationHandler implements InvocationHandler {
        Pattern pattern;
        Class<?>[] parameterTypes;

        ApplyMethodInvocationHandler(String regex, Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
            this.pattern = Pattern.compile(regex);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (pattern.matcher(method.getName()).matches()) {
                int i = 0;
                for (Class<?> type : parameterTypes)
                    if (args[i] != null && !type.isAssignableFrom(args[i++].getClass()))
                        return null;
                return apply(args != null ? args : new Object[0]);
            }
            return null;
        }
    }
}
