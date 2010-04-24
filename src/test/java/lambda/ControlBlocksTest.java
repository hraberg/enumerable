package lambda;

import static lambda.Lambda.*;
import static lambda.exception.UncheckedException.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import lambda.annotation.LambdaParameter;

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

    @LambdaParameter
    static BufferedReader r;

    @SuppressWarnings("serial")
    public static <C extends Closeable> Fn1<C, ?> with(final Fn1<C, ?> block) {
        return new Fn1<C, Object>() {
            Closeable c;

            public Object call() {
                try {
                    return block.call();
                } finally {
                    this.c = block.default$1;
                }
            }

            public Object call(C c) {
                this.c = c;
                return block.call(c);
            }

            public <B> B whileTrue(Fn0<B> block) {
                try {
                    return super.whileTrue(block);
                } finally {
                    if (c != null)
                        try {
                            c.close();
                        } catch (IOException silent) {
                        }
                }
            }
        };
    }

    boolean wasClosed;

    @Test
    public void with() throws Exception {
        BufferedReader br = new BufferedReader(new StringReader("hello\nworld") {
            public void close() {
                super.close();
                wasClosed = true;
            }
        });

        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);

        String line;
        with(λ(r = br, (line = r.readLine()) != null)).whileTrue(λ(w.printf(line + "\n")));

        assertTrue(wasClosed);
        assertEquals("hello\nworld\n", sw.toString());
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
