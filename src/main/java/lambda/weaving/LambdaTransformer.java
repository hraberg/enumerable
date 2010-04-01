package lambda.weaving;

import static java.lang.System.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lambda.LambdaParameter;
import lambda.NewLambda;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class LambdaTransformer {
    static boolean DEBUG = Boolean.valueOf(getProperty("lambda.weaving.debug"));

    static AnnotationCache lambdaParameterFields = new AnnotationCache(LambdaParameter.class);
    static AnnotationCache newLambdaMethods = new AnnotationCache(NewLambda.class);

    static boolean isLambdaParameterField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
        return lambdaParameterFields.hasAnnotation(owner, name, "");
    }

    static boolean isNewLambdaMethod(String owner, String name, String desc) throws NoSuchMethodException, ClassNotFoundException {
        return newLambdaMethods.hasAnnotation(owner, name, desc);
    }

    Map<String, byte[]> lambdasByResourceName = new HashMap<String, byte[]>();

    byte[] transform(String resource, InputStream in) throws IOException {
        if (lambdasByResourceName.containsKey(resource)) {
            debug("generated lambda was requested by the class loader " + resource);
            return lambdasByResourceName.get(resource);
        }

        // debug("First pass: analyzing " + resource);
        ClassReader cr = new ClassReader(in);

        FirstPassClassVisitor firstPass = new FirstPassClassVisitor();
        cr.accept(firstPass, 0);
        if (firstPass.hasNoLambdas()) {
            return null;
        }

        debug("Second pass: transforming lambdas and accessed locals in " + resource);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        SecondPassClassVisitor visitor = new SecondPassClassVisitor(cw, firstPass);
        cr.accept(visitor, 0);
        lambdasByResourceName.putAll(visitor.lambdasByResourceName);

        return cw.toByteArray();
    }

    static void debug(String msg) {
        if (DEBUG)
            err.println(msg);
    }
}
