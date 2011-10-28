package lambda.support.expression;

import static lambda.exception.UncheckedException.*;
import static org.objectweb.asm.Type.*;
import japa.parser.ast.expr.Expression;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.LambdaLocal;
import lambda.weaving.InMemoryCompiler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.ASMifierMethodVisitor;

public class LambdaExpressionTrees {
    static int expressionId = 1;
    static InMemoryCompiler compiler = new InMemoryCompiler();

    public static Expression parseExpression(String expression) {
        try {
            Class<?> parserClass = Class.forName("japa.parser.ASTParser");
            Constructor<?> ctor = parserClass.getConstructor(Reader.class);
            ctor.setAccessible(true);
            Object parser = ctor.newInstance(new StringReader(expression));
            Method method = parserClass.getMethod("Expression");
            method.setAccessible(true);
            return (Expression) method.invoke(parser);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    static Expression parseExpressionFromSingleMethodClass(Class<?> aClass, String... parameters) {
        return parseExpressionFromMethod(Fn0.getLambdaMethod(aClass), parameters);
    }
 
    public static Expression parseExpressionFromMethod(Method method, String... parameters) {
        try {
            MethodNode mn = findMethodNode(method);

            LocalVariableNode[] parameterLocals = new LocalVariableNode[parameters.length];
            Type[] argumentTypes = getArgumentTypes(mn.desc);
            int realIndex = 1;
            for (int i = 0; i < parameters.length; i++) {
                parameterLocals[i] = new LocalVariableNode(parameters[i], argumentTypes[i].getDescriptor(), null,
                        null, null, realIndex);
                realIndex += argumentTypes[i].getSize();
            }

            final ExpressionInterpreter interpreter = new ExpressionInterpreter(mn, parameterLocals);
            Analyzer analyzer = new Analyzer(interpreter) {
                protected Frame newFrame(Frame src) {
                    Frame frame = super.newFrame(src);
                    interpreter.setCurrentFrame(frame);
                    return frame;
                }

                protected void newControlFlowEdge(int insn, int successor) {
                    interpreter.newControlFlowEdge(insn, successor);
                }
            };
            interpreter.analyzer = analyzer;
            analyzer.analyze(getInternalName(method.getDeclaringClass()), mn);
            return interpreter.expression;
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    static void printASMifiedMethod(Method method) {
        try {
            MethodNode mn = findMethodNode(method);
            ASMifierMethodVisitor asm = new ASMifierMethodVisitor();
            mn.accept(asm);
            PrintWriter pw = new PrintWriter(System.out);
            asm.print(pw);
            pw.flush();
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    @SuppressWarnings("unchecked")
    static MethodNode findMethodNode(Method method) throws IOException {
        String className = method.getDeclaringClass().getName();
        ClassReader cr;
        if (InMemoryCompiler.bytesByClassName.containsKey(className))
            cr = new ClassReader(InMemoryCompiler.bytesByClassName.get(className));

        else
            cr = new ClassReader(className);

        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        String descriptor = getMethodDescriptor(method);
        for (MethodNode mn : (List<MethodNode>) cn.methods) {
            if (method.getName().equals(mn.name) && descriptor.equals(mn.desc))
                return mn;
        }
        throw new IllegalStateException("Cannot find method which does exist");
    }

    @SuppressWarnings("unchecked")
    public static <R extends Expression> R toExpression(Fn0<?> fn) {
        if (fn.getClass().getDeclaredFields().length > 0)
            throw new IllegalArgumentException("Turning Closures into Expressions isn't supported");

        LambdaLocal[] parameters = fn.getParameters();
        String[] parameterNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++)
            parameterNames[i] = parameters[i].name();

        return (R) parseExpressionFromSingleMethodClass(fn.getClass(), parameterNames);
    }

    public static <R> Fn0<R> toFn0(Class<R> returnType, Expression expression) {
        try {
            String className = "ExpressionFn0_" + expressionId++;

            String source = "class " + className + " extends " + Fn0.class.getName() + "{ public "
                    + typeToString(returnType) + " call() { return " + expression + "; }}";

            return compileAndCreate(className, source);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static <A1, R> Fn1<A1, R> toFn1(Class<R> returnType, Class<A1> a1Type, String a1Name,
            Expression expression) {
        try {
            String className = "ExpressionFn1_" + expressionId++;

            String source = "class " + className + " extends " + Fn1.class.getName() + "{ public "
                    + typeToString(returnType) + " call(" + typeToString(a1Type) + " " + a1Name + ") { return "
                    + expression + "; } public " + typeToString(returnType) + " call(Object " + a1Name
                    + ") { return call((" + typeToString(a1Type) + ") " + a1Name + ");  }  }";

            return compileAndCreate(className, source);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static <A1, A2, R> Fn2<A1, A2, R> toFn2(Class<R> returnType, Class<A1> a1Type, String a1Name,
            Class<A2> a2Type, String a2Name, Expression expression) {
        try {
            String className = "ExpressionFn2_" + expressionId++;

            String source = "class " + className + " extends " + Fn2.class.getName() + "{ public "
                    + typeToString(returnType) + " call(" + typeToString(a1Type) + " " + a1Name + ", "
                    + typeToString(a2Type) + " " + a2Name + ") { return " + expression + "; } public "
                    + typeToString(returnType) + " call(Object " + a1Name + ", Object " + a2Name
                    + ") { return call((" + typeToString(a1Type) + ") " + a1Name + ", (" + typeToString(a2Type)
                    + ") " + a2Name + ");  }  }";

            return compileAndCreate(className, source);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static <A1, A2, A3, R> Fn3<A1, A2, A3, R> toFn3(Class<R> returnType, Class<A1> a1Type, String a1Name,
            Class<A2> a2Type, String a2Name, Class<A2> a3Type, String a3Name, Expression expression) {
        try {
            String className = "ExpressionFn3_" + expressionId++;

            String source = "class " + className + " extends " + Fn3.class.getName() + "{ public "
                    + typeToString(returnType) + " call(" + typeToString(a1Type) + " " + a1Name + ", "
                    + typeToString(a2Type) + " " + a2Name + ", " + typeToString(a3Type) + " " + a3Name
                    + ") { return " + expression + "; } public " + typeToString(returnType) + " call(Object "
                    + a1Name + ", Object " + a2Name + ", Object " + a3Name + ") { return call(("
                    + typeToString(a1Type) + ") " + a1Name + ", (" + typeToString(a2Type) + ") " + a2Name + ", ("
                    + typeToString(a3Type) + ") " + a3Name + ");  }  }";

            return compileAndCreate(className, source);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    static String typeToString(Class<?> returnType) {
        return returnType.isArray() ? returnType.getComponentType().getName() + "[]" : returnType.getName();
    }

    @SuppressWarnings("unchecked")
    static <R extends Fn0<?>> R compileAndCreate(String className, String source) throws IOException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> aClass = compiler.compile(className, source);
        Constructor<?> ctor = aClass.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        return (R) ctor.newInstance();
    }
}
