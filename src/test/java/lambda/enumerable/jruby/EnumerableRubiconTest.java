package lambda.enumerable.jruby;

import org.junit.Test;

public class EnumerableRubiconTest extends JRubyTestBase {
    @Test
    public void test_enumerable() throws Exception {
        testUnit("test/rubicon/test_enumerable", "TestEnumerableRubicon");
    }
}
