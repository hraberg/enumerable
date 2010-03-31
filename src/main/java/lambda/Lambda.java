package lambda;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

public class Lambda {
	@LambdaParameter
	public static Integer n;
	@LambdaParameter
	public static Integer m;
	@LambdaParameter
	public static Integer i;
	@LambdaParameter
	public static Integer idx;
	@LambdaParameter
	public static String s;
	@LambdaParameter
	public static CharSequence cs;
	@LambdaParameter
	public static String t;
	@LambdaParameter
	public static Double d;
	@LambdaParameter
	public static Character c;
	@LambdaParameter
	public static Collection<?> col;
	@LambdaParameter
	public static Object obj;
	@LambdaParameter
	public static Object _;

	@NewLambda
	public static <E, R> Fn1<E, R> λ(E obj, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E, R> Fn1<E, R> lambda(E obj, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E, R> Fn1<E, R> fn(E obj, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E1, E2, R> Fn2<E1, E2, R> λ(E1 obj1, E2 obj2, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E1, E2, R> Fn2<E1, E2, R> lambda(E1 obj1, E2 obj2, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E1, E2, R> Fn2<E1, E2, R> fn(E1 obj1, E2 obj2, R block) {
		throw new UnsupportedOperationException();
	}

	public static <E1, E2, R> Fn1<E2, R> partial(final Fn2<E1, E2, R> lambda, final E1 obj1) {
		return new Fn1<E2, R>() {
			public R call(E2 obj2) {
				return lambda.call(obj1, obj2);
			}
		};
	}

	public static <E, R> Fn0<R> partial(final Fn1<E, R> lambda, final E obj) {
		return new Fn0<R>() {
			public R call() {
				return lambda.call(obj);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static <I> I as(Class<I> anInterface, final Fn2 lambda) {
		return (I) Proxy.newProxyInstance(Lambda.class.getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return lambda.call(args[0], args[1]);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static <I> I as(Class<I> anInterface, final Fn1 lambda) {
		return (I) Proxy.newProxyInstance(Lambda.class.getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return lambda.call(args == null ? null : args[0]);
			}
		});
	}
}
