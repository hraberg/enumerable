package lambda;

import static java.util.Collections.*;
import static lambda.exception.UncheckedException.*;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import lambda.annotation.LambdaLocal;

/**
 * A function that takes no arguments.
 */
@SuppressWarnings("serial")
public abstract class Fn0<R> implements Serializable {
    public final int arity = 0;

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
            if (!pattern.matcher(method.getName()).matches())
                return null;

            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (args[i] != null && !type.isAssignableFrom(args[i].getClass()))
                    return null;
            }
            return apply(args != null ? args : new Object[0]);
        }
    }

    /**
     * Returns this functions execution context as an unmodifiable map. Contains
     * captured local variables (including the outer this) and parameters.
     */
    public Map<String, Object> binding() {
        try {
            Map<String, Object> binding = new HashMap<String, Object>();
            for (Field field : getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(LambdaLocal.class)) {
                    LambdaLocal lambdaLocal = field.getAnnotation(LambdaLocal.class);

                    Object value = field.get(this);
                    if (!lambdaLocal.isReadOnly())
                        value = Array.get(value, 0);
                    binding.put(lambdaLocal.name(), value);
                }
            }
            return unmodifiableMap(binding);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }
}
