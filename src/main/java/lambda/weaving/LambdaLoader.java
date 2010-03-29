package lambda.weaving;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static lambda.weaving.LambdaTransformer.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
	static Set<String> packagesToSkip = new HashSet<String>();

	static {
		packagesToSkip.add("java");
		packagesToSkip.add("sun");
		packagesToSkip.add("$Proxy");
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		String resource = name.replace('.', '/') + ".class";
		InputStream in = getResourceAsStream(resource);
		try {
			byte[] b = transformClass(resource, in);
			if (b == null) {
				return super.loadClass(name, resolve);
			}
			return defineClass(name, b, 0, b.length);
		} catch (Exception e) {
			throw new ClassNotFoundException(name, e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			return transformClass(className + ".class", new ByteArrayInputStream(classfileBuffer));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	byte[] transformClass(String resource, InputStream in) throws IOException {
		if (isNotToBeInstrumented(resource.replace('/', '.')))
			return null;
		byte[] b = new LambdaTransformer().transform(resource, in);
		if (b != null && LambdaTransformer.DEBUG) {
			debug("Load time weaver: " + resource);
			dumpClass(resource, b);
		}

		return b;
	}

	boolean isNotToBeInstrumented(String name) {
		for (String prefix : packagesToSkip)
			if (name.startsWith(prefix))
				return true;
		return false;
	}

	void dumpClass(String resource, byte[] b) {
		new File(resource).getParentFile().mkdirs();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(resource);
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

	static void addSkippedPackages(String agentArgs) {
		if (agentArgs != null)
			for (String prefix : agentArgs.split(",")) {
				String trim = prefix.trim();
				if (trim.length() > 0)
					packagesToSkip.add(trim);
			}
	}

	public static void premain(String agentArgs, Instrumentation instrumentation) {
		debug("Running premain " + LambdaLoader.class.getSimpleName());
		addSkippedPackages(agentArgs);
		instrumentation.addTransformer(new LambdaLoader(), true);
	}

	public static void main(String[] args) throws Throwable {
		try {
			if (args.length == 0) {
				out.println("Usage: class [args...]");
				return;
			}
			debug("Running main " + LambdaLoader.class.getSimpleName());
			launchApplication(args[0], copyOfRange(args, 1, args.length));
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public static Object launchApplication(String className, String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		addSkippedPackages(System.getProperty("lambda.skippedPackages"));
		Class<?> c = new LambdaLoader().loadClass(className);
		Method m = c.getMethod("main", String[].class);
		return m.invoke(null, new Object[] { args });
	}
}
