package org.enumerable.lambda.support.methodhandle;

import org.enumerable.lambda.Fn0;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static org.enumerable.lambda.exception.UncheckedException.uncheck;

/**
 * This class converts lambdas created by {@link org.enumerable.lambda.weaving.LambdaLoader} to {@link java.lang.invoke.MethodHandle}
 *
 * It lives under support instead of being an instance method as it requires Java 7.
 */
public class LambdaMethodHandle {
    public static MethodHandle bind(Object fn) {
        try {
            return MethodHandles.lookup().unreflect(Fn0.getLambdaMethod(fn.getClass())).bindTo(fn);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }
}
