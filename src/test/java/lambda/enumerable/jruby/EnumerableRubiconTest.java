package lambda.enumerable.jruby;

import org.junit.Ignore;
import org.junit.Test;

public class EnumerableRubiconTest extends JRubyTestBase {
    @Test
    @Ignore("Does pass, but not in the suite, needs something done to how JRuby is shared etc")
    public void rubicon_test_enumerable() throws Exception {
        testUnit("/test/rubicon/test_enumerable.rb", "TestEnumerable");
    }
}
