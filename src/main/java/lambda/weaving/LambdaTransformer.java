package lambda.weaving;

import static java.lang.System.*;
import static org.objectweb.asm.Type.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lambda.LambdaParameter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

class LambdaTransformer {
    static boolean DEBUG = Boolean.valueOf(getProperty("lambda.weaving.debug"));

    static Method findMethod(String owner, String name, String desc) throws NoSuchMethodException, ClassNotFoundException {
        Class<?>[] argumentClasses = new Class[getArgumentTypes(desc).length];
        int i = 0;
        for (Type type : getArgumentTypes(desc)) {
            argumentClasses[i++] = getClassFromType(type);
        }
        return getClassFromType(getObjectType(owner)).getMethod(name, argumentClasses);
    }

    static Field findField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
        return getClassFromType(getObjectType(owner)).getDeclaredField(name);
    }

    static boolean isLambdaParameterField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
        return findField(owner, name).isAnnotationPresent(LambdaParameter.class);
    }

    static Class<?> getClassFromType(Type type) throws ClassNotFoundException {
        switch (type.getSort()) {
        case Type.BYTE:
            return Byte.TYPE;
        case Type.BOOLEAN:
            return Boolean.TYPE;
        case Type.SHORT:
            return Short.TYPE;
        case Type.CHAR:
            return Character.TYPE;
        case Type.INT:
            return Integer.TYPE;
        case Type.FLOAT:
            return Float.TYPE;
        case Type.LONG:
            return Long.TYPE;
        case Type.DOUBLE:
            return Double.TYPE;
        }
        return Class.forName(type.getClassName());
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
