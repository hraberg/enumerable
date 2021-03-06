package org.enumerable.lambda;

import org.enumerable.lambda.annotation.LambdaLocal;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static org.enumerable.lambda.exception.UncheckedException.uncheck;


/**
 * A function that takes no arguments.
 */
@SuppressWarnings("serial")
public abstract class Fn0<R> implements Serializable {
    /**
     * Creates a constant function always returning the provided value.
     */
    public static <R> Fn0<R> constant(final R value) {
        return new Fn0<R>() {
            public R call() {
                return value;
            }
        };
    }

    public static boolean isNotFalseOrNull(Object obj) {
        return obj != FALSE && obj != null;
    }

    public static boolean isFalseOrNull(Object result) {
        return !isNotFalseOrNull(result);
    }

    public static int getAndCheckArityForMethod(Class<?> aClass, String methodName) {
        int basicArity = 0;
        for (Method method : aClass.getDeclaredMethods())
            if (method.getName().equals(methodName))
                basicArity = method.getParameterTypes().length;

        SortedSet<Integer> defaultValues = new TreeSet<Integer>();
        for (Method method : aClass.getDeclaredMethods())
            if (method.getName().startsWith("default$"))
                defaultValues.add(Integer.valueOf(method.getName().substring("default$".length())));

        boolean consecutive = true;
        if (!defaultValues.isEmpty()) {
            int lastIndex = -1;
            for (int index : defaultValues) {
                if (lastIndex > 0)
                    consecutive = index == lastIndex + 1;
                lastIndex = index;
            }
            if (lastIndex != basicArity || !consecutive)
                throw new IllegalArgumentException("parameter " + lastIndex
                        + " cannot have a default value when there are parameters follwing without, arity is "
                        + basicArity);

            return -(basicArity - defaultValues.size() + 1);
        }

        return basicArity;
    }

    public Fn0() {
        getAndCheckArityForMethod(getClass(), "call");
    }

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
        return (I) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { anInterface },
                new ApplyMethodInvocationHandler(".*", new Class<?>[0]));
    }

    /**
     * Wraps this function in an interface using a {@link Proxy}, and forwards
     * calls with names matching the regular expression and arguments matching
     * the parameter types to {@link #apply(Object...)}.
     */
    @SuppressWarnings("unchecked")
    public <I> I as(Class<I> anInterface, String regex, Class<?>... parameterTypes) {
        return (I) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { anInterface },
                new ApplyMethodInvocationHandler(regex, parameterTypes));
    }

    public int arity() {
        return 0;
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

    public class Binding {
        /**
         * Gets the current value of a variable that was captured when creating
         * this lambda.
         */
        public Object get(String name) {
            try {
                Field field = findField(name);
                if (field == null)
                    return null;
                Object value = field.get(Fn0.this);
                if (!field.getAnnotation(LambdaLocal.class).isReadOnly())
                    value = Array.get(value, 0);
                return value;
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        /**
         * Sets the value of a variable that was captured and when creating this
         * lambda. Only works if the variable is changed somewhere in the
         * original method body or inside the lambda expression.
         */
        public Object set(String name, Object value) {
            try {
                Field field = findField(name);
                if (field == null)
                    throw new IllegalArgumentException("No such variable " + name + " in " + Fn0.this);

                if (field.getAnnotation(LambdaLocal.class).isReadOnly())
                    throw new IllegalArgumentException("Variable " + name + " " + field.getType().getName()
                            + " is not modifiable from " + Fn0.this);
                Object array = field.get(Fn0.this);
                Object previousValue = Array.get(array, 0);
                Array.set(array, 0, value);
                return previousValue;
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        Field findField(String name) {
            for (Field field : Fn0.this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(LambdaLocal.class)) {
                    LambdaLocal lambdaLocal = field.getAnnotation(LambdaLocal.class);
                    if (name.equals(lambdaLocal.name())) {
                        field.setAccessible(true);
                        return field;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Returns a limited execution context of this function. It contains local
     * variables (including the outer this) and parameters that are captured.
     */
    public Binding binding() {
        return new Binding();
    }

    public String toString() {
        return getClass().getName();
    }

    /**
     * Returns a function that will give the opposite boolean value of this
     * function. Uses null as false when used with non boolean functions.
     */
    public Fn0<Boolean> complement() {
        return new Fn0<Boolean>() {
            public Boolean call() {
                return isFalseOrNull(Fn0.this.call());
            }
        };
    }

    /**
     * Executes this function if the argument is false.
     */
    public R unless(boolean test) {
        if (!test)
            return call();
        return null;
    }

    /**
     * Calls this function repeatedly with no arguments and executes the given
     * block while the result is true or non null.
     */
    public <B> B whileTrue(Fn0<B> block) {
        B result = null;
        while (isNotFalseOrNull(call()))
            result = block.call();
        return result;
    }

    /**
     * Calls this function with no arguments and executes the given block if the
     * result is true or non null.
     */
    public <B> B ifTrue(Fn0<B> block) {
        if (isNotFalseOrNull(call()))
            return block.call();
        return null;
    }

    /**
     * Calls this function with no arguments and executes the given block if the
     * result is false or null.
     */
    public <B> B ifFalse(Fn0<B> block) {
        if (isFalseOrNull(call()))
            return block.call();
        return null;
    }

    /**
     * Returns true if both this function and the given block evaluates to true
     * of non null.
     */
    public <B> boolean and(Fn0<B> block) {
        return isNotFalseOrNull(call()) && isNotFalseOrNull(block.call());
    }

    /**
     * Returns true if either this function or the given block evaluates to true
     * of non null.
     */
    public <B> boolean or(Fn0<B> block) {
        return isNotFalseOrNull(call()) || isNotFalseOrNull(block.call());
    }

    public static Method getLambdaMethod(Class<?> aClass) {
        Method[] methods = aClass.getDeclaredMethods();
        if (methods.length != 1)
            throw new IllegalArgumentException(aClass.getName() + " has more than one declared method");
        return methods[0];
    }

    public Method getLambdaMethod() {
        return getLambdaMethod(getClass());
    }

    public List<LambdaLocal> getParameters() {
        Method lambdaMethod = getLambdaMethod(getClass());
    
        List<LambdaLocal> parameters = new ArrayList<LambdaLocal>();
        Annotation[][] annotations = lambdaMethod.getParameterAnnotations();
        for (Annotation[] annotation : annotations)
            parameters.add((LambdaLocal) annotation[0]);
        return parameters;
    }

    public List<Field> getParameterFields() {
        try {
            List<Field> result = new ArrayList<Field>();
            for (LambdaLocal parameter : getParameters())
                result.add(getClass().getClassLoader().loadClass(parameter.parameterClass()).getField(parameter.name()));
            return result;
        } catch (Exception e) {
            throw uncheck(e);
        }
    }
}
