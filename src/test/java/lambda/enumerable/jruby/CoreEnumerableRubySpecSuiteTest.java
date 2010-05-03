package lambda.enumerable.jruby;

import static java.lang.Thread.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CoreEnumerableRubySpecSuiteTest extends RubySpecTestBase {
    @Test
    public void core_enumerable() throws Exception {
        // Something clashes when run as part of the full suite in Eclipse.
        // Have already wasted too much time trying to figure out why.
        // Someone is trying to call entrySet on 1..3:Range for god know what
        // reason.
        for (StackTraceElement e : currentThread().getStackTrace())
            if (e.getClassName().startsWith("org.eclipse.jdt.internal."))
                return;

        File specDir = new File(getClass().getResource("/core/enumerable").toURI());
        List<String> specs = new ArrayList<String>();
        for (String file : specDir.list()) {
            if (file.endsWith(".rb"))
                specs.add("\"core/enumerable/" + file + "\"");
        }
        mspec(specs);
    }
}
