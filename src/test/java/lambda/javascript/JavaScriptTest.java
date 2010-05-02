package lambda.javascript;

import static java.util.Arrays.*;
import static lambda.Parameters.*;
import static lambda.javascript.LambdaJavaScript.*;
import static org.junit.Assert.*;
import groovy.lang.Closure;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lambda.Fn1;
import lambda.Lambda;
import lambda.clojure.ClojureTest;
import lambda.clojure.LambdaClojure;
import lambda.enumerable.Enumerable;
import lambda.groovy.GroovyTest;
import lambda.groovy.LambdaGroovy;
import lambda.javascript.LambdaJavaScript.FunctionFn2;
import lambda.jruby.JRubyTest;
import lambda.jruby.LambdaJRuby;

import org.jruby.RubyProc;
import org.junit.Before;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import clojure.lang.IFn;

public class JavaScriptTest {
    ScriptEngine js;

    @Test
    public void interactingWithJavaScript() throws ScriptException {
        js.put("f", function(n, n * 2));
        assertEquals(4.0, js.eval("f(2);"));

        js.put("f", function(b, !b));
        assertTrue((Boolean) js.eval("f(false);"));

        js.put("f", function(s, s.toUpperCase()));
        assertEquals("HELLO", js.eval("f('hello');"));

        js.put("f", function(obj, obj));
        assertNull(js.eval("f(undefined);"));
        assertNull(js.eval("f(null);"));
    }

    @Test
    public void convertFnToFunction() throws ScriptException {
        Function f = toFunction(Lambda.λ(s, s.toUpperCase()));
        assertEquals("HELLO", f.call(Context.getCurrentContext(), null, null, new Object[] { "hello" }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertedFunctionThrowsExceptionWhenCalledWithTooFewArguments() throws ScriptException {
        Function f = toFunction(Lambda.λ(s, s.toUpperCase()));
        f.call(Context.getCurrentContext(), null, null, new Object[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertedFunctionThrowsExceptionWhenCalledWithTooManyArguments() throws ScriptException {
        Function f = toFunction(Lambda.λ(s, s.toUpperCase()));
        f.call(Context.getCurrentContext(), null, null, new Object[] { "hello", "world" });
    }

    @Test
    public void convertFunctionToFn() throws ScriptException {
        Function f = (Function) js.eval("var f = function(s) { return s.toUpperCase(); }; f;");
        assertEquals("HELLO", toFn1(f).call("hello"));
    }

    @Test
    public void interactingWithEnumerableJava() throws Exception {
        List<Integer> list = asList(1, 2, 3);
        Fn1<Object, Object> block = toFn1((Function) js.eval("var f = function(n) { return n * 2}; f;"));
        assertEquals(asList(2.0, 4.0, 6.0), Enumerable.collect(list, block));
    }

    @Test
    public void interactingWithClojure() throws Exception {
        IFn star = (IFn) ClojureTest.getClojureEngine().eval("*");
        FunctionFn2 f = toFunction(LambdaClojure.toFn2(star));

        js.put("f", f);
        assertEquals(6.0, js.eval("f(2, 3)"));
    }

    @Test
    public void interactingWithJRuby() throws Exception {
        ScriptEngine rb = JRubyTest.getJRubyEngine();

        RubyProc proc = (RubyProc) rb.eval(":*.to_proc");
        FunctionFn2 f = toFunction(LambdaJRuby.toFn2(proc));

        js.put("f", f);
        assertEquals(6.0, js.eval("f(2, 3)"));
    }

    @Test
    public void interactingWithGroovy() throws Exception {
        ScriptEngine groovy = GroovyTest.getGroovyEngine();

        Closure closure = (Closure) groovy.eval("{ n, m -> n * m }");
        FunctionFn2 f = toFunction(LambdaGroovy.toFn2(closure));

        js.put("f", f);
        assertEquals(6.0, js.eval("f(2, 3)"));
    }

    @Before
    public void initEngine() {
        js = getJavaScriptEngine();
    }

    public static ScriptEngine getJavaScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("JavaScript");
    }
}
