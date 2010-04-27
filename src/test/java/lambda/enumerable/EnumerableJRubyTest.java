package lambda.enumerable;

import static java.lang.System.*;
import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import lambda.jruby.JRubyTest;
import lambda.weaving.Debug;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class EnumerableJRubyTest {
    static boolean debug = Debug.debug;
    static ScriptEngine rb = JRubyTest.getJRubyEngine();

    @Test
    public void sanityCheckMonkeyPatch() throws Exception {
        assertEquals(eval("[1, 2, 3]"),
                eval("[1, 2 ,3].each {|n| Java::LambdaEnumerable::EnumerableJRubyTest.debug n.to_s}"));
        assertEquals("a", eval("[\"b\", \"a\", \"c\"].min"));
        assertTrue((Boolean) eval("%w{ ant bear cat}.all? {|word| word.length >= 3}"));
        assertEquals("11", eval("[\"2\",\"33\",\"4\",\"11\"].min {|a,b| a <=> b }"));
        eval("test1 = [1,3,5,7,0, 2,43,53,6352,44,221,5]");
        assertEquals(eval("test1"), eval("test1.to_a"));
        assertEquals(eval("test1"), eval("test1.entries"));
        eval("test4 = (1..10)");
        assertEquals(eval("[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]"), eval("test4.sort { |a,b| b<=>a }"));
        assertEquals(37L, eval("(1..10).detect(lambda { 37 }) {|i| i % 5 == 0 and i % 7 == 0 }"));
        assertEquals(eval("[[2, 4, 6], [1, 3, 5]]"), eval("(1..6).partition {|i| (i&1).zero?}"));
        assertTrue((Boolean) eval("(1..10).include?(5)"));
        assertEquals(eval("[[1], [2], [3]]"), eval("(1..3).zip"));
        assertEquals(eval("[Array]"), eval("[['foo']].map {|a|a.class}"));
    }

    @Test
    public void testEnumerable() throws Exception {
        load("/test/testEnumerable.rb");
    }

    @Test
    public void testEnumerable_1_9() throws Exception {
        load("/test/testEnumerable_1_9.rb");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void rubicon_test_enumerable() throws Exception {
        testUnit("/test/rubicon/test_enumerable.rb", "TestEnumerable");
    }

    @Test
    public void tc_all() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_all.rb",
                "TC_Enumerable_All_InstanceMethod");
    }

    @Test
    public void tc_any() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_any.rb",
                "TC_Enumerable_Any_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_collect() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_collect.rb",
                "TC_Enumerable_Collect_Instance");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_detect() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_detect.rb",
                "TC_Enumerable_Detect_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_each_with_index() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_each_with_index.rb",
                "TC_Enumerable_EachWithIndex_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_find_all() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_find_all.rb",
                "TC_Enumerable_FindAll_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_grep() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_grep.rb",
                "TC_Enumerable_Grep_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_include() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_include.rb",
                "TC_Enumerable_Include_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_inject() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_inject.rb",
                "TC_Enumerable_Inject_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_max() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_max.rb",
                "TC_Enumerable_Max_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_min() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_min.rb",
                "TC_Enumerable_Min_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_partition() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_partition.rb",
                "TC_Enumerable_Partition_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_reject() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_reject.rb",
                "TC_Enumerable_Reject_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_sort_by() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_sort_by.rb",
                "TC_Enumerable_SortBy_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_to_a() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_to_a.rb",
                "TC_Enumerable_ToA_InstanceMethod");
    }

    @Test
    @Ignore("Monkey patch not ready yet")
    public void tc_zip() throws Exception {
        testUnit("/test/externals/ruby_test/test/core/Enumerable/instance/tc_zip.rb",
                "TC_Enumerable_Zip_InstanceMethod");
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

    @BeforeClass
    public static void monkeyPatchJRubyEnumerableToUseEnumerableJava() throws ScriptException {
        load("/enumerable_java.rb");
    }

    static void load(String test) throws ScriptException {
        rb.eval(new InputStreamReader(EnumerableJRubyTest.class.getResourceAsStream(test)));
    }

    static Object eval(String script) throws ScriptException {
        return rb.eval(script);
    }

    @After
    public void printErrors() throws ScriptException {
        if ((Boolean) eval("!$failed.nil? && !$failed.empty?"))
            eval("test_print_report; raise $failed.first.to_s");
    }

    public static void debug(String msg) {
        if (debug)
            out.println(msg);
    }
}
