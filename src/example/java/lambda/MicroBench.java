package lambda;

import lambda.primitives.Fn1ItoI;
import lambda.primitives.LambdaPrimitives;
import lambda.weaving.LambdaLoader;
import lambda.weaving.Version;
import static java.lang.System.*;
import static lambda.Lambda.*;
import static lambda.Parameters.*;

import static lambda.enumerable.Enumerable.*;

public class MicroBench {
    public static void main(String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);
        System.out.println("[microbench] " + Version.getVersionString());

        int n = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        int times = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        new MicroBench().run(n, times);
    }

    void run(int n, int times) {
        assert fibo.getClass().isAssignableFrom(fibp.getClass());
        assert !fibp.getClass().isAssignableFrom(fibo.getClass());

        bench("object lambda", times, λ(_, fibo.call(n)));
        bench("primitive lambda", times, λ(_, fibp.call(n)));
        bench("object method", times, λ(_, fibo(n)));
        bench("primitive method", times, λ(_, fibp(n)));
    }

    long bench(String name, int times, Fn0<?> block) {
        long now = currentTimeMillis();
        times(times, block);
        long time = currentTimeMillis() - now;
        System.out.println(name + " run " + times + " times, average " + time / times + " ms.");
        return time;
    }

    Fn1<Integer, Integer> fibo;
    Fn1ItoI fibp;

    {
        fibo = Lambda.λ(n, n <= 1 ? n : fibo.call(n - 1) + fibo.call(n - 2));
        fibp = LambdaPrimitives.λ(n, n <= 1 ? n : fibp.call(n - 1) + fibp.call(n - 2));
    }

    Integer fibo(Integer n) {
        return n <= 1 ? n : fibo(n - 1) + fibo(n - 2);
    }

    int fibp(int n) {
        return n <= 1 ? n : fibp(n - 1) + fibp(n - 2);
    }
}
