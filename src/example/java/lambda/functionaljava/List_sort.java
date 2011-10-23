package lambda.functionaljava;

import static fj.Ord.*;
import static fj.Show.*;
import static fj.data.List.*;
import static lambda.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.Ordering;
import fj.data.List;

public final class List_sort {
    @LambdaParameter
    static Integer a1, a2;

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final List<Integer> a = list(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final List<Integer> b = a.sort(ord(λ(a1, λ(a2, a1.compareTo(a2) < 0 ? Ordering.LT : a1.compareTo(a2) == 0 ? Ordering.EQ : Ordering.GT))));
        listShow(intShow()).println(b); // [1,3,6,6,22,42,44,64,67,77,79,90,97,98,1078]
    }
}
