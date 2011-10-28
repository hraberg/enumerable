package lambda.support.functionaljava;

import static fj.Show.*;
import static fj.data.Option.*;
import static lambda.support.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;
import lambda.annotation.LambdaParameter;

import org.junit.Test;

import fj.data.Option;

public final class Option_map {
    @LambdaParameter
    static Integer i1, i2;

    @Test
    public void test() {
        final Option<Integer> o1 = some(7);
        final Option<Integer> o2 = none();
        final Option<Integer> p1 = o1.map(λ(i1, i2, i1 + i2).f(42));
        final Option<Integer> p2 = o2.map(λ(i1, i2, i1 + i2).f(42));
        assertEquals("Some(49)", optionShow(intShow()).showS(p1)); // Some(49)
        assertEquals("None", optionShow(intShow()).showS(p2)); // None
    }
}
