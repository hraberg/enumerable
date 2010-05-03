package lambda;

import static lambda.Lambda.*;
import static lambda.exception.UncheckedException.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ControlBlocksTest {
    @Test
    public void unless() throws Exception {
        int i = 1;
        λ(i = 5).unless(i > 5);
        assertEquals(5, i);
        λ(i = 10).unless(i == 5);
        assertEquals(5, i);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlessWithException() throws Exception {
        String[] args = new String[0];
        λ(raise(new IllegalArgumentException())).unless(args.length > 0);
    }

    @Test
    public void whileTrue() throws Exception {
        int i = 0;
        λ(i < 5).whileTrue(λ(i++));
        assertEquals(5, i);
    }

    @Test
    public void ifTrue() throws Exception {
        int i = 1;
        λ(i < 5).ifTrue(λ(i = 5));
        assertEquals(5, i);
        λ(i < 5).ifTrue(λ(i = 10));
        assertEquals(5, i);
    }

    @Test
    public void ifFalse() throws Exception {
        int i = 1;
        λ(i > 5).ifFalse(λ(i = 5));
        assertEquals(5, i);
        λ(i >= 5).ifFalse(λ(i = 10));
        assertEquals(5, i);
    }

    public static Object cond(Fn0<?>... clauses) {
        assert clauses.length % 2 == 0;
        for (int i = 0; i < clauses.length; i += 2)
            if (Fn0.isNotFalseOrNull(clauses[i].call()))
                return clauses[i + 1].call();
        return null;
    }

    @Test
    public void cond() throws Exception {
        String result;
        cond(λ(false), λ(result = "hello"),//
                λ(2 / 2 == 2), λ(result = "space"), //
                λ(true), λ(result = "world"));
        assertEquals("world", result);
    }
}
