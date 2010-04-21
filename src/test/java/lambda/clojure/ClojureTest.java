package lambda.clojure;

import static clojure.lang.RT.*;
import static lambda.Parameters.*;
import static lambda.clojure.ClojureSeqs.*;
import static lambda.clojure.LambdaClojure.*;
import static org.junit.Assert.*;

import org.junit.Test;

import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureTest {
    @Test
    public void defnWithLambda() throws Exception {
        Var square = defn("square", fn(n, n * n));
        assertEquals(4, square.invoke(2));

        Var found = Var.find(Symbol.create(RT.CURRENT_NS.ns.toString(), "square"));
        assertSame(square, found);

        ISeq squares = (map(square, list(2, 4)));
        assertEquals(list(4, 16), squares);
    }

    @Test
    public void basicSequenceOperations() throws Exception {
        ISeq map = (map(fn(s, s.toUpperCase()), vector("hello", "world")));
        assertEquals(list("HELLO", "WORLD"), map);

        ISeq filter = (filter(fn(n, n % 2 == 0), set(1, 2, 3, 4, 5)));
        assertEquals(list(2, 4), filter);

        Object reduce = (reduce(fn(n, m, n * m), list(1, 2, 3, 4, 5)));
        assertEquals(120, reduce);
    }

    @Test
    public void varargsExpansion() throws Exception {
        ISeq concat = (concat(list(1), list(2), list(3, 4), list(5)));
        assertEquals(list(1, 2, 3, 4, 5), concat);

        ISeq interleave = (interleave(list(2, 4), list(3, 6), list(4, 8), list(5, 10), list(6, 12)));
        assertEquals(vector(2, 3, 4, 5, 6, 4, 6, 8, 10, 12), interleave);

        ISeq mapcat = (mapcat(fn(n, m, list(n * m)), list(2, 4), list(3, 6)));
        assertEquals(list(6, 24), mapcat);
    }
}
