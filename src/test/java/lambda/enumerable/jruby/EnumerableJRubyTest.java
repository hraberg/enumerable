package lambda.enumerable.jruby;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnumerableJRubyTest extends JRubyTestBase {
    @Test
    public void sanityCheckMonkeyPatch() throws Exception {
        assertEquals(eval("[1, 2, 3]"),
                eval("[1, 2 ,3].each {|n| Java::LambdaEnumerableJRuby::JRubyTestBase.debug n.to_s}"));
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
}
