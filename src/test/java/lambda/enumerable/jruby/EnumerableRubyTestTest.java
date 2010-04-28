package lambda.enumerable.jruby;

import org.junit.Ignore;
import org.junit.Test;

public class EnumerableRubyTestTest extends JRubyTestBase {
    @Test
    public void tc_all() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_all",
                "TC_Enumerable_All_InstanceMethod");
    }

    @Test
    public void tc_any() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_any",
                "TC_Enumerable_Any_InstanceMethod");
    }

    @Test
    public void tc_collect() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_collect",
                "TC_Enumerable_Collect_Instance");
    }

    @Test
    public void tc_detect() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_detect",
                "TC_Enumerable_Detect_InstanceMethod");
    }

    @Test
    public void tc_each_with_index() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_each_with_index",
                "TC_Enumerable_EachWithIndex_InstanceMethod");
    }

    @Test
    public void tc_find_all() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_find_all",
                "TC_Enumerable_FindAll_InstanceMethod");
    }

    @Test
    @Ignore("Need support for Ruby to Java regexp conversion and maybe ===")
    public void tc_grep() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_grep",
                "TC_Enumerable_Grep_InstanceMethod");
    }

    @Test
    public void tc_include() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_include",
                "TC_Enumerable_Include_InstanceMethod");
    }

    @Test
    public void tc_inject() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_inject",
                "TC_Enumerable_Inject_InstanceMethod");
    }

    @Test
    public void tc_max() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_max",
                "TC_Enumerable_Max_InstanceMethod");
    }

    @Test
    public void tc_min() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_min",
                "TC_Enumerable_Min_InstanceMethod");
    }

    @Test
    public void tc_partition() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_partition",
                "TC_Enumerable_Partition_InstanceMethod");
    }

    @Test
    public void tc_reject() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_reject",
                "TC_Enumerable_Reject_InstanceMethod");
    }

    @Test
    public void tc_sort_by() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_sort_by",
                "TC_Enumerable_SortBy_InstanceMethod");
    }

    @Test
    public void tc_to_a() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_to_a",
                "TC_Enumerable_ToA_InstanceMethod");
    }

    @Test
    public void tc_zip() throws Exception {
        testUnit("test/externals/ruby_test/test/core/Enumerable/instance/tc_zip",
                "TC_Enumerable_Zip_InstanceMethod");
    }
}
