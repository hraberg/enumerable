package lambda.weaving;

import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ClassInjector {
    static File classDir;

    static Method defineClass;
    static Method resolveClass;

    static Method verify;

    static Constructor<?> classReaderConstructor;

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
            String[] realAsmPackageNotToBeChangedByJarJar = { "org.objectweb." };

            Class<?> checkClassAdapter = Class.forName(realAsmPackageNotToBeChangedByJarJar[0]
                    + "asm.util.CheckClassAdapter");
            Class<?> classReader = Class.forName(realAsmPackageNotToBeChangedByJarJar[0] + "asm.ClassReader");

            verify = checkClassAdapter.getMethod("verify", classReader, Boolean.TYPE, PrintWriter.class);
            classReaderConstructor = classReader.getConstructor(InputStream.class);

            debug("asm-util is avaialbe, will pre-verify generated classes");

        } catch (Exception ignore) {
            debug("asm-util NOT avaialbe, will not be able to pre-verify generated classes");
        }
    }

    public void inject(ClassLoader loader, String className, byte[] bs) {
        try {
            debug("defining class " + className);
            Class<?> c = (Class<?>) defineClass.invoke(loader, className, bs, 0, bs.length);
            resolveClass.invoke(loader, c);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public void dump(String name, byte[] b) {
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

    // Disabled for now, printing loads of stuff anyway
    public void verifyIfAsmUtilIsAvailable(byte[] b) {
        try {
            // if (verify == null || !debug)
            // return;
            //
            // Object cr = classReaderConstructor.newInstance(new
            // ByteArrayInputStream(b));
            // PrintWriter pw = new PrintWriter(System.out);
            // verify.invoke(null, cr, false, pw);

        } catch (Exception e) {
            throw uncheck(e);
        }
    }
}