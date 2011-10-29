package org.enumerable.lambda.support.functionaljava;

import static fj.data.Array.*;
import static org.enumerable.lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;

import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

import fj.data.Array;

public final class Array_foldLeft {
    @LambdaParameter
    static Integer i1, i2;

    @Test
    public void test() {
        final Array<Integer> a = array(97, 44, 67, 3, 22, 90, 1, 77, 98, 1078, 6, 64, 6, 79, 42);
        final int b = a.foldLeft(λ(i1, i2, i1 + i2), 0);
        assertEquals(1774, b); // 1774
    }
}
