package org.enumerable.lambda.exception;

import org.enumerable.lambda.annotation.NewLambda;
import org.enumerable.lambda.weaving.LambdaLoader;

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
