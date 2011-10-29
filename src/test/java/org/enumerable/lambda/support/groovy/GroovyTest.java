package org.enumerable.lambda.support.groovy;

import static java.util.Arrays.*;
import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.support.groovy.LambdaGroovy.*;
import static org.junit.Assert.*;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import org.codehaus.groovy.runtime.MethodClosure;
import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Lambda;
import org.enumerable.lambda.enumerable.Enumerable;
import org.enumerable.lambda.support.clojure.ClojureTest;
import org.enumerable.lambda.support.clojure.LambdaClojure;
import org.enumerable.lambda.support.groovy.LambdaGroovy.ClosureFn2;
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
import clojure.lang.IFn;

public class GroovyTest {
    ScriptEngine groovy;

    @Test
    public void interactingWithGroovy() throws ScriptException {
        groovy.put("c", closure(n, n * 2));
        assertEquals(4, groovy.eval("c(2);"));

        groovy.put("c", closure(b, !b));
        assertTrue((Boolean) groovy.eval("c(false);"));

        groovy.put("c", closure(s, s.toUpperCase()));
        assertEquals("HELLO", groovy.eval("c('hello');"));

        groovy.put("c", closure(obj, obj));
        assertNull(groovy.eval("c(null);"));
    }

    @Test
    public void defaultValuesForJavaScriptFunctions() throws ScriptException {
        groovy.put("c", closure(n = 2, n * 2));
        assertEquals(4, groovy.eval("c()"));
        groovy.put("c", closure(n, m = 2, n * m));
        assertEquals(8, groovy.eval("c(4)"));
        groovy.put("c", closure(n = 2, m = 2, n * m));
        assertEquals(4, groovy.eval("c()"));
    }

    @Test
    public void convertFnToClosure() throws ScriptException {
        Closure closure = toClosure(Lambda.λ(s, s.toUpperCase()));
        assertEquals("HELLO", closure.call(new Object[] { "hello" }));
    }

    @Test
    public void convertFnToClosureKeepsDefaultValues() throws ScriptException {
        Closure closure = toClosure(Lambda.λ(s = "world", s.toUpperCase()));
        assertEquals("WORLD", closure.call(new Object[0]));
    }

    @Test(expected = NullPointerException.class)
    public void convertedClosureThrowsExceptionWhenCalledWithTooFewArguments() throws ScriptException {
        Closure closure = toClosure(Lambda.λ(s, s.toUpperCase()));
        closure.call(new Object[0]);
    }

    @Test(expected = MissingMethodException.class)
    public void convertedClosureThrowsExceptionWhenCalledWithTooManyArguments() throws ScriptException {
        Closure closure = toClosure(Lambda.λ(s, s.toUpperCase()));
        closure.call(new Object[] { "hello", "world" });
    }

    @Test
    public void convertClosureToFn() throws ScriptException {
        Closure closure = (Closure) groovy.eval("{ it -> it.toUpperCase() }");
        assertEquals("HELLO", toFn1(closure).call("hello"));
    }

    @Test
    public void convertGroovyMethodToFn() throws ScriptException {
        MethodClosure closure = (MethodClosure) groovy.eval("'hello'.&toUpperCase");
        assertEquals("HELLO", toFn0(closure).call());
    }

    @Test
    public void interactingWithEnumerableJava() throws Exception {
        List<Integer> list = asList(1, 2, 3);
        Fn1<Object, Object> block = toFn1((Closure) groovy.eval("{ n -> n * 2 }"));
        assertEquals(asList(2, 4, 6), Enumerable.collect(list, block));
    }

    @Test
    public void interactingWithClojure() throws Exception {
        IFn star = (IFn) ClojureTest.getClojureEngine().eval("*");
        Closure closure = toClosure(LambdaClojure.toFn2(star));

        groovy.put("f", closure);
        assertEquals(6L, groovy.eval("f(2, 3)"));
    }

    @Test
    public void interactingWithJRuby() throws Exception {
        ScriptEngine rb = JRubyTest.getJRubyEngine();

        RubyProc proc = (RubyProc) rb.eval(":*.to_proc");
        Closure closure = toClosure(LambdaJRuby.toFn2(proc));

        groovy.put("f", closure);
        assertEquals(6L, groovy.eval("f(2, 3)"));
    }

    @Test
    public void interactingWithJavaScript() throws Exception {
        ScriptEngine js = JavaScriptTest.getJavaScriptEngine();

        Function f = (Function) js.eval("var f = function(n, m) { return n * m; }; f;");
        ClosureFn2 closure = toClosure(LambdaJavaScript.toFn2(f));

        assertEquals(6.0, closure.call(new Object[] { 2, 3 }));

        groovy.put("closure", closure);
        assertEquals(120.0, groovy.eval("[1, 2, 3, 4, 5].inject(1, closure)"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void interactingWithScala() throws Exception {
        ScalaInterpreter scala = ScalaTest.getScalaInterpreter();

        Function2<Integer, Integer, Integer> f = (Function2<Integer, Integer, Integer>) scala.eval("(n: Int, m: Int) => n * m");
        Closure closure = toClosure(LambdaScala.toFn2(f));

        groovy.put("closure", closure);
        assertEquals(120, groovy.eval("[1, 2, 3, 4, 5].inject(1, closure)"));
    }

    @Before
    public void initEngine() {
        groovy = getGroovyEngine();
    }

    public static ScriptEngine getGroovyEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("Groovy");
    }
}
