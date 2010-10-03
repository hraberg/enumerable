package lambda.support.jruby;

import static java.util.Arrays.*;
import static lambda.Parameters.*;
import static lambda.support.jruby.LambdaJRuby.*;
import static org.junit.Assert.*;
import groovy.lang.Closure;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lambda.Fn1;
import lambda.Lambda;
import lambda.enumerable.Enumerable;
import lambda.support.clojure.ClojureTest;
import lambda.support.clojure.LambdaClojure;
import lambda.support.groovy.GroovyTest;
import lambda.support.groovy.LambdaGroovy;
import lambda.support.javascript.JavaScriptTest;
import lambda.support.javascript.LambdaJavaScript;
import lambda.support.scala.LambdaScala;
import lambda.support.scala.ScalaTest;
import lambda.support.scala.ScalaTest.ScalaInterpreter;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

import scala.Function2;
import sun.org.mozilla.javascript.internal.Function;
import clojure.lang.IFn;

public class JRubyTest {
    ScriptEngine rb;

    @Test
    public void interactingWithJRuby() throws ScriptException {
        rb.put("block", lambda(n, n * 2));
        assertEquals(asList(2L, 4L, 6L), rb.eval("[1, 2, 3].collect &block"));
    }

    @Test
    public void defaultValuesForJRubyProcs() throws ScriptException {
        rb.put("block", lambda(n = 2, n * 2));
        assertEquals(4L, rb.eval("block.call"));
        rb.put("block", lambda(n, m = 2, n * m));
        assertEquals(8L, rb.eval("block.call 4"));
        rb.put("block", lambda(n = 2, m = 2, n * m));
        assertEquals(4L, rb.eval("block.call"));
        rb.put("block", lambda(s, n = 2, m = 2, s + n * m));
        assertEquals("#: 4", rb.eval("block.call '#: '"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultValuesThrowsExceptionIfOnlyProvidedForEarlierParameterForJRubyProcs() throws ScriptException {
        rb.put("block", lambda(n = 2, m, n * m));
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultValuesThrowsExceptionIfOnlyProvidedForMiddleParameterForJRubyProcs() throws ScriptException {
        rb.put("block", lambda(s, n = 2, m, n * m));
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultValuesThrowsExceptionIfOnlyProvidedForAllEarlierParametersForJRubyProcs()
            throws ScriptException {
        rb.put("block", lambda(s = "", n = 2, m, n * m));
    }

    @Test
    public void convertFnToRubyProc() throws ScriptException {
        Ruby ruby = Ruby.getGlobalRuntime();

        RubyProc proc = toProc(Lambda.λ(s, s.toUpperCase()));
        assertEquals(ruby.newString("HELLO"), proc.call(ruby.getThreadService().getCurrentContext(),
                new IRubyObject[] { ruby.newString("hello") }));
    }

    @Test
    public void convertFnToRubyProcKeepsDefaultValues() throws ScriptException {
        Ruby ruby = Ruby.getGlobalRuntime();

        RubyProc proc = toProc(Lambda.λ(s = "world", s.toUpperCase()));
        assertEquals(ruby.newString("WORLD"), proc.call(ruby.getThreadService().getCurrentContext(),
                new IRubyObject[] {}));
    }

    @Test(expected = RaiseException.class)
    public void convertedRubyProcRaisesArgumentErrorWhenCalledWithTooFewArguments() throws ScriptException {
        Ruby ruby = Ruby.getGlobalRuntime();
        try {
            RubyProc proc = toProc(Lambda.λ(s, s.toUpperCase()));
            proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]);
        } catch (RaiseException e) {
            assertEquals(ruby.getArgumentError(), e.getException().getType());
            throw e;
        }
    }

    @Test(expected = RaiseException.class)
    public void convertedRubyProcRaisesArgumentErrorWhenCalledWithTooManyArguments() throws ScriptException {
        Ruby ruby = Ruby.getGlobalRuntime();
        try {
            RubyProc proc = toProc(Lambda.λ(s, s.toUpperCase()));
            proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] { ruby.newString("hello"),
                    ruby.newString("world") });
        } catch (RaiseException e) {
            assertEquals(ruby.getArgumentError(), e.getException().getType());
            throw e;
        }
    }

    @Test
    public void convertRubyProcToFn() throws ScriptException {
        RubyProc proc = (RubyProc) rb.eval("lambda {|s| s.upcase}");
        assertEquals("HELLO", toFn1(proc).call("hello"));
    }

    @Test
    public void convertRubyMethodProcToFn() throws ScriptException {
        RubyProc proc = (RubyProc) rb.eval("(\"hello\".method :upcase).to_proc");
        assertEquals("HELLO", toFn0(proc).call());
    }

    @Test
    public void convertRubyMethodProcToFnKeepsDefaultValues() throws ScriptException {
        RubyProc proc = (RubyProc) rb.eval("def my_upcase(s = 'world') s.upcase end; method(:my_upcase).to_proc");
        assertEquals("WORLD", toFn1(proc).call());
    }

    @Test
    public void interactingWithEnumerableJava() throws Exception {
        List<Integer> list = asList(1, 2, 3);
        Fn1<Object, Object> block = toFn1((RubyProc) rb.eval("lambda {|n| n * 2}"));
        assertEquals(asList(2L, 4L, 6L), Enumerable.collect(list, block));
    }

    @Test
    public void interactingWithClojure() throws Exception {
        Ruby ruby = Ruby.getGlobalRuntime();

        IFn star = (IFn) ClojureTest.getClojureEngine().eval("*");
        RubyProc proc = toProc(LambdaClojure.toFn2(star));

        assertEquals(ruby.newFixnum(6), proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                ruby.newFixnum(2), ruby.newFixnum(3) }));

