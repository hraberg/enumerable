package lambda.annotation;

/**
 * This class used to be used to mark an unused parameter. The need for it will
 * be deprecated by the ASM Tree API weaver.
 * 
 * The {@link NewLambda} marked method definition must explicitly use this type.
 */
public final class Unused {
    private Unused() {
    }
}