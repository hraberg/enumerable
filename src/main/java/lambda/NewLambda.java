package lambda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import lambda.exception.LambdaWeavingNotEnabledException;

/**
 * This annotation is used to mark static methods that returns a {@link Fn0} or
 * one of it's subclasses so calls to it will be transformed at load time into
 * code that instantiates the new lambda as a {@link Fn0}.
 * 
 * <p>
 * This method can also be used to create anonymous subclasses implementing
 * other interfaces, but several restrictions apply. Example:
 * </p>
 * 
 * <pre>
 * 
 *  &#064;NewLambda
 *  public static Runnable runnable(Object none, Object block) {
 *      throw new LambdaWeavingNotEnabledException();
 *  }
 * 
 *  String hello = "";
 *  Runnable runnable = runnable(_, hello = "hello");
 *  runnable.run();
 * 
 * </pre>
 * 
 * Methods marked with this annotation should throw
 * {@link LambdaWeavingNotEnabledException} when the code is run without
 * transformation.
 */
@Target(ElementType.METHOD)
public @interface NewLambda {
}