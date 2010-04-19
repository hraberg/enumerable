package lambda.weaving.tree;

import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static org.junit.Assert.*;
import static org.objectweb.asm.Type.*;

import java.io.IOException;
import java.util.Comparator;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.TestBase;
import lambda.annotation.LambdaParameter;
import lambda.primitives.Fn1DtoI;
import lambda.primitives.Fn2DDtoD;
import lambda.primitives.Fn2LLtoL;
import lambda.primitives.LambdaPrimitives;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer.LambdaAnalyzer;

import org.junit.After;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@SuppressWarnings("unused")
public class LambdaTreeWeaverTest extends TestBase implements Opcodes {
    @Test
    public void analyzingZeroArgumentLambda() throws Exception {
        class C {
            void m() {
                λ();
            }
        }

        LambdaTreeWeaver weaver = analyze(C.class);
        assertEquals(getType(C.class).getInternalName(), weaver.c.name);
        assertEquals(2, weaver.methods.size());

        MethodAnalyzer constructor = weaver.methods.get(0);
        assertEquals("<init>", constructor.m.name);
        assertTrue(constructor.lambdas.isEmpty());

        MethodAnalyzer method = weaver.methods.get(1);
        assertEquals("m", method.m.name);

        assertEquals(1, method.lambdas.size());
        LambdaAnalyzer lambda = method.lambdas.get(0);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertTrue(lambda.parameters.isEmpty());
        assertTrue(lambda.newLambdaParameterTypes.isEmpty());

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn0.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(0, lambda.sam.getArgumentTypes().length);

        assertEquals(object, lambda.sam.getReturnType());
        assertFalse(lambda.returnNeedsUnboxing());
        assertFalse(lambda.returnNeedsBoxing());
    }

