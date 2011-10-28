package lambda.support.functionaljava;

import static fj.Show.*;
import static fj.data.Array.*;
import static lambda.support.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;
import lambda.annotation.LambdaParameter;

import org.junit.Test;

import fj.data.Array;

public final class Array_filter {
    @LambdaParameter
    static Integer i;

    @Test
    public void test() {
        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final Array<Integer> b = a.filter(λ(i, i % 2 == 0));
        assertEquals("{44,22,90,98,1078,6,64,6,42}", arrayShow(intShow()).showS(b)); // {44,22,90,98,1078,6,64,6,42}
    }
}
