package lambda.annotation;

import lambda.Lambda;

/**
 * This class is used to mark an unused parameter, see
 * {@link Lambda#fn(None, Object)} and {@link Lambda#_} for the default way
 * to do this.
 * 
 * The {@link NewLambda} marked method definition must explicitly use this
 * type.
 */
public final class None {
    private None() {
    }
}