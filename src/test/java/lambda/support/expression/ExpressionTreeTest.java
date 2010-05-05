package lambda.support.expression;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static lambda.support.expression.ExpressionInterpreter.*;
import static org.junit.Assert.*;
import japa.parser.ast.expr.Expression;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;

public class ExpressionTreeTest {
    static boolean debug = false;

    InMemoryCompiler compiler = new InMemoryCompiler();

    @Test
    public void parseLiteralExpressions() throws Exception {
        assertEquals(parseExpression("0"), parseExpressionViaASM("0", int.class));
        assertEquals(parseExpression("-1"), parseExpressionViaASM("-1", int.class));
        assertEquals(parseExpression("1"), parseExpressionViaASM("1", int.class));
        assertEquals(parseExpression("42"), parseExpressionViaASM("42", int.class));
        assertEquals(parseExpression("" + (1024 * 1024)), parseExpressionViaASM("" + (1024 * 1024), int.class));
        assertEquals(parseExpression("1.0"), parseExpressionViaASM("1.0", double.class));
        assertEquals(parseExpression("1L"), parseExpressionViaASM("1L", long.class));
        assertEquals(parseExpression("42L"), parseExpressionViaASM("42L", long.class));
        assertEquals(parseExpression("1.0f"), parseExpressionViaASM("1.0f", float.class));
        assertEquals(parseExpression("42.0f"), parseExpressionViaASM("42.0f", float.class));
        assertEquals(parseExpression("-2"), parseExpressionViaASM("-2", int.class));
        assertEquals(parseExpression("-1.0"), parseExpressionViaASM("-1.0", double.class));
        assertEquals(parseExpression("-1L"), parseExpressionViaASM("-1L", long.class));
        assertEquals(parseExpression("-1.0f"), parseExpressionViaASM("-1.0f", float.class));
        assertEquals(parseExpression("" + (-1024 * 1024)), parseExpressionViaASM("" + (-1024 * 1024), int.class));
        assertEquals(parseExpression("\"Hello World\""), parseExpressionViaASM("\"Hello World\"", String.class));
        assertEquals(parseExpression("java.lang.String.class"), parseExpressionViaASM("java.lang.String.class",
                Class.class));
        assertEquals(parseExpression("null"), parseExpressionViaASM("null", Object.class));
    }

    @Test
    public void parseBooleanLiteralExpressions() throws Exception {
        assertEquals(parseExpression("true"), parseExpressionViaASM("true", boolean.class));
        assertEquals(parseExpression("false"), parseExpressionViaASM("false", boolean.class));
    }

    @Test
    public void parseInlinedUnaryExpressions() throws Exception {
        assertEquals(parseExpression("1"), parseExpressionViaASM("+1", int.class));
        assertEquals(parseExpression("-1"), parseExpressionViaASM("~0", int.class));
        assertEquals(parseExpression("false"), parseExpressionViaASM("!true", boolean.class));
        assertEquals(parseExpression("true"), parseExpressionViaASM("!false", boolean.class));
    }

    @Test
    public void parseResolvedParameterExpressions() throws Exception {
        assertEquals(parseExpression("i"), parseExpressionViaASM("i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("j"), parseExpressionViaASM("j", int.class, param(int.class, "i"), param(
                int.class, "j")));
        assertEquals(parseExpression("d"), parseExpressionViaASM("d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("j"), parseExpressionViaASM("j", int.class, param(double.class, "d"), param(
                int.class, "j")));
    }

    @Test
    public void parseThisExpressions() throws Exception {
        assertEquals(parseExpression("this"), parseExpressionViaASM("this", Object.class));
        assertEquals(parseExpression("this"), parseExpressionViaASM("this", Object.class, param(int.class, "i")));
    }

    @Test
    public void parseIincUnaryExpressions() throws Exception {
        assertEquals(parseExpression("i++"), parseExpressionViaASM("i++", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i--"), parseExpressionViaASM("i--", int.class, param(int.class, "i")));
        assertEquals(parseExpression("++i"), parseExpressionViaASM("++i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("--i"), parseExpressionViaASM("--i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("++i"), parseExpressionViaASM("i += 1", int.class, param(int.class, "i")));
        assertEquals(parseExpression("--i"), parseExpressionViaASM("i -= 1", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i += 2"), parseExpressionViaASM("i += 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i -= 2"), parseExpressionViaASM("i -= 2", int.class, param(int.class, "i")));
    }

    @Test
    public void parseInncUnaryExpressionsWhichAreBinaryInSouurce() throws Exception {
        assertEquals(parseExpression("i += 2"), parseExpressionViaASM("i += 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i -= 2"), parseExpressionViaASM("i -= 2", int.class, param(int.class, "i")));
    }

    @Test
    @Ignore("This one includes a branch, but is in scope for 0.2.4")
    public void parseNotUnaryExpression() throws Exception {
        assertEquals(parseExpression("!b"), parseExpressionViaASM("!b", boolean.class, param(boolean.class, "b")));
    }

    @Test
    @Ignore("Bitwise logic is OUT of scope for 0.2.4")
    public void parseXorSourceCodeUnaryExpressionsWhichAreBinaryInBytecode() throws Exception {
        assertEquals(parseExpression("~i"), parseExpressionViaASM("~i", int.class, param(int.class, "i")));
    }

    static int expressionId = 1;

    Param param(Class<?> type, String name) {
        return new Param(type, name);
    }

    class Param {
        Class<?> type;
        String name;

        Param(Class<?> type, String name) {
            this.type = type;
            this.name = name;
        }

        public String toString() {
            return type.getName() + " " + name;
        }
    }

    Expression parseExpressionViaASM(String expression, Class<?> returnValue, Param... parameters) throws Exception {
        Class<?> c = compileExpression(expression, returnValue, parameters);

        Class<?>[] parameterTypes = new Class[parameters.length];
        String[] parameterNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterNames[i] = parameters[i].name;
            parameterTypes[i] = parameters[i].type;
        }

        Method method = c.getDeclaredMethod("eval", parameterTypes);

        debug("compiling: " + expression);
        debug("asm:");
        if (debug)
            printASMifiedMethod(method);

        Expression result = parseExpressionFromMethod(method, parameterNames);

        debug("decompiled: " + result + (result == null ? "" : " // " + result.getClass()));
        debug("");

        return result;
    }

    Class<?> compileExpression(String expression, Class<?> returnValue, Param... parameters) throws IOException {
        String className = "Expression" + expressionId++;
        String parametersString = asList(parameters).toString().replace('[', '(').replace(']', ')');
        String source = "class " + className + " { " + returnValue.getName() + " eval" + parametersString
                + " { return " + expression + "; }}";
        return (Class<?>) compiler.compile(className, source);
    }

    void debug(String msg) {
        if (debug)
            out.println(msg);
    }
}
