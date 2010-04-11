package lambda.weaving;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lambda.annotation.LambdaParameter;
import lambda.annotation.NewLambda;
import lambda.annotation.Unused;

import org.objectweb.asm.*;

import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Type.*;

class LambdaTransformer implements Opcodes {
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

    boolean isNewLambdaMethod(String owner, String name, String desc) throws NoSuchMethodException,
            ClassNotFoundException {
        return newLambdaMethods.hasAnnotation(owner, name, desc);
    }

    boolean isInterface(String owner) {
       return (getClassReader(owner).getAccess() & ACC_INTERFACE) != 0;
    }

    String[] getInterfaces(String owner) {
        return getClassReader(owner).getInterfaces();
    }

    String getSuperClass(String owner) {
        return getClassReader(owner).getSuperName();
    }

    boolean isFieldPrivate(String owner, final String field) {
        class IsFieldPrivateVisitor extends EmptyVisitor {
            int access;
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                if (field.equals(name))
                    IsFieldPrivateVisitor.this.access = access;
                return null;
            }
        }
        return (visitClass(owner, new IsFieldPrivateVisitor()).access & ACC_PRIVATE) != 0;
     }

    MethodInfo findMethodByParameterTypes(String owner, String desc) {
        MethodInfo method = visitClass(owner, new MethodFinder(desc)).getMethod();
        if (method != null)
            return method;

        for (String anInterface : getInterfaces(owner)) {
            method = findMethodByParameterTypes(anInterface, desc);
            if (method != null)
                return method;
        }
        String superClass = getSuperClass(owner);
        if (superClass != null)
            return findMethodByParameterTypes(superClass, desc);
        return null;
    }

    <V extends ClassVisitor> V visitClass(String owner, V cv) {
        ClassReader cr = getClassReader(owner);
        cr.accept(cv, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
        return cv;
    }

    ClassReader getClassReader(String owner) {
        try {
            return new ClassReader(getObjectType(owner).getClassName());
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    boolean isUnusedParameter(String desc) {
        return getType(desc).getClassName().equals(Unused.class.getName());
    }

    boolean isStoreInstruction(int opcode) {
        return opcode >= ISTORE && opcode <= ASTORE;
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

        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
        SecondPassClassVisitor visitor = new SecondPassClassVisitor(cw, firstPass, this);
        cr.accept(visitor, 0);

        byte[] bs = cw.toByteArray();

        injector.dump(name, bs);
        injector.verifyIfAsmUtilIsAvailable(bs);

        return bs;
    }

    void newLambdaClass(String name, byte[] bs) {
        lambdasByClassName.put(name, bs);

        injector.dump(name, bs);
        injector.verifyIfAsmUtilIsAvailable(bs);
        injector.inject(getClass().getClassLoader(), name.replace('/', '.'), bs);
    }
}
