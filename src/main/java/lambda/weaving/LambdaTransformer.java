package lambda.weaving;

import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Type.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lambda.LambdaParameter;
import lambda.NewLambda;
import lambda.Lambda.None;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class LambdaTransformer {
    Map<String, byte[]> lambdasByClassName = new HashMap<String, byte[]>();

    ClassInjector injector = new ClassInjector();

    public LambdaTransformer() {
        debug("current class loader is " + getClass().getClassLoader());
    }

    AnnotationCache lambdaParameterFields = new AnnotationCache(LambdaParameter.class);
    AnnotationCache newLambdaMethods = new AnnotationCache(NewLambda.class);

    boolean isLambdaParameterField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
        return lambdaParameterFields.hasAnnotation(owner, name, "");
    }

    boolean isNewLambdaMethod(String owner, String name, String desc) throws NoSuchMethodException, ClassNotFoundException {
        return newLambdaMethods.hasAnnotation(owner, name, desc);
    }

    boolean isInterface(String owner) {
        try {
            class IsInterfaceFinder extends EmptyVisitor {
                boolean isInterface;

                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    isInterface = (access & ACC_INTERFACE) != 0;
                }
            }
            ClassReader cr = new ClassReader(getObjectType(owner).getClassName());
            IsInterfaceFinder finder = new IsInterfaceFinder();
            cr.accept(finder, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
            return finder.isInterface;
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    boolean isNoneParameter(String desc) {
        return getType(desc).getClassName().equals(None.class.getName());
    }

    MethodInfo getLambdaMethodBestMatch(String owner, String desc) {
        try {
            MethodFinder finder = new MethodFinder(desc);
            ClassReader cr = new ClassReader(getObjectType(owner).getClassName());
            cr.accept(finder, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
            return finder.getMethod();
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    byte[] transform(String name, InputStream in) throws IOException {
        if (lambdasByClassName.containsKey(name)) {
            debug("generated lambda requested by the class loader " + name);
            return lambdasByClassName.get(name);
        }

        ClassReader cr = new ClassReader(in);

        FirstPassClassVisitor firstPass = new FirstPassClassVisitor(this);
        cr.accept(firstPass, 0);
        if (firstPass.hasNoLambdas())
            return null;

        ClassWriter cw = new ClassWriter(COMPUTE_MAXS);
        SecondPassClassVisitor visitor = new SecondPassClassVisitor(cw, firstPass, this);
        cr.accept(visitor, 0);

        return cw.toByteArray();
    }

    void newLambdaClass(String name, byte[] bs) {
        lambdasByClassName.put(name, bs);
        injector.dump(name, bs);
        injector.inject(getClass().getClassLoader(), name.replace('/', '.'), bs);
    }
}
