package org.enumerable.lambda.weaving;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * This class can be used to attach the Enumerable.java agent to the currently running VM.
 *
 * It doesn't depend on any Enumerable.java classes and can serve as a template to write your own bootstrapping.
 */
public class LambdaAgentAttach {
    /**
     * Attempts to attach the Enumerable.java agent already present on the classpath to the running process.
     * $JAVA_HOME/lib/tools.jar with com.sun.tools.attach.VirtualMachine must be present.
     */
    public static void attachAgent() {
        attachAgent(agentJar());
    }

    /**
     * Attempts to attach the Enumerable.java jar to the running process.
     * $JAVA_HOME/lib/tools.jar with com.sun.tools.attach.VirtualMachine must be present.
     */
    public static void attachAgent(String pathToEnumerableJavaJar) {
        try {
            if (!toolsJar().isFile()) throw new IllegalStateException("Cannot find tools jar in " + System.getProperty("java.home" ));

            Class<?> vmClass = classLoaderWithToolsJar().loadClass("com.sun.tools.attach.VirtualMachine");
            Object vm = vmClass.getMethod("attach", String.class).invoke(null, pidOfRunningVM());

            vmClass.getMethod("loadAgent", String.class).invoke(vm, pathToEnumerableJavaJar);
            vmClass.getMethod("detach").invoke(vm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String agentJar() {
        Enumeration<URL> resources = potentialClasspathEntries();
        while (resources.hasMoreElements()) {
            URL url =  resources.nextElement();
            if ("jar".equals(url.getProtocol())) {
                String file = url.getFile().split("!")[0];
                if (file.startsWith("file:")) {
                    return file.replace("file:", "");
                }
            }
        }
        throw new IllegalStateException("Cannot find Enumerable.java on classpath");
    }

    private static Enumeration<URL> potentialClasspathEntries() {
        try {
            return LambdaAgentAttach.class.getClassLoader().getResources("org/enumerable/lambda/weaving/version.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URLClassLoader classLoaderWithToolsJar() throws MalformedURLException {
        return new URLClassLoader(new URL[]{toolsJar().toURI().toURL()});
    }

    private static File toolsJar() {
        return new File(System.getProperty("java.home" ), "../lib/tools.jar" );
    }

    private static String pidOfRunningVM() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        return nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
    }
}
