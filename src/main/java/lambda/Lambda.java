package lambda;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Lambda {
	public static <E1, E2, R> Fn1<E2, R> partial(final Fn2<E1, E2, R> lambda, final E1 arg1) {
		return new Fn1<E2, R>() {
			public R call(E2 arg2) {
				return lambda.call(arg1, arg2);
			}
		};
	}

	public static <E, R> Fn0<R> partial(final Fn1<E, R> lambda, final E arg1) {
		return new Fn0<R>() {
			public R call() {
				return lambda.call(arg1);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static <I> I wrap(final Fn2 lambda, Class<I> anInterface) {
		return (I) Proxy.newProxyInstance(Lambda.class.getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return lambda.call(args[0], args[1]);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static <I> I wrap(final Fn1 lambda, Class<I> anInterface) {
		return (I) Proxy.newProxyInstance(Lambda.class.getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return lambda.call(args[0]);
			}
		});
	}
}
