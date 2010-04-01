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

    Map<String, byte[]> lambdasByResourceName = new HashMap<String, byte[]>();

    AnnotationCache lambdaParameterFields = new AnnotationCache(LambdaParameter.class);
    AnnotationCache newLambdaMethods = new AnnotationCache(NewLambda.class);

    boolean isLambdaParameterField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
        return lambdaParameterFields.hasAnnotation(owner, name, "");
    }

    boolean isNewLambdaMethod(String owner, String name, String desc) throws NoSuchMethodException, ClassNotFoundException {
        return newLambdaMethods.hasAnnotation(owner, name, desc);
    }

    byte[] transform(String resource, InputStream in) throws IOException {
        if (lambdasByResourceName.containsKey(resource)) {
            debug("generated lambda was requested by the class loader " + resource);
            return lambdasByResourceName.get(resource);
        }

        ClassReader cr = new ClassReader(in);

        FirstPassClassVisitor firstPass = new FirstPassClassVisitor(this);
        cr.accept(firstPass, 0);
        if (firstPass.hasNoLambdas()) {
            return null;
        }

        debug("second pass: transforming lambdas and accessed locals in " + resource);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        SecondPassClassVisitor visitor = new SecondPassClassVisitor(cw, firstPass, this);
        cr.accept(visitor, 0);
        lambdasByResourceName.putAll(visitor.lambdasByResourceName);

        return cw.toByteArray();
    }

    static void debug(String msg) {
        if (DEBUG)
            err.println(msg);
    }

    void newLambdaClass(String internalName, byte[] bs) {
        String resource = internalName + ".class";
        lambdasByResourceName.put(resource, bs);

        ClassInjector injector = new ClassInjector();
        injector.dump(resource, bs);
        injector.inject(getClass().getClassLoader(), internalName.replace('/', '.'), bs);
    }
}
