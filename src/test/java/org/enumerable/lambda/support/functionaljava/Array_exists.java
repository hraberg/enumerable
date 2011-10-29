package org.enumerable.lambda.support.functionaljava;

import static fj.data.Array.*;
import static fj.data.List.*;
import static junit.framework.Assert.*;
import static org.enumerable.lambda.support.functionaljava.LambdaFunctionalJava.*;

import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

import fj.data.Array;

public final class Array_exists {
    @LambdaParameter
    static String s;
    @LambdaParameter
    static Character ch;

    @Test
    public void test() {
        final Array<String> a = array("Hello", "There", "what", "DAY", "iS", "iT");
        final boolean b = a.exists(λ(s, fromString(s).forall(λ(ch, Character.isLowerCase(ch)))));
        
        assertTrue(b); // true ("what" provides the only example; try
                               // removing it)
    }
}
