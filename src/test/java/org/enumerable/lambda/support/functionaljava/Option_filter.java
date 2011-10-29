package org.enumerable.lambda.support.functionaljava;

import static fj.Show.*;
import static fj.data.Option.*;
import static org.enumerable.lambda.support.functionaljava.Array_bind.*;
import static org.enumerable.lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;

import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

import fj.data.Option;

public final class Option_filter {
    @LambdaParameter
    static Integer i;

    @Test
    public void test() {
        final Option<Integer> o1 = some(7);
        final Option<Integer> o2 = none();
        final Option<Integer> o3 = some(8);

        final Option<Integer> p1 = o1.filter(λ(i, i % 2 == 0));
        final Option<Integer> p2 = o2.filter(λ(i, i % 2 == 0));
        final Option<Integer> p3 = o3.filter(λ(i, i % 2 == 0));

        assertEquals("None", optionShow(intShow()).showS(p1)); // None
        assertEquals("None", optionShow(intShow()).showS(p2)); // None
        assertEquals("Some(8)", optionShow(intShow()).showS(p3)); // Some(8)
    }
}
