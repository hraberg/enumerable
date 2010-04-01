package lambda.weaving;

import static lambda.weaving.Debug.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

class ClassInjector {
    static File classDir;

    static Method defineClass;
    static Method resolveClass;

    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            defineClass.setAccessible(true);
            resolveClass = ClassLoader.class.getDeclaredMethod("resolveClass", Class.class);
            resolveClass.setAccessible(true);
            
            classDir = new File(System.getProperty("lambda.weaving.debug.classes.dir", "target/generated-classes/"));
            
            debug("writing generated classes to " + classDir.getAbsolutePath());
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

    void dump(String name, byte[] b) {
        if (!debug)
            return;

        File file = new File(classDir, name.replace('.', '/') + ".class");

        FileOutputStream out = null;
        try {
            file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            out.write(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}