package lambda.weaving.tree;

import static lambda.Lambda.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class LambdaTreeWeaverTest {
    public static class TreeWeaverTest {
        public void simpleLambda() throws Exception {
            λ("hello");
        }
    }

    @Test
    public void smoke() throws Exception {
        ClassNode cn = transform();
        assertEquals(TreeWeaverTest.class.getName(), cn.name.replace('/', '.'));
    }

    ClassNode transform() throws IOException, Exception {
        ClassReader cr = new ClassReader(TreeWeaverTest.class.getName());
        return new LambdaTreeWeaver().transform(cr);
    }
}
