package lambda.weaving;

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
			Class<?> c = (Class<?>) defineClass.invoke(loader, className.replace('/', '.'), bs, 0, bs.length);
			resolveClass.invoke(loader, c);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}