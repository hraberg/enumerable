package lambda.exception;

import lambda.annotation.NewLambda;
import lambda.weaving.LambdaLoader;

/**
 * Exception to be thrown by methods marked with {@link NewLambda} if
 * transformation hasn't taken place.
 */
@SuppressWarnings("serial")
public class LambdaWeavingNotEnabledException extends UnsupportedOperationException {
    public LambdaWeavingNotEnabledException() {
        super(LambdaLoader.getNotEnabledMessage());
    }
}
