package lambda.weaving.tree;

import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static org.junit.Assert.*;
import static org.objectweb.asm.Type.*;

import java.io.IOException;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.TestBase;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer.LambdaAnalyzer;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

@SuppressWarnings("unused")
public class LambdaTreeWeaverTest extends TestBase {
    @Test
    public void analyzingZeroArgumentLambda() throws Exception {
        class ZeroArgumentLambda {
            void test() {
                λ(null);
            }
        }

        LambdaTreeWeaver weaver = transform(ZeroArgumentLambda.class);
        assertEquals(getType(ZeroArgumentLambda.class).getInternalName(), weaver.c.name);
        assertEquals(2, weaver.methods.size());

        MethodAnalyzer constructor = weaver.methods.get(0);
        assertEquals("<init>", constructor.m.name);
        assertTrue(constructor.lambdas.isEmpty());

        MethodAnalyzer test = weaver.methods.get(1);
        assertEquals("test", test.m.name);

        assertEquals(1, test.lambdas.size());
        LambdaAnalyzer lambda = test.lambdas.get(0);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.mutableLocals.isEmpty());

        assertTrue(lambda.parameters.isEmpty());
        assertTrue(lambda.methodParameterTypes.isEmpty());

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn0.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(0, lambda.sam.getArgumentTypes().length);
        assertEquals(object, lambda.sam.getReturnType());
    }

    @Test
    public void analyzingOneArgumentLambda() throws Exception {
        class OneArgumentLambda {
            void test() {
                λ(n, null);
            }
        }

        LambdaAnalyzer lambda = lambdaFor(OneArgumentLambda.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.mutableLocals.isEmpty());

        assertEquals(list("n"), lambda.getParameterNames());
        assertEquals(list(INT_TYPE), lambda.getParameterTypes());

        assertEquals(list(object), lambda.methodParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn1.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object), lambda.getSamArgumentTypes());
        assertEquals(object, lambda.sam.getReturnType());
    }

    @Test
    public void analyzingTwoArgumentsLambda() throws Exception {
        class TwoArgumentsLambda {
            void test() {
                λ(s, n, null);
            }
        }

        LambdaAnalyzer lambda = lambdaFor(TwoArgumentsLambda.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.mutableLocals.isEmpty());

        assertEquals(list("s", "n"), lambda.getParameterNames());
        assertEquals(list(getType(String.class), INT_TYPE), lambda.getParameterTypes());

        assertEquals(list(object, object), lambda.methodParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn2.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object, object), lambda.getSamArgumentTypes());
        assertEquals(object, lambda.sam.getReturnType());
    }

    @Test
    public void analyzingThreeArgumentLambda() throws Exception {
        class ThreeArgumentsLambda {
            void test() {
                λ(s, n, b, null);
            }
        }

        LambdaAnalyzer lambda = lambdaFor(ThreeArgumentsLambda.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.mutableLocals.isEmpty());

        assertEquals(list("s", "n", "b"), lambda.getParameterNames());
        assertEquals(list(getType(String.class), INT_TYPE, BOOLEAN_TYPE), lambda.getParameterTypes());

        assertEquals(list(object, object, object), lambda.methodParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn3.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object, object, object), lambda.getSamArgumentTypes());
        assertEquals(object, lambda.sam.getReturnType());
    }

    LambdaAnalyzer lambdaFor(Class<?> aClass) throws Exception {
        MethodAnalyzer test = transform(aClass).methods.get(1);
        return test.lambdas.get(0);
    }

    Type object = getType(Object.class);

    LambdaTreeWeaver transform(Class<?> aClass) throws IOException, Exception {
        ClassReader cr = new ClassReader(aClass.getName());
        LambdaTreeWeaver weaver = new LambdaTreeWeaver();
        weaver.transform(cr);
        return weaver;
    }
}
