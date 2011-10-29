package org.enumerable.lambda.support.clojure;

import static clojure.lang.RT.*;
import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.support.clojure.ClojureSeqs.*;
import static org.enumerable.lambda.support.clojure.ClojureSeqs.Vars.*;
import static org.enumerable.lambda.support.clojure.LambdaClojure.*;
import static org.junit.Assert.*;
import groovy.lang.Closure;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import org.enumerable.lambda.Lambda;
import org.enumerable.lambda.enumerable.Enumerable;
import org.enumerable.lambda.support.clojure.LambdaClojure;
import org.enumerable.lambda.support.groovy.GroovyTest;
import org.enumerable.lambda.support.groovy.LambdaGroovy;
import org.enumerable.lambda.support.javascript.JavaScriptTest;
import org.enumerable.lambda.support.javascript.LambdaJavaScript;
import org.enumerable.lambda.support.jruby.JRubyTest;
import org.enumerable.lambda.support.jruby.LambdaJRuby;
import org.enumerable.lambda.support.scala.LambdaScala;
import org.enumerable.lambda.support.scala.ScalaTest;
import org.enumerable.lambda.support.scala.ScalaTest.ScalaInterpreter;
import org.jruby.RubyProc;
import org.junit.Before;
import org.junit.Test;

import scala.Function2;
import sun.org.mozilla.javascript.internal.Function;
import clojure.lang.APersistentMap;
import clojure.lang.APersistentSet;
import clojure.lang.APersistentVector;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureTest {
    ScriptEngine clj;

    @Test
    public void inUserNamespaceByDefault() throws Exception {
        assertEquals("user", eval("*ns*").toString());
    }

    @Test
    public void initDoesNotChangeNamespaceOtherThanClojureCore() throws Exception {
        try {
            CURRENT_NS.doReset(Namespace.findOrCreate(Symbol.create("my-ns")));
            LambdaClojure.init();
            assertEquals("my-ns", CURRENT_NS.get().toString());
        } finally {
            CURRENT_NS.doReset(Namespace.findOrCreate(Symbol.create("user")));
        }
    }

    @Test
    public void evalUsingThreadBindings() throws Exception {
        try {
            Var.pushThreadBindings(RT.map(RT.CURRENT_NS, RT.CURRENT_NS.deref()));
            assertEquals("my-ns", eval("(in-ns 'my-ns)").toString());
        } finally {
            Var.popThreadBindings();
        }
        assertEquals("user", eval("*ns*").toString());

    }

    @Test
    public void clojureEngineDoesNotChangeNamespaceBetweenInvocations() throws Exception {
        assertEquals("my-ns", clj.eval("(in-ns 'my-ns)").toString());
        assertEquals("user", clj.eval("*ns*").toString());
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
    public void defaultValuesForIFns() throws ScriptException {
        defn("f", fn(n = 2, n * 2));
        assertEquals(4, eval("(f)"));
        defn("f", fn(n, m = 2, n * m));
        assertEquals(8, eval("(f 4)"));
        defn("f", fn(n = 2, m = 2, n * m));
        assertEquals(4, eval("(f)"));
        defn("f", fn(s, n = 2, m = 2, s + n * m));
        assertEquals("#: 4", eval("(f \"#: \")"));
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

        ISeq odd = list(1L, 3L, 5L);
        assertEquals(odd, (filter(isOdd, v)));
        assertEquals(odd, eval(filter, isOdd, v));
        assertEquals(odd, eval("(filter odd? v)"));

        IFn isEven = defn("is-even?", toIFn(Lambda.λ(n, n % 2 == 0)));

        ISeq even = list(2L, 4L);
        assertEquals(even, (filter(isEven, v)));
        assertEquals(even, eval(filter, isEven, v));
        assertEquals(even, eval("(filter is-even? v)"));
    }

    @Test
    public void interactingWithClojureEngine() throws Exception {
        eval("(def v [1 2 3 4 5])");

        defn("times", fn(n, m, n * m));

        Integer factorial = 120;
        assertEquals(factorial, clj.eval("(reduce times 1 v)"));

        ISeq odd = list(1L, 3L, 5L);
        assertEquals(odd, clj.eval("(filter odd? v)"));

        defn("is-even?", toIFn(Lambda.λ(n, n % 2 == 0)));

        ISeq even = list(2L, 4L);
        assertEquals(even, clj.eval("(filter is-even? v)"));
    }

    @Test
    public void convertFnToIFn() throws Exception {
        IFn fn = toIFn(Lambda.λ(s, s.toUpperCase()));
        assertEquals("HELLO", fn.invoke("hello"));
    }

    @Test
    public void convertFnToIFnKeepsDefaultValues() throws Exception {
        IFn fn = toIFn(Lambda.λ(s = "world", s.toUpperCase()));
        assertEquals("WORLD", fn.invoke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertedFnToIFnThrowsArityWhenCalledWithTooFewArguments() throws Exception {
        IFn fn = toIFn(Lambda.λ(s, s.toUpperCase()));
        fn.invoke();
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertedFnToIFnThrowsArityWhenCalledWithTooManyArguments() throws Exception {
        IFn fn = toIFn(Lambda.λ(s, s.toUpperCase()));
        fn.invoke("hello", "world");
    }

    @Test
    public void convertFnToIFnHandlesWithMeta() throws Exception {
        IFn fn = (IFn) toIFn(Lambda.λ(s = "world", s.toUpperCase()));
        defn("to-upper-case", fn);

        IFn fnWithMeta = (IFn) clj.eval("(with-meta to-upper-case {:hello \"world\"})");
        assertNotSame(fn, fnWithMeta);

        defn("to-upper-case-with-meta", fnWithMeta);
        IPersistentMap meta = (IPersistentMap) clj.eval("(meta to-upper-case-with-meta)");
        assertEquals(1, meta.count());
        assertEquals("world", meta.valAt(clj.eval(":hello")));

        assertEquals("HELLO", fnWithMeta.invoke("hello"));
        assertEquals("WORLD", fnWithMeta.invoke());
   }

    @Test
    public void convertIFnToFn() throws ScriptException {
        IFn star = eval("*");
        assertEquals(6L, toFn2(star).call(2, 3));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void interactingWithEnumerableJava() throws Exception {
        APersistentVector v = (APersistentVector) clj.eval("[1 2 3 4 5]");
        IFn star = (IFn) clj.eval("*");

        assertEquals(120L, Enumerable.inject(v, 1, toFn2(star)));
    }

    @Test
    public void interactingWithJRuby() throws Exception {
        ScriptEngine rb = JRubyTest.getJRubyEngine();

        RubyProc proc = (RubyProc) rb.eval(":*.to_proc");
        IFn times = toIFn(LambdaJRuby.toFn2(proc));

        assertEquals(6L, times.invoke(2, 3));

        defn("times-rb", times);
        assertEquals(120L, clj.eval("(reduce times-rb 1 [1, 2, 3, 4, 5])"));
    }

    @Test
    public void interactingWithJavaScript() throws Exception {
        ScriptEngine js = JavaScriptTest.getJavaScriptEngine();

        Function f = (Function) js.eval("var f = function(n, m) { return n * m; }; f;");
        IFn times = toIFn(LambdaJavaScript.toFn2(f));

        assertEquals(6.0, times.invoke(2, 3));

        defn("times-js", times);
        assertEquals(120.0, clj.eval("(reduce times-js 1 [1, 2, 3, 4, 5])"));
    }

    @Test
    public void interactingWithGroovy() throws Exception {
        ScriptEngine groovy = GroovyTest.getGroovyEngine();

        Closure<?> closure = (Closure<?>) groovy.eval("{ n, m -> n * m }");
        IFn times = toIFn(LambdaGroovy.toFn2(closure));

        assertEquals(6, times.invoke(2, 3));

        defn("times-groovy", times);
        assertEquals(120L, clj.eval("(reduce times-groovy 1 [1, 2, 3, 4, 5])"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void interactingWithScala() throws Exception {
        ScalaInterpreter scala = ScalaTest.getScalaInterpreter();

        Function2<Long, Long, Long> f = (Function2<Long, Long, Long>) scala.eval("(n: Long, m: Long) => n * m");
        IFn times = toIFn(LambdaScala.toFn2(f));

        assertEquals(6L, times.invoke(2L, 3L));

        defn("times-scala", times);
        assertEquals(120L, clj.eval("(reduce times-scala 1 [1, 2, 3, 4, 5])"));
    }

    @Before
    public void initEngine() {
        clj = getClojureEngine();
    }

    public static ScriptEngine getClojureEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("Clojure");
    }

    @Before
    public void ensureClojureIsInitialzed() {
        LambdaClojure.init();
    }
}
