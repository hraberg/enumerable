package lambda.weaving;

import static java.lang.System.*;
import static org.objectweb.asm.Type.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class LambdaTransformer implements Opcodes {
	static boolean DEBUG = true;

	static Method findMethod(String owner, String name, String desc) throws NoSuchMethodException, ClassNotFoundException {
		Class<?>[] argumentClasses = new Class[getArgumentTypes(desc).length];
		Arrays.fill(argumentClasses, Object.class);
		return Class.forName(getObjectType(owner).getClassName()).getMethod(name, argumentClasses);
	}

	static Field findField(String owner, String name) throws NoSuchFieldException, ClassNotFoundException {
		String className = getObjectType(owner).getClassName();
		return Class.forName(className).getDeclaredField(name);
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

	static void debug(Object msg) {
		if (DEBUG)
			err.println(msg);
	}
}
