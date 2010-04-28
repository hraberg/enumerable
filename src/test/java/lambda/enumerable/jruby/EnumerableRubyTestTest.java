package lambda.enumerable.jruby;

import org.junit.Ignore;
import org.junit.Test;

public class EnumerableRubyTestTest extends JRubyTestBase {
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
}
