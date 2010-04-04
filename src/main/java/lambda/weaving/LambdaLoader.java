package lambda.weaving;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static lambda.weaving.Version.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class LambdaLoader extends ClassLoader implements ClassFileTransformer {
    public static boolean tranformationFailed;

    static Set<String> packagesToSkip = new HashSet<String>();
    static {
        packagesToSkip.add("java.");
        packagesToSkip.add("javax.");
        packagesToSkip.add("sun.");
        packagesToSkip.add("$Proxy");
    }

    LambdaTransformer transformer = new LambdaTransformer();

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
        try {
            byte[] b = transformClass(name, in);
            if (b == null)
                return super.loadClass(name, resolve);
            return defineClass(name, b, 0, b.length);
        } catch (Exception e) {
            throw uncheck(e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException silent) {
            }
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            return transformClass(className.replace('/', '.'), new ByteArrayInputStream(classfileBuffer));
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    byte[] transformClass(String name, InputStream in) {
        try {
            if (isNotToBeInstrumented(name) || tranformationFailed)
                return null;
            byte[] b = transformer.transform(name, in);
            if (b != null)
                new ClassInjector().dump(name, b);
            return b;
        } catch (Throwable t) {
            tranformationFailed = true;
            err.println(getVersionString());
            err.println("caught throwable while transforming " + name
                    + ", transformation is disabled from here on");
            throw uncheck(t);
        }
    }

    boolean isNotToBeInstrumented(String name) {
        for (String prefix : packagesToSkip)
            if (name.startsWith(prefix))
                return true;
        return false;
    }

    static void addSkippedPackages(String agentArgs) {
        for (String prefix : agentArgs.split(",")) {
            String trim = prefix.trim();
            if (trim.length() > 0)
                packagesToSkip.add(trim);
        }
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        debug("[premain] " + getVersionString());
        addSkippedPackages(System.getProperty("lambda.weaving.skipped.packages", ""));
        instrumentation.addTransformer(new LambdaLoader());
    }

    public static void main(String[] args) throws Throwable {
        try {
            if (args.length == 0) {
                out.println("Usage: class [args...]");
                return;
            }
            debug("[main] " + getVersionString());
            addSkippedPackages(System.getProperty("lambda.weaving.skipped.packages", ""));
            launchApplication(args[0], copyOfRange(args, 1, args.length));
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    static Object launchApplication(String className, String[] args) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class<?> c = new LambdaLoader().loadClass(className);
        Method m = c.getMethod("main", String[].class);
        return m.invoke(null, new Object[] { args });
    }
}
