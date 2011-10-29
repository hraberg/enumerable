package org.enumerable.lambda.enumerable.jruby;

import static java.lang.System.*;
import static org.enumerable.lambda.exception.UncheckedException.*;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.jruby.exceptions.RaiseException;

public class RubySpecTestBase extends JRubyTestBase {
    public static boolean specdoc = true;

    public String enumerableJava() {
        return "enumerable_java_rubyspec";
    }

    void mspec(String file) throws Exception {
        List<String> specs = new ArrayList<String>();
        specs.add("\"core/enumerable/" + file + "\"");
        mspec(specs);
    }

    void mspec(List<String> files) throws Exception {
        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();
        Writer originalOut = rb.getContext().getWriter();
        Writer originalErr = rb.getContext().getErrorWriter();

        if (!specdoc)
            rb.getContext().setWriter(stdout);
        if (!debug)
            rb.getContext().setErrorWriter(stderr);

        try {
            // We need to trick MSpec into thinking we're running a real ruby
            eval("RUBY_EXE = '/usr/bin/jruby'");
            // While telling it we're not, to skip specs for our "platform"
            eval("RUBY_PLATFORM = 'enumerable_java'");
            // We support Enumerable from 1.8.8
            eval("RUBY_VERSION = '1.8.8'");
            require("mspec");
            require("mspec/utils/script");
            // Identity won't work as JRuby will turn Ruby objects into Java
            // and then back again.
            eval("class EqualMatcher; def matches?(actual); @actual = actual; @actual == @expected; end; end");

            eval("formatter = SpecdocFormatter.new; formatter.register;");
            eval("MSpec.store :formatter, formatter");
            eval("MSpec.register_files " + files);
            eval("MSpec.process");

            try {
                eval("raise formatter.exceptions[0] unless MSpec.exit_code == 0");
            } catch (RaiseException e) {
                try {
                    fail(e.getException().message.asJavaString());
                } catch (AssertionError error) {
                    error.setStackTrace(e.getStackTrace());
                    throw error;
                }
            }
        } catch (ScriptException e) {
            out.println(stdout.toString());
            err.println(stderr.toString());
            throw uncheck(e);
        } finally {
            rb.getContext().setWriter(originalOut);
            rb.getContext().setErrorWriter(originalErr);
            eval("MSpec.unregister :exception, formatter; MSpec.unregister :before, formatter; "
                    + "MSpec.unregister :after, formatter; MSpec.unregister :finish, formatter; "
                    + "MSpec.unregister :enter, formatter; MSpec.register_exit(nil); "
                    + "MSpec.clear_current; MSpec.clear_modes; MSpec.clear_expectations");
        }
    }
}
