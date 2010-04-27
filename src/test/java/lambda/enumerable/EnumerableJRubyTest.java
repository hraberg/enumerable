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
        // assertEquals(rb.eval("[1, 2, 3]"),
        // rb.eval("[1, 2 ,3].each {|n| puts n}"));
        assertEquals("a", rb.eval("[\"b\", \"a\", \"c\"].min"));
        assertTrue((Boolean) rb.eval("%w{ ant bear cat}.all? {|word| word.length >= 3}"));
        assertEquals("11", rb.eval("[\"2\",\"33\",\"4\",\"11\"].min {|a,b| a <=> b }"));
        rb.eval("test1 = [1,3,5,7,0, 2,43,53,6352,44,221,5]");
        assertEquals(rb.eval("test1"), rb.eval("test1.to_a"));
        assertEquals(rb.eval("test1"), rb.eval("test1.entries"));
        rb.eval("test4 = (1..10)");
        // assertEquals(rb.eval("[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]"),
        // rb.eval("test4.sort { |a,b| b<=>a }"));
        rb.eval("test6 = 1..100");
        // assertEquals(rb.eval("[38, 39, 40, 41, 42, 43, 44]"),
        // rb.eval("test6.grep(38..44)"));
        assertEquals(37L, rb.eval("(1..10).detect(lambda { 37 }) {|i| i % 5 == 0 and i % 7 == 0 }"));
        assertEquals(rb.eval("[[2, 4, 6], [1, 3, 5]]"), rb.eval("(1..6).partition {|i| (i&1).zero?}"));
        assertTrue((Boolean) rb.eval("(1..10).include?(5)"));
        // assertEquals(rb.eval("[[\"cat\n\", 1], [\"dog\", nil]]"),
        // rb.eval("\"cat\ndog\".zip([1])"));
        // assertEquals(rb.eval("[[1], [2], [3]]"), rb.eval("(1..3).zip"));
        // assertEquals(rb.eval("[Array]"),
        // rb.eval("[['foo']].map {|a|a.class}"));
    }

    @Test
    public void testEnumerableRb() throws Exception {
        miniunit("/testEnumerable.rb");
    }

    @Test
    public void testEnumerable_1_9Rb() throws Exception {
        miniunit("/testEnumerable_1_9.rb");
    }

    void miniunit(String test) throws ScriptException {
        rb.eval(new InputStreamReader(getClass().getResourceAsStream(test)));
    }

    @BeforeClass
    public static void enumerableJava() throws ScriptException {
        rb.eval(new InputStreamReader(EnumerableJRubyTest.class.getResourceAsStream("/enumerableJava.rb")));
    }

    @After
    public void printErrors() throws ScriptException {
        if ((Boolean) rb.eval("!$failed.nil? && !$failed.empty?"))
            rb.eval("test_print_report; raise $failed.first.to_s");
    }

    public static void debug(String message) {
        if (debug)
            out.println(message);
    }
}
