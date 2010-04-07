package lambda;

import static java.lang.System.*;
import static lambda.Lambda.*;
import static lambda.enumerable.Enumerable.*;
import lambda.primitives.Fn1ItoI;
import lambda.weaving.Version;

public class MicroBench {
    public static void main(String[] args) {
        System.out.println("[microbench] " + Version.getVersionString());

        assert fibo.getClass().isAssignableFrom(fibp.getClass());
        assert !fibp.getClass().isAssignableFrom(fibo.getClass());

        int n = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        int times = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        bench("object lambda", times, λ(_, fibo.call(n)));
        bench("primitive lambda", times, λ(_, fibp.call(n)));
        bench("object method", times, λ(_, fibo(n)));
        bench("primitive method", times, λ(_, fibp(n)));
    }

    static long bench(String name, int times, Fn0<?> block) {
        long now = currentTimeMillis();
        times(times, block);
        long time = currentTimeMillis() - now;
        System.out.println(name + " run " + times + " times, average " + time / times + " ms.");
        return time;
    }

    static Fn1<Integer, Integer> fibo;
    static Fn1ItoI fibp;

    static {
        fibo = Lambda.λ(n, n <= 1 ? n : fibo.call(n - 1) + fibo.call(n - 2));
        fibp = Lambda.Primitives.λ(n, n <= 1 ? n : fibp.call(n - 1) + fibp.call(n - 2));
    }

    static Integer fibo(Integer n) {
        return n <= 1 ? n : fibo(n - 1) + fibo(n - 2);
    }

    static int fibp(int n) {
        return n <= 1 ? n : fibp(n - 1) + fibp(n - 2);
    }
}
