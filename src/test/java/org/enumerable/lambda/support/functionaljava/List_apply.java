package org.enumerable.lambda.support.functionaljava;

import static fj.Show.*;
import static fj.data.List.*;
import static org.enumerable.lambda.support.functionaljava.Array_bind.*;
import static org.enumerable.lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;

import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

import fj.F;
import fj.data.List;

public class List_apply {
    @LambdaParameter
    static Integer i1, i2;

    @Test
    public void test() {
        final List<F<Integer, Integer>> fs = single(λ(i1, i2, i1 - i2).f(2)).cons(λ(i1, i2, i1 * i2).f(2)).cons(λ(i1, i2, i1 + i2).f(2));
        final List<Integer> three = list(3);
        assertEquals("<5,6,-1>", listShow(intShow()).showS(three.apply(fs))); // Prints out: <5,6,-1>
    }
}