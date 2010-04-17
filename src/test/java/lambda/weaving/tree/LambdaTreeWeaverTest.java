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
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer.LambdaAnalyzer;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

@SuppressWarnings("unused")
public class LambdaTreeWeaverTest extends TestBase {
    @Test
    public void analyzingZeroArgumentLambda() throws Exception {
        class C {
            void m() {
                λ(null);
            }
        }

        LambdaTreeWeaver weaver = transform(C.class);
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
        class C {
            void m() {
                λ(n, null);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertTrue(lambda.locals.isEmpty());
        assertTrue(lambda.mutableLocals.isEmpty());

        assertEquals(list("n"), list(lambda.parameters.keySet()));
        assertEquals(list(INT_TYPE), lambda.getParameterTypes());

        assertEquals(list(object), lambda.methodParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn1.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object), list(lambda.sam.getArgumentTypes()));
        assertEquals(object, lambda.sam.getReturnType());
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
        assertTrue(lambda.mutableLocals.isEmpty());

        assertEquals(list("s", "n"), list(lambda.parameters.keySet()));
        assertEquals(list(getType(String.class), INT_TYPE), lambda.getParameterTypes());

        assertEquals(list(object, object), lambda.methodParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn2.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object, object), list(lambda.sam.getArgumentTypes()));
        assertEquals(object, lambda.sam.getReturnType());
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
        assertTrue(lambda.mutableLocals.isEmpty());

        assertEquals(list("s", "n", "b"), list(lambda.parameters.keySet()));
        assertEquals(list(getType(String.class), INT_TYPE, BOOLEAN_TYPE), lambda.getParameterTypes());

        assertEquals(list(object, object, object), lambda.methodParameterTypes);

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Fn3.class), lambda.lambdaType);

        assertEquals("call", lambda.sam.getName());
        assertEquals(list(object, object, object), list(lambda.sam.getArgumentTypes()));
        assertEquals(object, lambda.sam.getReturnType());
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
        assertTrue(lambda.mutableLocals.isEmpty());

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
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
    }

    @Test
    public void analyzingLambdaClosingOverMutableVariable() throws Exception {
        class C {
            void m() {
                int i = 1;
                λ(i = 2);
            }
        }

        LambdaAnalyzer lambda = lambdaIn(C.class);

        assertEquals(1, lambda.locals.size());
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
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
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
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
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
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
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
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
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
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
        assertTrue(lambda.mutableLocals.isEmpty());

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
        assertEquals(1, lambda.mutableLocals.size());

        assertEquals(list("i"), list(lambda.mutableLocals.keySet()));
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
        assertTrue(lambda.methodParameterTypes.isEmpty());

        assertEquals(object, lambda.expressionType);
        assertEquals(getType(Runnable.class), lambda.lambdaType);

        assertEquals("run", lambda.sam.getName());
        assertEquals(0, lambda.sam.getArgumentTypes().length);
        assertEquals(VOID_TYPE, lambda.sam.getReturnType());
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
        assertEquals(list(object, object), lambda.methodParameterTypes);
        assertEquals(object, lambda.expressionType);

        assertEquals(getType(Comparator.class), lambda.lambdaType);

        assertEquals("compare", lambda.sam.getName());
        assertEquals(list(object, object), list(lambda.sam.getArgumentTypes()));
        assertEquals(INT_TYPE, lambda.sam.getReturnType());
    }

    LambdaAnalyzer lambdaIn(Class<?> aClass) throws Exception {
        MethodAnalyzer ma = transform(aClass).methods.get(1);
        return ma.lambdas.get(0);
    }

    Type object = getType(Object.class);

    LambdaTreeWeaver transform(Class<?> aClass) throws IOException, Exception {
        ClassReader cr = new ClassReader(aClass.getName());
        LambdaTreeWeaver weaver = new LambdaTreeWeaver();
        weaver.transform(cr);
        return weaver;
    }
}
