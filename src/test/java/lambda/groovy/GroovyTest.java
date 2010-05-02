package lambda.groovy;

import static java.util.Arrays.*;
import static lambda.Parameters.*;
import static lambda.groovy.LambdaGroovy.*;
import static org.junit.Assert.*;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lambda.Fn1;
import lambda.Lambda;
import lambda.clojure.ClojureTest;
import lambda.clojure.LambdaClojure;
import lambda.enumerable.Enumerable;
import lambda.groovy.LambdaGroovy.ClosureFn2;
import lambda.javascript.JavaScriptTest;
import lambda.javascript.LambdaJavaScript;
import lambda.jruby.JRubyTest;
import lambda.jruby.LambdaJRuby;

import org.jruby.RubyProc;
import org.junit.Before;
import org.junit.Test;

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
    public void convertFnToClosure() throws ScriptException {
        Closure closure = toClosure(Lambda.λ(s, s.toUpperCase()));
        assertEquals("HELLO", closure.call(new Object[] { "hello" }));
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
        assertEquals(6, groovy.eval("f(2, 3)"));
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

    @Before
    public void initEngine() {
        groovy = getGroovyEngine();
    }

    public static ScriptEngine getGroovyEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("Groovy");
    }
}
