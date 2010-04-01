package lambda.weaving;

import static lambda.weaving.Debug.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lambda.LambdaParameter;
import lambda.NewLambda;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class LambdaTransformer {
    Map<String, byte[]> lambdasByClassName = new HashMap<String, byte[]>();

    AnnotationCache lambdaParameterFields = new AnnotationCache(LambdaParameter.class);
    AnnotationCache newLambdaMethods = new AnnotationCache(NewLambda.class);

    boolean isLambdaParameterField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
        return lambdaParameterFields.hasAnnotation(owner, name, "");
    }

    boolean isNewLambdaMethod(String owner, String name, String desc) throws NoSuchMethodException, ClassNotFoundException {
        return newLambdaMethods.hasAnnotation(owner, name, desc);
    }

    byte[] transform(String name, InputStream in) throws IOException {
        if (lambdasByClassName.containsKey(name)) {
            debug("generated lambda requested by the class loader " + name);
            return lambdasByClassName.get(name);
        }

        ClassReader cr = new ClassReader(in);

        FirstPassClassVisitor firstPass = new FirstPassClassVisitor(this);
        cr.accept(firstPass, 0);
        if (firstPass.hasNoLambdas()) {
            return null;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        SecondPassClassVisitor visitor = new SecondPassClassVisitor(cw, firstPass, this);
        cr.accept(visitor, 0);

        return cw.toByteArray();
    }

    void newLambdaClass(String name, byte[] bs) {
        lambdasByClassName.put(name, bs);

        ClassInjector injector = new ClassInjector();
        injector.dump(name, bs);
        injector.inject(getClass().getClassLoader(), name.replace('/', '.'), bs);
    }
}
