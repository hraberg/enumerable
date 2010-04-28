package lambda.enumerable.jruby;

import static java.lang.System.*;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import lambda.jruby.JRubyTest;
import lambda.weaving.Debug;

import org.junit.Before;

public class JRubyTestBase {
    static boolean debug = Debug.debug;
    ScriptEngine rb = JRubyTest.getJRubyEngine();

    @Before
    public void monkeyPatchJRubyEnumerableToUseEnumerableJava() throws ScriptException {
        require("enumerable_java");
    }

    void load(String test) throws ScriptException {
        rb.eval(new InputStreamReader(JRubyTestBase.class.getResourceAsStream(test)));
    }

    void require(String file) throws ScriptException {
        rb.eval("require '" + file + "'");
    }

    Object eval(String script) throws ScriptException {
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