        rb.put("block", proc);
        assertEquals(120L, rb.eval("[1, 2, 3, 4, 5].inject &block"));
    }

    @Test
    public void interactingWithJavaScript() throws Exception {
        Ruby ruby = Ruby.getGlobalRuntime();
        ScriptEngine js = JavaScriptTest.getJavaScriptEngine();

        Function f = (Function) js.eval("var f = function(n, m) { return n * m; }; f;");
        RubyProc proc = toProc(LambdaJavaScript.toFn2(f));

        assertEquals(ruby.newFloat(6), proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                ruby.newFixnum(2), ruby.newFixnum(3) }));

        rb.put("block", proc);
        assertEquals(120.0, rb.eval("[1, 2, 3, 4, 5].inject &block"));
    }

    @Test
    public void interactingWithGroovy() throws Exception {
        Ruby ruby = Ruby.getGlobalRuntime();
        ScriptEngine groovy = GroovyTest.getGroovyEngine();

        Closure closure = (Closure) groovy.eval("{ n, m -> n * m }");
        RubyProc proc = toProc(LambdaGroovy.toFn2(closure));

        assertEquals(ruby.newFixnum(6), proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                ruby.newFixnum(2), ruby.newFixnum(3) }));

        rb.put("block", proc);
        assertEquals(120L, rb.eval("[1, 2, 3, 4, 5].inject &block"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void interactingWithScala() throws Exception {
        Ruby ruby = Ruby.getGlobalRuntime();
        ScalaInterpreter scala = ScalaTest.getScalaInterpreter();

        Function2<Integer, Integer, Integer> f = (Function2<Integer, Integer, Integer>) scala.eval("(n: Long, m: Long) => { n * m }");
        RubyProc proc = toProc(LambdaScala.toFn2(f));

        assertEquals(ruby.newFixnum(6), proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                ruby.newFixnum(2), ruby.newFixnum(3) }));

        rb.put("block", proc);
        assertEquals(120L, rb.eval("[1, 2, 3, 4, 5].inject &block"));
    }

    @Before
    public void initEngine() {
        rb = getJRubyEngine();
    }

    public static ScriptEngine getJRubyEngine() {
        System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("jruby");
    }
}
