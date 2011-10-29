package org.enumerable.lambda.enumerable.jruby;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("This test is mainly for development, CoreEnumerableRubySpecSuiteTest is the real thing")
public class EnumerableRubySpecTest extends RubySpecTestBase {
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
}
