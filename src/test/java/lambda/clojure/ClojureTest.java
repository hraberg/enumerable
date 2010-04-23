package lambda.clojure;

import static clojure.lang.RT.*;
import static lambda.Parameters.*;
import static lambda.clojure.ClojureSeqs.*;
import static lambda.clojure.ClojureSeqs.Vars.*;
import static lambda.clojure.LambdaClojure.*;
import static org.junit.Assert.*;

import javax.script.ScriptException;

import lambda.Lambda;
import lambda.enumerable.Enumerable;
import lambda.jruby.JRubyTest;
import lambda.jruby.LambdaJRuby;

import org.jruby.RubyProc;
import org.jruby.embed.jsr223.JRubyEngine;
import org.junit.Before;
import org.junit.Test;

import clojure.lang.APersistentMap;
import clojure.lang.APersistentSet;
import clojure.lang.APersistentVector;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureTest {
    @Test
    public void inUserNamespaceByDefault() throws Exception {
        assertEquals("user", RT.CURRENT_NS.get().toString());
    }

    @Test
    public void initDoesNotChangeNamespaceOtherThanClojureCore() throws Exception {
        try {
            CURRENT_NS.doReset(Namespace.findOrCreate(Symbol.create("my-ns")));
            LambdaClojure.init();
            assertEquals("my-ns", RT.CURRENT_NS.get().toString());
        } finally {
            CURRENT_NS.doReset(Namespace.findOrCreate(Symbol.create("user")));
        }
    }

    @Test
    public void defnLambda() throws Exception {
        Var square = defn("square", fn(n, n * n));
        assertEquals(4, square.invoke(2));

        Var found = var(CURRENT_NS.get().toString(), "square");
        assertSame(square, found);

        ISeq squares = (map(square, list(2, 4)));
        assertEquals(list(4, 16), squares);
    }

    @Test
    public void basicSequenceOperations() throws Exception {
        ISeq map = (map(fn(s, s.toUpperCase()), list("hello", "world")));
        assertEquals(list("HELLO", "WORLD"), map);

        ISeq filter = (filter(fn(n, n % 2 == 0), list(1, 2, 3, 4, 5)));
        assertEquals(list(2, 4), filter);

        Integer reduce = (reduce(fn(n, m, n * m), list(1, 2, 3, 4, 5)));
        assertEquals(120, reduce.intValue());
    }

    @Test
    public void creatingPersistentCollections() throws Exception {
        APersistentVector vector = (APersistentVector) vec(list("hello", "world"));
        assertEquals("hello", vector.invoke(0));
        assertEquals("world", vector.invoke(1));
        assertEquals("[\"hello\" \"world\"]", vector.toString());

        APersistentSet set = (APersistentSet) set(list("hello", "world"));
        assertEquals("hello", set.invoke("hello"));
        assertEquals("world", set.invoke("world"));
        assertEquals("#{\"hello\" \"world\"}", set.toString());

        APersistentMap map = (APersistentMap) zipmap(list("hello", "world"), list(1, 2));
        assertEquals(1, map.invoke("hello"));
        assertEquals(2, map.invoke("world"));
        assertEquals("{\"world\" 2, \"hello\" 1}", map.toString());
    }

    @Test
    public void varargsExpansion() throws Exception {
        ISeq concat = (concat(list(1), list(2), list(3, 4), list(5)));
        assertEquals(list(1, 2, 3, 4, 5), concat);

        ISeq interleave = (interleave(list(2, 4), list(3, 6), list(4, 8), list(5, 10), list(6, 12)));
        assertEquals(vector(2, 3, 4, 5, 6, 4, 6, 8, 10, 12), interleave);

        ISeq mapcat = (mapcat(fn(n, m, list(n * m)), list(2, 4), list(3, 6)));
        assertEquals(list(6, 24), mapcat);

        ISeq pmap = (pmap(fn(n, m, n * m), list(2, 4), list(3, 6)));
        assertEquals(list(6, 24), pmap);
    }

    @Test
    public void interactingWithClojure() throws Exception {
        eval("(def v [1 2 3 4 5])");
        IPersistentVector v = eval("v");

        IFn times = defn("times", fn(n, m, n * m));

        Integer factorial = 120;
        assertEquals(factorial, (reduce(times, 1, v)));
        assertEquals(factorial, eval(reduce, times, 1, v));
        assertEquals(factorial, eval("(reduce times 1 v)"));

        IFn isOdd = eval("odd?");

        ISeq odd = list(1, 3, 5);
        assertEquals(odd, (filter(isOdd, v)));
        assertEquals(odd, eval(filter, isOdd, v));
        assertEquals(odd, eval("(filter odd? v)"));

        IFn isEven = defn("is-even?", toIFn(Lambda.λ(n, n % 2 == 0)));

        ISeq even = list(2, 4);
        assertEquals(even, (filter(isEven, v)));
        assertEquals(even, eval(filter, isEven, v));
        assertEquals(even, eval("(filter is-even? v)"));
    }

    @Test
    public void convertFnToIFn() throws Exception {
        IFn fn = toIFn(Lambda.λ(s, s.toUpperCase()));
        assertEquals("HELLO", fn.invoke("hello"));
    }

    @Test
    public void convertIFnToFn() throws ScriptException {
        IFn star = eval("*");
        assertEquals(6, toFn2(star).call(2, 3));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void interactingWithEnumerableJava() throws Exception {
        APersistentVector v = eval("[1 2 3 4 5]");
        IFn star = eval("*");

        assertEquals(120, Enumerable.inject(v, 1, toFn2(star)));
    }

    @Test
    public void interactingWithJRuby() throws Exception {
        JRubyEngine instance = JRubyTest.getJRubyEngine();
        RubyProc proc = (RubyProc) instance.eval("lambda {|n, m| n * m}");
        IFn times = toIFn(LambdaJRuby.toFn2(proc));

        assertEquals(120L, reduce(times, list(1, 2, 3, 4, 5)));
    }

    @Before
    public void ensureClojureIsInitialzed() {
        LambdaClojure.init();
    }
}
