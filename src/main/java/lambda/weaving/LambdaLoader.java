package lambda.weaving;

import static java.lang.System.*;
import static java.lang.Thread.*;
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

import lambda.exception.LambdaWeavingNotEnabledException;

public class LambdaLoader extends ClassLoader implements ClassFileTransformer {
    private static boolean isEnabled;
    private static boolean tranformationFailed;
    static String weavingNotEnabledMessage = "Please start the JVM with -javaagent:enumerable-"
            + Version.getVersion() + "-agent.jar";;

    static {
        isEnabled = LambdaLoader.class.getClassLoader().getResource(LambdaCompiler.AOT_COMPILED_MARKER) != null;
    }

    /**
     * Allows you to query the Lambda weaver at runtime to see if it's enabled.
     */
    public static boolean isEnabled() {
        return isEnabled && !tranformationFailed;
    }

    /**
     * This method can be used as a guard clause in your code, potentially
     * throwing a {@link LambdaWeavingNotEnabledException}.
     */
    public static void ensureIsEnabled() {
        if (!isEnabled())
            throw new LambdaWeavingNotEnabledException();
    }

    /**
     * This method can be used as a guard clause in your code, exiting the VM if
     * weaving isn't enabled.
     */
    public static void ensureIsEnabledOrExit() {
        if (!isEnabled()) {
            err.println(LambdaLoader.getNotEnabledMessage());
            System.exit(1);
        }
    }

    /**
     * This method can be used early in a main method to allow it to reload the
     * caller in the same process with lambda weaving enabled if it is currently
     * disabled. Control will not normally be returned to the caller as the VM
     * will be exited after the reloaded main has finished.
     * <p>
     * If waving is already enabled, this method just returns.
     * <p>
     * This method is mainly intended to be used as a convenience in smaller
     * applications.
     */
    public static void bootstrapMainIfNotEnabledAndExitUponItsReturn(String[] args) {
        if (!isEnabled()) {
            StackTraceElement caller = currentThread().getStackTrace()[2];
            if ("main".equals(caller.getMethodName())) {
                try {
                    String className = caller.getClassName();
                    out.println(getNotEnabledMessage());
                    out.println("Will try to reload " + className + " in the same process:");
                    launchApplication(className, args);
                    exit(0);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
            throw new IllegalStateException("Must be called from a main method.");
        }
    }

    /**
     * Loads a class in a new class loader with lambda weaving enabled and
     * invokes its main method.
     */
    public static Object launchApplication(String className, String[] args) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        debug("[main] " + getVersionString());
        isEnabled = true;
        addSkippedPackages(System.getProperty("lambda.weaving.skipped.packages", ""));
        Class<?> c = new LambdaLoader().loadClass(className);
        Method m = c.getMethod("main", String[].class);
        return m.invoke(null, new Object[] { args });
    }

    public static String getNotEnabledMessage() {
        return weavingNotEnabledMessage;
    }

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

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
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
            return transformer.transform(in);
        } catch (Throwable t) {
            tranformationFailed = true;
            weavingNotEnabledMessage = t.getMessage();

            err.println(getVersionString());
            err
                    .println("caught throwable while transforming " + name
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
        isEnabled = true;
        addSkippedPackages(System.getProperty("lambda.weaving.skipped.packages", ""));
        instrumentation.addTransformer(new LambdaLoader());
    }

    public static void main(String[] args) throws Throwable {
        try {
            if (args.length == 0) {
                out.println("Usage: class [args...]");
                return;
            }

            String[] argsCopy = new String[args.length - 1];
            arraycopy(args, 1, argsCopy, 0, args.length - 1);

            launchApplication(args[0], argsCopy);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
