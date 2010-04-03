package lambda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark static fields which can then be used in a
 * lambda to define parameters and access it's arguments.
 * <p>
 * Example:
 * 
 * <pre>
 * &#064;LambdaParameter
 * public static int n;
 * 
 * fn(n, n * n);
 * </pre>
 * 
 * Here, the first reference to <code>n</code> is treated as defining a
 * parameter for the lambda. The next two references refer to the first argument
 * of the lambda when called.
 * 
 * All access to the actual field will be redirected, so once marked, the field
 * cannot be used as a normal field, and doing so will fail in undefined ways.
 */
@Target(ElementType.FIELD)
public @interface LambdaParameter {
}