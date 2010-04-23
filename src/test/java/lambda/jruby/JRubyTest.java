package lambda.jruby;

import static java.util.Arrays.*;
import static lambda.Parameters.*;
import static lambda.jruby.LambdaJRuby.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lambda.Lambda;
import lambda.enumerable.Enumerable;

import org.jruby.RubyProc;
import org.jruby.embed.jsr223.JRubyEngine;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;

public class JRubyTest {
    @Test
    public void interactingWithJRuby() throws ScriptException {
        JRubyEngine instance = getJRubyEngine();

        instance.put("block", lambda(n, n * 2));
        assertEquals(asList(2L, 4L, 6L), instance.eval("[1, 2, 3].collect &block"));
    }

    @Test
    public void convertFnToRubyProc() throws ScriptException {
        RubyProc proc = toProc(Lambda.λ(s, s.toUpperCase()));
        assertEquals(ruby.newString("HELLO"), proc.call(ruby.getThreadService().getCurrentContext(),
                new IRubyObject[] { ruby.newString("hello") }));
    }

    @Test
    public void convertRubyProcToFn() throws ScriptException {
        JRubyEngine instance = getJRubyEngine();

        RubyProc proc = (RubyProc) instance.eval("lambda {|s| s.upcase}");
        assertEquals("HELLO", toFn1(proc).call("hello"));
    }

    @Test
    public void convertRubyMethodProcToFn() throws ScriptException {
        JRubyEngine instance = getJRubyEngine();

        RubyProc proc = (RubyProc) instance.eval("(\"hello\".method :upcase).to_proc");
        assertEquals("HELLO", toFn0(proc).call());
    }

    @Test
    public void interactingWithEnumerableJava() throws Exception {
        JRubyEngine instance = getJRubyEngine();

        List<Object> list = new ArrayList<Object>();
        list.addAll(asList(1, 2, 3));
        assertEquals(asList(2L, 4L, 6L), Enumerable.collect(list, toFn1((RubyProc) instance
                .eval("lambda {|n| n * 2}"))));
    }

    JRubyEngine getJRubyEngine() {
        System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
        ScriptEngineManager manager = new ScriptEngineManager();
        return (JRubyEngine) manager.getEngineByName("jruby");
    }
}
