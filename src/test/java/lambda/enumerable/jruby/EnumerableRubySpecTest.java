package lambda.enumerable.jruby;

import static java.lang.System.*;
import static lambda.exception.UncheckedException.*;

import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptException;

import org.junit.Test;

public class EnumerableRubySpecTest extends JRubyTestBase {
    @Test
    public void all_spec() throws Exception {
        mspec("core/enumerable/all_spec.rb");
    }

    @Test
    public void any_spec() throws Exception {
        mspec("core/enumerable/any_spec.rb");
    }

    void mspec(String file) throws Exception {
        StringWriter writer = new StringWriter();
        Writer originalWriter = rb.getContext().getWriter();
        rb.getContext().setWriter(writer);
        try {
            // We need to trick MSpec into thinking we're running a real ruby
            eval("RUBY_EXE = '/usr/bin/ruby'");
            require("mspec");
            require("mspec/utils/script");

            eval("formatter = DottedFormatter.new; formatter.register;");
            eval("MSpec.store :formatter, formatter");
            eval("MSpec.register_files ['" + file + "']");
            eval("MSpec.process");

            eval("raise formatter.exceptions[0] unless MSpec.exit_code == 0");

            if (debug)
                out.println(writer);
        } catch (ScriptException e) {
            out.println(writer);
            throw uncheck(e);
        } finally {
            rb.getContext().setWriter(originalWriter);
        }
    }
}
