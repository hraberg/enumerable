package org.enumerable.lambda;

import static java.lang.System.*;
import static org.enumerable.lambda.Lambda.*;
import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.enumerable.Enumerable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.enumerable.lambda.Fn0;
import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Lambda;
import org.enumerable.lambda.enumerable.primitives.EnumerableInts;
import org.enumerable.lambda.primitives.Fn1ItoI;
import org.enumerable.lambda.primitives.LambdaPrimitives;
import org.enumerable.lambda.weaving.LambdaLoader;
import org.enumerable.lambda.weaving.Version;


public class MicroBench {
    public static void main(String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);
        out.println("[microbench] " + Version.getVersionString());

        int n = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        int times = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        new MicroBench().run(n, times);
    }

    void run(int n, int times) {
        assert fibo.getClass().isAssignableFrom(fibp.getClass());
        assert !fibp.getClass().isAssignableFrom(fibo.getClass());

        bench("object fib method", times, λ(fibo(n)));
        bench("object fib lambda", times, λ(fibo.call(n)));

        bench("primitive fib method", times, λ(fibp(n)));
        bench("primitive fib lambda", times, λ(fibp.call(n)));

        out.println();

        bench("object collect for loop", times, λ(methodCollect()));
        bench("object collect lambda", times, λ(lambdaCollect()));

        bench("primitive collect for loop", times, λ(methodCollectP()));
        bench("primitive collect lambda", times, λ(lambdaCollectP()));

        out.println();

        bench("object inject for loop", times, λ(methodInject()));
        bench("object inject lambda", times, λ(lambdaInject()));

        bench("primitive inject for loop", times, λ(methodInjectP()));
        bench("primitive inject lambda", times, λ(lambdaInjectP()));
    }

    List<String> methodCollect() {
        List<String> result = new ArrayList<String>();
        for (Integer i : integers)
            result.add(i + "");
        return result;
    }

    List<String> lambdaCollect() {
        return collect(integers, λ(n, n + ""));
    }

    String[] methodCollectP() {
        String[] result = new String[length];
        int idx = 0;
        for (int i : ints)
            result[idx++] = i + "";
        return result;
    }

    String[] lambdaCollectP() {
        return EnumerableInts.collect(ints, LambdaPrimitives.λ(n, n + ""), String.class);
    }

    Integer methodInject() {
        Integer result = 1;
        for (Integer i : integers)
            result *= i;
        return result;
    }

    Integer lambdaInject() {
        return inject(integers, λ(n, m, n * m));
    }

    int methodInjectP() {
        int result = 1;
        for (int i : ints)
            result *= i;
        return result;
    }

    int lambdaInjectP() {
        return EnumerableInts.inject(ints, LambdaPrimitives.λ(n, m, n * m));
    }

    long bench(String name, int times, Fn0<?> block) {
        long now = currentTimeMillis();
        times(times, block);
        long time = currentTimeMillis() - now;
        out.println(name + " run " + times + " times, average " + time / times + " ms.");
        return time;
    }

    Fn1<Integer, Integer> fibo;
    Fn1ItoI fibp;

    int length = 100000;

    List<Integer> integers = new ArrayList<Integer>(length);
    int[] ints = new int[length];

    MicroBench() {
        fibo = Lambda.λ(n, n <= 1 ? n : fibo.call(n - 1) + fibo.call(n - 2));
        fibp = LambdaPrimitives.λ(n, n <= 1 ? n : fibp.call(n - 1) + fibp.call(n - 2));

        Random random = new Random();
        for (int i = 0; i < length; i++)
            integers.add(ints[i] = random.nextInt());
    }

    Integer fibo(Integer n) {
        return n <= 1 ? n : fibo(n - 1) + fibo(n - 2);
    }

    int fibp(int n) {
        return n <= 1 ? n : fibp(n - 1) + fibp(n - 2);
    }
}
