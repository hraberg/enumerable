package lambda;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class Fn0<R> {
    public abstract R call();
    
    public void run() {
        call();
    }

    public R apply(Object... args) {
        return call();
    }

    @SuppressWarnings("unchecked")
    public <I> I as(Class<I> anInterface) {
        return (I) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { anInterface }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return apply(args != null ? args : new Object[0]);
            }
        });
    }
}
