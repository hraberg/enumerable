package lambda.enumerable.jruby;

import static java.lang.System.*;
import static lambda.exception.UncheckedException.*;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import lambda.jruby.JRubyTest;
import lambda.weaving.Debug;

public class JRubyTestBase {
    static boolean debug = Debug.debug;
    ScriptEngine rb = JRubyTest.getJRubyEngine();

    static {
        try {
            monkeyPatchJRubyEnumerableToUseEnumerableJava();
        } catch (ScriptException e) {
            throw uncheck(e);
        }
    }

    public static void monkeyPatchJRubyEnumerableToUseEnumerableJava() throws ScriptException {
        JRubyTest.getJRubyEngine().eval(
                new InputStreamReader(JRubyTestBase.class.getResourceAsStream("/enumerable_java.rb")));
    }

    void load(String test) throws ScriptException {
        rb.eval(new InputStreamReader(JRubyTestBase.class.getResourceAsStream(test)));
    }

    Object eval(String script) throws ScriptException {
        return rb.eval(script);
    }

    void testUnit(String file, String testClass) throws ScriptException {
        StringWriter writer = new StringWriter();
        Writer originalWriter = rb.getContext().getWriter();
        rb.getContext().setWriter(writer);
        try {
            load(file);
            eval("require 'test/unit/ui/console/testrunner'");
            eval("r = Test::Unit::UI::Console::TestRunner.run(" + testClass + ")");
            eval("raise r.to_s unless r.passed?");
        } catch (ScriptException e) {
            out.println(writer);
            throw e;
        } finally {
            rb.getContext().setWriter(originalWriter);
        }
    }

    public static void debug(String msg) {
        if (debug)
            out.println(msg);
    }
}
