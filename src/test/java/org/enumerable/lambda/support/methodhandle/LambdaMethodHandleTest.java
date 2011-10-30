package org.enumerable.lambda.support.methodhandle;

import org.junit.Ignore;
import org.junit.Test;

import static org.enumerable.lambda.Lambda.λ;
import static org.enumerable.lambda.Parameters.s;
import static org.enumerable.lambda.support.methodhandle.LambdaMethodHandle.bind;
import static org.junit.Assert.assertEquals;

@Ignore("Behaves differently in Ant vs IDEA")
public class LambdaMethodHandleTest {
    @Test
    public void createMethodHandleFromLambda() throws Throwable {
        assertEquals("hello world", bind(λ(s, s + " world")).invoke("hello"));
    }
}
