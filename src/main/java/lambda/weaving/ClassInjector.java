package lambda.weaving;

import java.io.*;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;

import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;

class ClassInjector {
    static File classDir;

    static Method defineClass;
    static Method resolveClass;

    static Method verify;

    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                    Integer.TYPE, Integer.TYPE);
            defineClass.setAccessible(true);
            resolveClass = ClassLoader.class.getDeclaredMethod("resolveClass", Class.class);
            resolveClass.setAccessible(true);

            classDir = new File(System.getProperty("lambda.weaving.debug.classes.dir", "target/generated-classes/"));
            debug("writing generated classes to " + classDir.getAbsolutePath());

        } catch (Exception e) {
            throw uncheck(e);
        }

        try {
            try{
                Class<?> checkClassAdapter = Class.forName("org.objectweb.asm.util.CheckClassAdapter");
                verify = checkClassAdapter.getMethod("verify", Class.forName("org.objectweb.asm.ClassReader"), Boolean.TYPE, PrintWriter.class);
            } catch (ClassNotFoundException e) {                
                Class<?> checkClassAdapter = Class.forName("lambda.asm.util.CheckClassAdapter");
                verify = checkClassAdapter.getMethod("verify", Class.forName("lambda.asm.ClassReader"), Boolean.TYPE, PrintWriter.class);
            }
            
            debug("asm-util is avaialbe, will pre-verify generated classes");

        } catch (Exception ignore) {
            debug("asm-util NOT avaialbe, will not be able to pre-verify generated classes");
        }

    }

    void inject(ClassLoader loader, String className, byte[] bs) {
        try {
            debug("defining class " + className);
            Class<?> c = (Class<?>) defineClass.invoke(loader, className, bs, 0, bs.length);
            resolveClass.invoke(loader, c);
        } catch (Exception e) {
            throw uncheck(e);
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
            throw uncheck(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException silent) {
                }
            }
        }
    }

    void verifyIfAsmUtilIsAvailable(byte[] b) {
        try {
            if (verify == null)
                return;

            ClassReader cr = new ClassReader(new ByteArrayInputStream(b));
            PrintWriter pw = new PrintWriter(System.out);
            verify.invoke(null, cr, false, pw);

        } catch (Exception e) {
            throw uncheck(e);
        }
    }
}