    @Test
    public void analyzingOneArgumentLambda() throws Exception {
        class C {
            void m() {
                λ(n, null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertEquals(list("n"), list(lambda.parameters.keySet()));

        assertEquals(list(INT_TYPE), lambda.getParameterTypes());
        assertEquals(list(object), lambda.newLambdaParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn1.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object), list(lambda.sam.getArgumentTypes()));
        assertEquals(object, lambda.sam.getReturnType());

        assertTrue(lambda.parameterHasConversionAtDefinition("n"));
        assertTrue(lambda.parameterNeedsUnboxing("n"));
    }

    @Test
    public void analyzingTwoArgumentsLambda() throws Exception {
        class C {
            void m() {
                λ(s, n, null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertEquals(list("s", "n"), list(lambda.parameters.keySet()));
        assertEquals(list(getType(String.class), INT_TYPE), lambda.getParameterTypes());

        assertEquals(list(object, object), lambda.newLambdaParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn2.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object, object), list(lambda.sam.getArgumentTypes()));
        assertEquals(object, lambda.sam.getReturnType());

        assertFalse(lambda.parameterHasConversionAtDefinition("s"));
        assertFalse(lambda.parameterNeedsUnboxing("s"));
        assertTrue(lambda.parameterHasConversionAtDefinition("n"));
        assertTrue(lambda.parameterNeedsUnboxing("n"));
    }

    @Test
    public void analyzingThreeArgumentLambda() throws Exception {
        class C {
            void m() {
                λ(s, n, b, null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertEquals(list("s", "n", "b"), list(lambda.parameters.keySet()));
        assertEquals(list(getType(String.class), INT_TYPE, BOOLEAN_TYPE), lambda.getParameterTypes());

        assertEquals(list(object, object, object), lambda.newLambdaParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn3.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object, object, object), list(lambda.sam.getArgumentTypes()));
        assertEquals(object, lambda.sam.getReturnType());

        assertFalse(lambda.parameterNeedsUnboxing("s"));
        assertFalse(lambda.parameterHasConversionAtDefinition("s"));
        assertTrue(lambda.parameterNeedsUnboxing("n"));
        assertTrue(lambda.parameterHasConversionAtDefinition("n"));
        assertTrue(lambda.parameterNeedsUnboxing("b"));
        assertTrue(lambda.parameterHasConversionAtDefinition("b"));
    }

    @Test
    public void analyzingLambdaClosingOverEffectivelyFinalVariable() throws Exception {
        class C {
            void m() {
                int i = 1;
                λ(i);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertEquals(list("i"), list(lambda.locals.keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverMutableVariableMutatedOutsideLambda() throws Exception {
        class C {
            void m() {
                int i = 1;
                i = 2;
                λ(i);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
    }

    @Test
    public void analyzingtLambdaClosingOverMutableVariable() throws Exception {
        class C {
            void m() {
                int i = 1;
                λ(i = 2);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverMutableVariableChangedAfterLambda() throws Exception {
        class C {
            void m() {
                int i = 1;
                λ(i);
                i = 2;
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverIncreasedVariable() throws Exception {
        class C {
            void m() {
                int i = 1;
                λ(i++);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverIncreasedVariableOutsideLambda() throws Exception {
        class C {
            void m() {
                int i = 1;
                λ(i++);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverVariableInitialziedInLambda() throws Exception {
        class C {
            void m() {
                int i;
                λ(i = 2);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverEffectivelyFinalArgument() throws Exception {
        class C {
            void m(int i) {
                λ(i);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertEquals(list("i"), list(lambda.locals.keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaClosingOverMutableArgument() throws Exception {
        class C {
            void m(int i) {
                i = 2;
                i = 4;
                λ(i);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.getMutableLocals().size());

        assertEquals(list("i"), list(lambda.getMutableLocals().keySet()));
        assertEquals(INT_TYPE, lambda.getLocalVariableType("i"));
    }

    @Test
    public void analyzingLambdaCreatedFromGenericCast() throws Exception {
        class C {
            void m() {
                Runnable r = delegate(null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertTrue(lambda.parameters.isEmpty());
        assertTrue(lambda.newLambdaParameterTypes.isEmpty());

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Runnable.class), lambda.lambdaType);

        assertEquals("run", lambda.sam.getName());
        assertEquals(0, lambda.sam.getArgumentTypes().length);

        assertEquals(VOID_TYPE, lambda.sam.getReturnType());
        assertFalse(lambda.returnNeedsBoxing());
    }

    @LambdaParameter
    static Object o1;

    @LambdaParameter
    static Object o2;

    @Test
    public void analyzingLambdaCreatedFromGenericCastWhichAlsoDefinesMethodFromObject() throws Exception {
        class C {
            void m() {
                Comparator<?> c = delegate(o1, o2, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(list("o1", "o2"), list(lambda.parameters.keySet()));
        assertEquals(list(object, object), lambda.newLambdaParameterTypes);
        assertEquals(object, lambda.expressionType);

        assertEquals(getType(Comparator.class), lambda.lambdaType);

        assertEquals("compare", lambda.sam.getName());
        assertEquals(list(object, object), list(lambda.sam.getArgumentTypes()));

        assertEquals(INT_TYPE, lambda.sam.getReturnType());
        assertTrue(lambda.returnNeedsUnboxing());
        assertFalse(lambda.returnNeedsBoxing());
    }

    interface I {
        long invoke(int x, Object y, double z);
    }

    @LambdaParameter
    static Double dbl;

    @Test
    public void analyzingLambdaCreatedFromGenericCastWithTypesThatNeedConversion() throws Exception {
        class C {
            void m() {
                I i = delegate(n, o1, dbl, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(getType(I.class), lambda.lambdaType);
        assertEquals("invoke", lambda.sam.getName());

        assertEquals(list("n", "o1", "dbl"), list(lambda.parameters.keySet()));

        assertEquals(list(INT_TYPE, object, DOUBLE_TYPE), list(lambda.sam.getArgumentTypes()));
        assertEquals(list(INT_TYPE, object, getType(Double.class)), lambda.getParameterTypes());

        assertFalse(lambda.parameterNeedsUnboxing("n"));
        assertFalse(lambda.parameterNeedsBoxing("n"));

        assertFalse(lambda.parameterNeedsUnboxing("o1"));
        assertFalse(lambda.parameterNeedsBoxing("o1"));

        assertFalse(lambda.parameterNeedsUnboxing("dbl"));
        assertTrue(lambda.parameterNeedsBoxing("dbl"));

        assertEquals(object, lambda.expressionType);
        assertEquals(LONG_TYPE, lambda.sam.getReturnType());

        assertTrue(lambda.returnNeedsUnboxing());
        assertFalse(lambda.returnNeedsBoxing());
    }

    @Test
    public void analyzingLambdaCreatedFromPrimitiveLambdaThatNeedsNarrowConversionFromDoubleToInt()
            throws Exception {
        class C {
            void m() {
                LambdaPrimitives.λ(d, idx, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(getType(Fn2DDtoD.class), lambda.lambdaType);
        assertEquals(list(DOUBLE_TYPE, DOUBLE_TYPE), list(lambda.sam.getArgumentTypes()));
        assertEquals(list(DOUBLE_TYPE, INT_TYPE), lambda.getParameterTypes());

        assertFalse(lambda.parameterNeedsUnboxing("d"));
        assertFalse(lambda.parameterNeedsBoxing("d"));
        assertFalse(lambda.parameterNeedsNarrowConversionFromActualArgument("d"));
        assertEquals(-1, lambda.parameterNarrowConversionOpcode("d"));

        assertFalse(lambda.parameterNeedsUnboxing("idx"));
        assertFalse(lambda.parameterNeedsBoxing("idx"));
        assertFalse(lambda.parameterHasConversionAtDefinition("d"));
        assertTrue(lambda.parameterNeedsNarrowConversionFromActualArgument("idx"));
        assertTrue(lambda.parameterHasConversionAtDefinition("idx"));
        assertEquals(D2I, lambda.parameterNarrowConversionOpcode("idx"));

        assertEquals(DOUBLE_TYPE, lambda.expressionType);
        assertEquals(DOUBLE_TYPE, lambda.sam.getReturnType());
    }

    @Test
    public void analyzingLambdaCreatedFromPrimitiveLambdaThatNeedsNarrowConversionFromDoubleToLong()
            throws Exception {
        class C {
            void m() {
                LambdaPrimitives.λ(d, l, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(getType(Fn2DDtoD.class), lambda.lambdaType);
        assertEquals(list(DOUBLE_TYPE, DOUBLE_TYPE), list(lambda.sam.getArgumentTypes()));
        assertEquals(list(DOUBLE_TYPE, LONG_TYPE), lambda.getParameterTypes());

        assertFalse(lambda.parameterNeedsUnboxing("l"));
        assertFalse(lambda.parameterNeedsBoxing("l"));
        assertTrue(lambda.parameterNeedsNarrowConversionFromActualArgument("l"));
        assertEquals(D2L, lambda.parameterNarrowConversionOpcode("l"));
    }

    @LambdaParameter
    static float fl;

    @Test
    public void analyzingLambdaCreatedFromPrimitiveLambdaThatNeedsNarrowConversionFromDoubleToFloat()
            throws Exception {
        class C {
            void m() {
                LambdaPrimitives.λ(d, fl, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(getType(Fn2DDtoD.class), lambda.lambdaType);
        assertEquals(list(DOUBLE_TYPE, DOUBLE_TYPE), list(lambda.sam.getArgumentTypes()));
        assertEquals(list(DOUBLE_TYPE, FLOAT_TYPE), lambda.getParameterTypes());

        assertFalse(lambda.parameterNeedsUnboxing("fl"));
        assertFalse(lambda.parameterNeedsBoxing("fl"));
        assertTrue(lambda.parameterNeedsNarrowConversionFromActualArgument("fl"));
        assertEquals(D2F, lambda.parameterNarrowConversionOpcode("fl"));
    }

    @Test
    public void analyzingLambdaCreatedFromPrimitiveLambdaThatNeedsNarrowConversionFromLongToInt() throws Exception {
        class C {
            void m() {
                LambdaPrimitives.λ(l, idx, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(getType(Fn2LLtoL.class), lambda.lambdaType);

        assertEquals(list(LONG_TYPE, LONG_TYPE), list(lambda.sam.getArgumentTypes()));
        assertEquals(list(LONG_TYPE, INT_TYPE), lambda.getParameterTypes());

        assertFalse(lambda.parameterNeedsUnboxing("l"));
        assertFalse(lambda.parameterNeedsBoxing("l"));
        assertFalse(lambda.parameterNeedsNarrowConversionFromActualArgument("l"));
        assertEquals(-1, lambda.parameterNarrowConversionOpcode("l"));

        assertFalse(lambda.parameterNeedsUnboxing("idx"));
        assertFalse(lambda.parameterNeedsBoxing("idx"));
        assertTrue(lambda.parameterNeedsNarrowConversionFromActualArgument("idx"));
        assertEquals(L2I, lambda.parameterNarrowConversionOpcode("idx"));

        assertEquals(LONG_TYPE, lambda.expressionType);
        assertEquals(LONG_TYPE, lambda.sam.getReturnType());
    }

    @Test
    public void analyzingOneArgumentLambdaWithDefaultValue() throws Exception {
        class C {
            void m() {
                λ(n = 2, null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.getMutableLocals().isEmpty());

        assertEquals(list("n"), list(lambda.parameters.keySet()));
        assertEquals(list(INT_TYPE), lambda.getParameterTypes());

        assertTrue(lambda.parametersWithDefaultValue.containsKey("n"));
        assertTrue(lambda.parameterDefaultValueNeedsBoxing("n"));
    }

    @Test
    public void analyzingTwoArgumentLambdaWithDefaultValue() throws Exception {
        class C {
            void m() {
                λ(n, s = "", null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(list("n", "s"), list(lambda.parameters.keySet()));
        assertEquals(list(INT_TYPE, getType(String.class)), lambda.getParameterTypes());

        assertFalse(lambda.parametersWithDefaultValue.containsKey("n"));
        assertTrue(lambda.parametersWithDefaultValue.containsKey("s"));

        assertTrue(lambda.parameterDefaultValueNeedsBoxing("n"));
        assertFalse(lambda.parameterDefaultValueNeedsBoxing("s"));
    }

    @Test
    public void analyzingLambdaCreatedFromPrimitiveLambdaThatHasDefaultValue() throws Exception {
        class C {
            void m() {
                LambdaPrimitives.λ(d = 2, 0);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(getType(Fn1DtoI.class), lambda.lambdaType);
        assertEquals(list(DOUBLE_TYPE), list(lambda.sam.getArgumentTypes()));
        assertEquals(list(DOUBLE_TYPE), lambda.getParameterTypes());

        assertTrue(lambda.parametersWithDefaultValue.containsKey("d"));
        assertTrue(lambda.parameterDefaultValueNeedsBoxing("d"));
    }

    @Test
    public void analyzingLambdaAccessingPrivateField() throws Exception {
        class C {
            private int p;

            void m() {
                λ(p);
            }
        }

        analyze(C.class);
        assertTrue(weaver.fieldsThatNeedStaticAccessMethod.containsKey("p"));
    }

    @Test
    public void analyzingLambdaAccessingPrivateMethod() throws Exception {
        class C {
            void m() {
                λ(p());
            }

            private Object p() {
                return null;
            }
        }

        analyze(C.class);
        assertTrue(weaver.methodsThatNeedStaticAccessMethod.containsKey("p"));
    }

    static class PrivateStaticField {
        private static int p;

        void m() {
            λ(p);
        }
    }

    @Test
    public void analyzingLambdaAccessingPrivateStaticField() throws Exception {
        analyze(PrivateStaticField.class);
        assertTrue(weaver.fieldsThatNeedStaticAccessMethod.containsKey("p"));
    }

    static class PrivateStaticMethod {
        void m() {
            λ(p());
        }

        private static Object p() {
            return null;
        }
    }

    @Test
    public void analyzingLambdaAccessingPrivateStaticMethod() throws Exception {
        analyze(PrivateStaticMethod.class);
        assertTrue(weaver.methodsThatNeedStaticAccessMethod.containsKey("p"));
    }

    LambdaAnalyzer lambdaIn(Class<?> aClass) throws Exception {
        return methodIn(aClass).lambdas.get(0);
    }

    MethodAnalyzer methodIn(Class<?> aClass) throws IOException, Exception {
        return analyze(aClass).methods.get(1);
    }

    @After
    public void transform() throws Exception {
        weaver.transform();
    }

    LambdaTreeWeaver weaver;
    Type object = getType(Object.class);

    LambdaTreeWeaver analyze(Class<?> aClass) throws IOException, Exception {
        ClassReader cr = new ClassReader(aClass.getName());
        weaver = new LambdaTreeWeaver(cr);
        weaver.analyze();
        return weaver;
    }
}
