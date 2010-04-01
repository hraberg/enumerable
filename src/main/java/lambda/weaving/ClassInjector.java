package lambda.weaving;

import static lambda.weaving.LambdaTransformer.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

class ClassInjector {
    static Method defineClass;
    static Method resolveClass;
    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            defineClass.setAccessible(true);
            resolveClass = ClassLoader.class.getDeclaredMethod("resolveClass", Class.class);
            resolveClass.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void inject(ClassLoader loader, String className, byte[] bs) {
        try {
            debug("injecting " + className + " into " + loader);
            Class<?> c = (Class<?>) defineClass.invoke(loader, className, bs, 0, bs.length);
            resolveClass.invoke(loader, c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void dump(String resource, byte[] b) {
        if (!DEBUG) {
            return;
        }
        FileOutputStream out = null;
        try {
            String target = "target/generated-classes/" + resource;
            debug("writing " + target + " (" + b.length + " bytes)");
            new File(target).getParentFile().mkdirs();
            out = new FileOutputStream(target);
            out.write(b);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }
}