package org.enumerable.lambda.weaving;

import org.enumerable.lambda.weaving.ClassFilter;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ClassFilterTest {
    @Test
    public void testExcludedPackages(){
         ClassFilter filter = new ClassFilter("packagetoingnore", "", "");
         assertFalse(filter.isToBeInstrumented("packagetoingnore.MyClass"));
         assertFalse(filter.isToBeInstrumented("java.util.Map"));
         assertTrue(filter.isToBeInstrumented("newpackage.AnotherClass"));
     }

    @Test
    public void testIncludePackages(){
        ClassFilter filter = new ClassFilter("", "mypackage", "");
        assertTrue(filter.isToBeInstrumented("mypackage.AnotherClass"));
        assertFalse(filter.isToBeInstrumented("packagetoingnore.MyClass"));
        assertFalse(filter.isToBeInstrumented("java.util.Map"));
     }

    @Test
    public void testIncludeExcludePattern(){
        ClassFilter filter = new ClassFilter("", "mypackage", "UUAARGH");
        assertTrue(filter.isToBeInstrumented("mypackage.AnotherClass"));
        assertFalse(filter.isToBeInstrumented("mypackage.AnotherUUAARGHClass"));
        assertFalse(filter.isToBeInstrumented("packagetoingnore.MyClass"));
     }
}
