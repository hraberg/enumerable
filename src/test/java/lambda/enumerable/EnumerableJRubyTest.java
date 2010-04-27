package lambda.enumerable;

import static java.lang.System.*;
import static org.junit.Assert.*;

import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import lambda.jruby.JRubyTest;
import lambda.weaving.Debug;

import org.junit.After;
import org.junit.BeforeClass;
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
        // assertEquals(eval("[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]"),
        // eval("test4.sort { |a,b| b<=>a }"));
        assertEquals(37L, eval("(1..10).detect(lambda { 37 }) {|i| i % 5 == 0 and i % 7 == 0 }"));
        assertEquals(eval("[[2, 4, 6], [1, 3, 5]]"), eval("(1..6).partition {|i| (i&1).zero?}"));
        assertTrue((Boolean) eval("(1..10).include?(5)"));
        assertEquals(eval("[[1], [2], [3]]"), eval("(1..3).zip"));
        assertEquals(eval("[Array]"), eval("[['foo']].map {|a|a.class}"));
    }

    @Test
    public void testEnumerableRb() throws Exception {
        load("/test/testEnumerable.rb");
    }

    @Test
    public void testEnumerable_1_9Rb() throws Exception {
        load("/test/testEnumerable_1_9.rb");
    }

    @BeforeClass
    public static void enumerableJava() throws ScriptException {
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
