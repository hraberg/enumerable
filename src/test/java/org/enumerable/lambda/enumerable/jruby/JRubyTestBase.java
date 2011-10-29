package org.enumerable.lambda.enumerable.jruby;

import static java.lang.System.*;
import static org.enumerable.lambda.exception.UncheckedException.*;
import static org.jruby.javasupport.JavaEmbedUtils.*;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;


import org.enumerable.lambda.Fn0;
import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Fn2;
import org.enumerable.lambda.support.jruby.JRubyTest;
import org.enumerable.lambda.weaving.Debug;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyProc;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;

public class JRubyTestBase {
    public static boolean debug = Debug.debug;
    public ScriptEngine rb = JRubyTest.getJRubyEngine();

    @Before
    public void monkeyPatchJRubyEnumerableToUseEnumerableJava() throws ScriptException {
        require(enumerableJava());
    }

    public String enumerableJava() {
        return "enumerable_java";
    }

    public void load(String test) throws ScriptException {
        rb.eval(new InputStreamReader(JRubyTestBase.class.getResourceAsStream(test)));
    }

    public void require(String file) throws ScriptException {
        rb.eval("require '" + file + "'");
    }

    public Object eval(String script) throws ScriptException {
        return rb.eval(script);
    }

    void testUnit(String file, String testClass) throws ScriptException {
        StringWriter writer = new StringWriter();
        Writer originalWriter = rb.getContext().getWriter();
        rb.getContext().setWriter(writer);
        try {
            require(file);
            require("test/unit/ui/console/testrunner");
            eval("r = Test::Unit::UI::Console::TestRunner.run(" + testClass + ")");
            eval("raise r.to_s unless r.passed?");

            if (debug)
                out.println(writer);
        } catch (ScriptException e) {
            out.println(writer);
            throw uncheck(e);
        } finally {
            rb.getContext().setWriter(originalWriter);
        }
    }

    public static void debug(String msg) {
        if (debug)
            out.println(msg);
    }

    @SuppressWarnings("serial")
    public static Fn0<Object> toFn0(final RubyProc proc) {
        return new Fn0<Object>() {
            public Object call() {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]));
            }
        };
    }

    @SuppressWarnings("serial")
    public static Fn1<Object, Object> toFn1(final RubyProc proc) {
        return new Fn1<Object, Object>() {
            public Object call(Object a1) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(),
                        new IRubyObject[] { javaInRubyToRuby(ruby, a1) }));
            }
        };
    }

    @SuppressWarnings("serial")
    public static Fn2<Object, Object, Object> toFn2(final RubyProc proc) {
        return new Fn2<Object, Object, Object>() {
            public Object call(Object a1, Object a2) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                        javaInRubyToRuby(ruby, a1), javaInRubyToRuby(ruby, a2) }));
            }
        };
    }

    protected static IRubyObject javaInRubyToRuby(Ruby ruby, Object value) {
        if (value instanceof List<?> && !(value instanceof RubyArray)) {
            RubyArray array = RubyArray.newArray(ruby);
            array.addAll((Collection<?>) value);
            return array;
        }

        if (value instanceof Map<?, ?> && !(value instanceof RubyHash)) {
            RubyHash hash = RubyHash.newHash(ruby);
            hash.putAll((Map<?, ?>) value);
            return hash;
        }
        return javaToRuby(ruby, value);
    }
}
