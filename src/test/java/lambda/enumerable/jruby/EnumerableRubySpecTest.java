package lambda.enumerable.jruby;

import static java.lang.System.*;
import static lambda.exception.UncheckedException.*;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptException;

import org.jruby.exceptions.RaiseException;
import org.junit.Ignore;
import org.junit.Test;

//@Ignore("Passes now, but not without some issues. Clashes with the other Ruby tests")
public class EnumerableRubySpecTest extends JRubyTestBase {
    public static boolean specdoc = true;

    @Test
    public void all_spec() throws Exception {
        mspec("all_spec.rb");
    }

    @Test
    public void any_spec() throws Exception {
        mspec("any_spec.rb");
    }

    @Test
    @Ignore("From newer 1.9 than the 1.8.7 backport")
    public void chunk_spec() throws Exception {
        mspec("chunk_spec.rb");
    }

    @Test
    @Ignore("From newer 1.9 than the 1.8.7 backport")
    public void collect_concat_spec() throws Exception {
        mspec("collect_concat_spec.rb");
    }

    @Test
    public void collect_spec() throws Exception {
        mspec("collect_spec.rb");
    }

    @Test
    public void count_spec() throws Exception {
        mspec("count_spec.rb");
    }

    @Test
    public void cycle_spec() throws Exception {
        mspec("cycle_spec.rb");
    }

    @Test
    public void detect_spec() throws Exception {
        mspec("detect_spec.rb");
    }

    @Test
    public void drop_spec() throws Exception {
        mspec("drop_spec.rb");
    }

    @Test
    public void drop_while_spec() throws Exception {
        mspec("drop_while_spec.rb");
    }

    @Test
    public void each_cons_spec() throws Exception {
        mspec("each_cons_spec.rb");
    }

    @Test
    @Ignore("From newer 1.9 than the 1.8.7 backport")
    public void each_entry_spec() throws Exception {
        mspec("each_entry_spec.rb");
    }

    @Test
    public void each_slice_spec() throws Exception {
        mspec("each_slice_spec.rb");
    }

    @Test
    public void each_with_index_spec() throws Exception {
        mspec("each_with_index_spec.rb");
    }

    @Test
    public void each_with_object_spec() throws Exception {
        mspec("each_with_object_spec.rb");
    }

    @Test
    public void entries_spec() throws Exception {
        mspec("entries_spec.rb");
    }

    @Test
    public void find_all_spec() throws Exception {
        mspec("find_all_spec.rb");
    }

    @Test
    public void find_index_spec() throws Exception {
        mspec("find_index_spec.rb");
    }

    @Test
    public void find_spec() throws Exception {
        mspec("find_spec.rb");
    }

    @Test
    public void first_spec() throws Exception {
        mspec("first_spec.rb");
    }

    @Test
    @Ignore("From newer 1.9 than the 1.8.7 backport")
    public void flat_map_spec() throws Exception {
        mspec("flat_map_spec.rb");
    }

    @Test
    @Ignore("Does rely on === while Enumerable.java uses java.util.regex.Pattern")
    public void grep_spec() throws Exception {
        mspec("grep_spec.rb");
    }

    @Test
    public void group_by_spec() throws Exception {
        mspec("group_by_spec.rb");
    }

    @Test
    public void include_spec() throws Exception {
        mspec("include_spec.rb");
    }

    @Test
    public void inject_spec() throws Exception {
        mspec("inject_spec.rb");
    }

    @Test
    public void map_spec() throws Exception {
        mspec("map_spec.rb");
    }

    @Test
    public void max_by_spec() throws Exception {
        mspec("max_by_spec.rb");
    }

    @Test
    public void max_spec() throws Exception {
        mspec("max_spec.rb");
    }

    @Test
    public void member_spec() throws Exception {
        mspec("member_spec.rb");
    }

    @Test
    public void min_by_spec() throws Exception {
        mspec("min_by_spec.rb");
    }

    @Test
    public void min_spec() throws Exception {
        mspec("min_spec.rb");
    }

    @Test
    public void minmax_by_spec() throws Exception {
        mspec("minmax_by_spec.rb");
    }

    @Test
    public void minmax_spec() throws Exception {
        mspec("minmax_spec.rb");
    }

    @Test
    public void none_spec() throws Exception {
        mspec("none_spec.rb");
    }

    @Test
    public void one_spec() throws Exception {
        mspec("one_spec.rb");
    }

    @Test
    public void partition_spec() throws Exception {
        mspec("partition_spec.rb");
    }

    @Test
    public void reduce_spec() throws Exception {
        mspec("reduce_spec.rb");
    }

    @Test
    public void reject_spec() throws Exception {
        mspec("reject_spec.rb");
    }

    @Test
    public void reverse_each_spec() throws Exception {
        mspec("reverse_each_spec.rb");
    }

    @Test
    public void select_spec() throws Exception {
        mspec("select_spec.rb");
    }

    @Test
    @Ignore("From newer 1.9 than the 1.8.7 backport")
    public void slice_before_spec() throws Exception {
        mspec("slice_before_spec.rb");
    }

    @Test
    public void sort_by_spec() throws Exception {
        mspec("sort_by_spec.rb");
    }

    @Test
    public void sort_spec() throws Exception {
        mspec("sort_spec.rb");
    }

    @Test
    public void take_spec() throws Exception {
        mspec("take_spec.rb");
    }

    @Test
    public void take_while_spec() throws Exception {
        mspec("take_while_spec.rb");
    }

    @Test
    public void to_a_spec() throws Exception {
        mspec("to_a_spec.rb");
    }

    @Test
    public void zip_spec() throws Exception {
        mspec("zip_spec.rb");
    }

    public String enumerableJava() {
        return "enumerable_java_rubyspec";
    }

    void mspec(String file) throws Exception {
        StringWriter stdout = new StringWriter();
        Writer originalOut = rb.getContext().getWriter();
        Writer originalErr = rb.getContext().getErrorWriter();
        if (!specdoc)
            rb.getContext().setWriter(stdout);
        StringWriter stderr = new StringWriter();
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
            eval("MSpec.register_files ['core/enumerable/" + file + "']");
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
            throw uncheck(e);
        } finally {
            rb.getContext().setWriter(originalOut);
            rb.getContext().setErrorWriter(originalErr);
            eval("MSpec.unregister :exception, formatter; MSpec.unregister :before, formatter; MSpec.unregister :after, formatter; MSpec.unregister :finish, formatter; MSpec.unregister :enter, formatter; "
                    + "MSpec.register_exit(nil); MSpec.clear_current; MSpec.clear_modes; MSpec.clear_expectations");
        }
    }
}
