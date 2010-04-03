package lambda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import lambda.exception.LambdaWeavingNotEnabledException;

/**
 * This annotation is used to mark static methods that returns a {@link Fn0} or
 * one of it's subclasses so calls to it will be transformed at load time into
 * code that instantiates the new lambda as a {@link Fn0}.
 * 
 * This annotation is not really expected to to be used by clients of the
 * library.
 * 
 * Methods marked with this method are expected to throw
 * {@link LambdaWeavingNotEnabledException} when the code is run without
 * transformation.
 */
@Target(ElementType.METHOD)
public @interface NewLambda {
}