package lambda.support.functionaljava;

import static fj.data.Array.*;
import static fj.data.List.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;
import lambda.annotation.LambdaParameter;

import org.junit.Test;

import fj.data.Array;

public final class Array_forall {
    @LambdaParameter
    static String s;
    @LambdaParameter
    static Character ch;

    @Test
    public void test() {
        final Array<String> a = array("hello", "There", "what", "day", "is", "it");
        final boolean b = a.forall(λ(s, fromString(s).forall(λ(ch, Character.isLowerCase(ch)))));
        assertFalse(b); // false ("There" is a counter-example; try
                               // removing it)
    }
}
