package org.enumerable.lambda.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.enumerable.lambda.Fn0;
import org.enumerable.lambda.exception.LambdaWeavingNotEnabledException;


/**
 * This annotation is used to mark static methods that returns a {@link Fn0} or
 * one of it's subclasses so calls to it will be transformed at load time into
 * code that instantiates the new lambda as a {@link Fn0}.
 * <p>
 * The last argument to the method is the actual block expression. The other
 * arguments have to be {@link LambdaParameter} marking the blocks signature.
 * <p>
 * The return type of the block is the type of the block expression parameter.
 * 
 * <p>
 * This method can also be used to create anonymous subclasses implementing
 * other interfaces, but several restrictions apply. Example:
 * 
 * <pre>
 *  &#064;NewLambda
 *  public static Runnable runnable(Object none, Object block) {
 *      throw new LambdaWeavingNotEnabledException();
 *  }
 * 
 *  String hello = "";
 *  Runnable runnable = runnable(hello = "hello");
 *  runnable.run();
 * </pre>
 * 
 * It also supports creation using generics, like this:
 * 
 * <pre>
 * &#064;NewLambda
 *  public static &lt;I&gt; I delegate(Object o, Object block) {
 *      throw new LambdaWeavingNotEnabledException(); 
 *  }
 * 
 *  String hello = ""; Runnable runnable = delegate(hello = "hello");
 *  runnable.run();
 * </pre>
 * 
 * Methods marked with this annotation should throw
 * {@link LambdaWeavingNotEnabledException} when the code is run without
 * transformation.
 */
@Target(ElementType.METHOD)
public @interface NewLambda {
}