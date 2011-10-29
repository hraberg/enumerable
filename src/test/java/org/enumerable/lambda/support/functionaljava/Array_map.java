package org.enumerable.lambda.support.functionaljava;

import static fj.Show.*;
import static fj.data.Array.*;
import static org.enumerable.lambda.support.functionaljava.Array_bind.*;
import static org.enumerable.lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;

import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

import fj.data.Array;

public final class Array_map {
    @LambdaParameter
    static Integer i1, i2;

    @Test
    public void test() {
        final Array<Integer> a = array(1, 2, 3);
        final Array<Integer> b = a.map(λ(i1, i2, i1 + i2).f(42));
        assertEquals("{43,44,45}", arrayShow(intShow()).showS(b)); // {43,44,45}
    }
}
