package lambda.exception;

import lambda.NewLambda;

/**
 * Exception to be thrown by methods marked with {@link NewLambda} if
 * transformation hasn't taken place.
 */
@SuppressWarnings("serial")
public class LambdaWeavingNotEnabledException extends UnsupportedOperationException {
}
