package lambda;

import static lambda.Lambda.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ControlBlocksTest {
    @Test
    public void and() throws Exception {
        assertTrue(λ(true).and(λ(true)));
        assertFalse(λ(true).and(λ(false)));
        assertFalse(λ(false).and(λ(true)));
        assertFalse(λ(false).and(λ(false)));
    }

    @Test
    public void or() throws Exception {
        assertTrue(λ(true).or(λ(true)));
        assertTrue(λ(true).or(λ(false)));
        assertTrue(λ(false).or(λ(true)));
        assertFalse(λ(false).or(λ(false)));
    }

    @Test
    public void isTrue() throws Exception {
        assertTrue(λ(true).isTrue());
        assertFalse(λ(false).isTrue());
    }

    @Test
    public void isFalse() throws Exception {
        assertTrue(λ(false).isFalse());
        assertFalse(λ(true).isFalse());
    }

    @Test
    public void unless() throws Exception {
        int i = 1;
        λ(i = 5).unless(λ(i > 5));
        assertEquals(5, i);
        λ(i = 10).unless(λ(i == 5));
        assertEquals(5, i);
    }

    @Test
    public void whileTrue() throws Exception {
        int i = 1;
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
}
