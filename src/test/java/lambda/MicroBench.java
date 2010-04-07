package lambda;
import static java.lang.System.*;
import static lambda.Lambda.*;
import static lambda.enumerable.Enumerable.*;
import lambda.Fn1;
import lambda.Lambda;
import lambda.primitives.Fn1ItoI;

public class MicroBench {
    public static void main(String[] args) {
        assert fibo.getClass().isAssignableFrom(fibp.getClass());
        assert !fibp.getClass().isAssignableFrom(fibo.getClass());

        int n = args.length > 0 ? Integer.parseInt(args[0]) : 32;
        int times = 5;

        bench("object lambda", times, λ(i, fibo.call(n)));
        bench("primitive lambda", times, λ(i, fibp.call(n)));
        bench("object method", times, λ(i, fibo(n)));
        bench("primitive method", times, λ(i, fibp(n)));
    }

    static void bench(String name, int times, Fn1<Integer, ?> block) {
        long now = currentTimeMillis();
        each(range(1, times), block);
        System.out.println(name  + " took " + (currentTimeMillis() - now) + " ms.");
    }

    static Fn1<Integer, Integer> fibo;
    static Fn1ItoI fibp;

    static {
        fibo = Lambda.fn(n, n <= 1 ? n : fibo.call(n - 1) + fibo.call(n - 2));
        fibp = Lambda.Primitives.fn(n, n <= 1 ? n : fibp.call(n - 1) + fibp.call(n - 2));        
    }
    
    static int fibo(Integer n) {
        return n <= 1 ? n : fibo(n - 1) + fibo(n - 2);
    }

    static int fibp(int n) {
        return n <= 1 ? n : fibp(n - 1) + fibp(n - 2);
    }
}
