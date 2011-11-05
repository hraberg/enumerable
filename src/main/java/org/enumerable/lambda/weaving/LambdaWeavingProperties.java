package org.enumerable.lambda.weaving;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.enumerable.lambda.exception.UncheckedException.uncheck;
import static org.enumerable.lambda.weaving.Debug.debug;

/**
 * This class will load <code>/org/enumerable/lambda/weaving/lambda.weaving.properties</code> to configure the annotations used
 * to guide the bytecode transformation. The default file of Enumerable.java is defined as:
 * <p>
 * <code>
 *     
 *  lambda.weaving.annotation.newlambda=org.enumerable.lambda.annotation.NewLambda
 *  lambda.weaving.annotation.lambdaparameter=org.enumerable.lambda.annotation.LambdaParameter
 *  lambda.weaving.annotation.lambdalocal=org.enumerable.lambda.annotation.LambdaLocal
 *
 * </code>
 * </p>
 */
public class LambdaWeavingProperties {
    private final static Properties properties;

    static {
        InputStream in = null;
        try {
            in = LambdaWeavingProperties.class.getResourceAsStream("lambda.weaving.properties");
            if (in == null) throw new IllegalStateException("Could not find /org/enumerable/lambda/weaving/lambda.weaving.properties");
            (properties = new Properties(System.getProperties())).load(in);
        } catch (IOException e) {
            throw uncheck(e);
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException silent) {
            }
        }
    }


    public static String get(String property, boolean nullable) {
        String value = properties.getProperty(property);
        debug(property + ": " + value);
        boolean notSet = value == null || value.trim().length() == 0;
        if (notSet && nullable) return null;
        if (notSet) throw new IllegalStateException(property + " cannot be null");
        return value;
    }
}
