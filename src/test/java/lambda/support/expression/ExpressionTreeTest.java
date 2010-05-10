package lambda.support.expression;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static lambda.support.expression.ExpressionInterpreter.*;
import static org.junit.Assert.*;
import japa.parser.ast.expr.Expression;

import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

public class ExpressionTreeTest {
    static boolean debug = false;

    InMemoryCompiler compiler = new InMemoryCompiler();

    @Test
    public void parseLiteralExpressions() throws Exception {
        assertEquals(parseExpression("0"), parseViaASM("0", int.class));
        assertEquals(parseExpression("-1"), parseViaASM("-1", int.class));
        assertEquals(parseExpression("1"), parseViaASM("1", int.class));
        assertEquals(parseExpression("42"), parseViaASM("42", int.class));
        assertEquals(parseExpression("" + (1024 * 1024)), parseViaASM("" + (1024 * 1024), int.class));
        assertEquals(parseExpression("1.0"), parseViaASM("1.0", double.class));
        assertEquals(parseExpression("1L"), parseViaASM("1L", long.class));
        assertEquals(parseExpression("42L"), parseViaASM("42L", long.class));
        assertEquals(parseExpression("1.0f"), parseViaASM("1.0f", float.class));
        assertEquals(parseExpression("42.0f"), parseViaASM("42.0f", float.class));
        assertEquals(parseExpression("-2"), parseViaASM("-2", int.class));
        assertEquals(parseExpression("-1.0"), parseViaASM("-1.0", double.class));
        assertEquals(parseExpression("-1L"), parseViaASM("-1L", long.class));
        assertEquals(parseExpression("-1.0f"), parseViaASM("-1.0f", float.class));
        assertEquals(parseExpression("" + (-1024 * 1024)), parseViaASM("" + (-1024 * 1024), int.class));
        assertEquals(parseExpression("\"Hello World\""), parseViaASM("\"Hello World\"", String.class));
        assertEquals(parseExpression("java.lang.String.class"), parseViaASM("java.lang.String.class", Class.class));
        assertEquals(parseExpression("null"), parseViaASM("null", Object.class));
    }

    @Test
    public void parseCharLiteralExpressionWhichCannotBeToldFromIntsInBytecode() throws Exception {
        assertEquals(parseExpression("97"), parseViaASM("'a'", char.class));
    }

    @Test
    public void parseBooleanLiteralExpressions() throws Exception {
        assertEquals(parseExpression("true"), parseViaASM("true", boolean.class));
        assertEquals(parseExpression("false"), parseViaASM("false", boolean.class));
    }

    @Test
    public void parseInlinedUnaryExpressions() throws Exception {
        assertEquals(parseExpression("1"), parseViaASM("+1", int.class));
        assertEquals(parseExpression("-1"), parseViaASM("~0", int.class));
        assertEquals(parseExpression("false"), parseViaASM("!true", boolean.class));
        assertEquals(parseExpression("true"), parseViaASM("!false", boolean.class));
    }

    @Test
    public void parseResolvedParameterExpressions() throws Exception {
        assertEquals(parseExpression("i"), parseViaASM("i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("j"),
                parseViaASM("j", int.class, param(int.class, "i"), param(int.class, "j")));
        assertEquals(parseExpression("d"), parseViaASM("d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("j"), parseViaASM("j", int.class, param(double.class, "d"), param(int.class,
                "j")));
    }

    @Test
    public void parseThisExpressions() throws Exception {
        assertEquals(parseExpression("this"), parseViaASM("this", Object.class));
        assertEquals(parseExpression("this"), parseViaASM("this", Object.class, param(int.class, "i")));
    }

    @Test
    public void parseIincUnaryExpressions() throws Exception {
        assertEquals(parseExpression("i++"), parseViaASM("i++", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i--"), parseViaASM("i--", int.class, param(int.class, "i")));
        assertEquals(parseExpression("++i"), parseViaASM("++i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("--i"), parseViaASM("--i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("++i"), parseViaASM("i += 1", int.class, param(int.class, "i")));
        assertEquals(parseExpression("--i"), parseViaASM("i -= 1", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i += 2"), parseViaASM("i += 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i -= 2"), parseViaASM("i -= 2", int.class, param(int.class, "i")));
    }

    @Test
    public void parseNegateUnaryExpressions() throws Exception {
        assertEquals(parseExpression("-i"), parseViaASM("-i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("-d"), parseViaASM("-d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("-f"), parseViaASM("-f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("-l"), parseViaASM("-l", long.class, param(long.class, "l")));
    }

    @Test
    public void parseInncUnaryExpressionsWhichAreBinaryInSouuce() throws Exception {
        assertEquals(parseExpression("i += 2"), parseViaASM("i += 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i -= 2"), parseViaASM("i -= 2", int.class, param(int.class, "i")));
    }

    @Test
    public void parseIncrementAssignUnaryExpressionsWhichAreBinaryInSouuce() throws Exception {
        assertEquals(parseExpression("d += 2.0"), parseViaASM("d += 2.0", double.class, param(double.class, "d")));
        assertEquals(parseExpression("d -= 2.0"), parseViaASM("d -= 2.0", double.class, param(double.class, "d")));
        assertEquals(parseExpression("f += 2.0f"), parseViaASM("f += 2.0f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("f -= 2.0f"), parseViaASM("f -= 2.0f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("l += 2L"), parseViaASM("l += 2L", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l -= 2L"), parseViaASM("l -= 2L", long.class, param(long.class, "l")));
    }

    @Test
    public void parseInverseUnaryExpressionsWhichAreBinaryXorInBytecode() throws Exception {
        assertEquals(parseExpression("~i"), parseViaASM("~i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("~l"), parseViaASM("~l", long.class, param(long.class, "l")));
    }

    @Test
    public void parsePrimitiveCastUnaryExpressions() throws Exception {
        assertEquals(parseExpression("(int) l"), parseViaASM("(int) l", int.class, param(long.class, "l")));
        assertEquals(parseExpression("(int) f"), parseViaASM("(int) f", int.class, param(float.class, "f")));
        assertEquals(parseExpression("(int) d"), parseViaASM("(int) d", int.class, param(double.class, "d")));
        assertEquals(parseExpression("(byte) i"), parseViaASM("(byte) i", byte.class, param(int.class, "i")));
        assertEquals(parseExpression("(char) i"), parseViaASM("(char) i", char.class, param(int.class, "i")));
        assertEquals(parseExpression("(short) i"), parseViaASM("(short) i", short.class, param(int.class, "i")));
        assertEquals(parseExpression("(float) i"), parseViaASM("(float) i", float.class, param(int.class, "i")));
        assertEquals(parseExpression("(float) l"), parseViaASM("(float) l", float.class, param(long.class, "l")));
        assertEquals(parseExpression("(float) d"), parseViaASM("(float) d", float.class, param(double.class, "d")));
        assertEquals(parseExpression("(long) i"), parseViaASM("(long) i", long.class, param(int.class, "i")));
        assertEquals(parseExpression("(long) f"), parseViaASM("(long) f", long.class, param(float.class, "f")));
        assertEquals(parseExpression("(long) d"), parseViaASM("(long) d", long.class, param(double.class, "d")));
        assertEquals(parseExpression("(double) i"), parseViaASM("(double) i", double.class, param(int.class, "i")));
        assertEquals(parseExpression("(double) l"), parseViaASM("(double) l", double.class, param(long.class, "l")));
        assertEquals(parseExpression("(double) f"),
                parseViaASM("(double) f", double.class, param(float.class, "f")));
    }

    @Test
    public void parseCastUnaryExpressions() throws Exception {
        assertEquals(parseExpression("(String) o"), parseViaASM("(String) o", String.class,
                param(Object.class, "o")));
        assertEquals(parseExpression("(java.util.List) o"), parseViaASM("(java.util.List) o", List.class, param(
                Object.class, "o")));
    }

    @Test
    public void parseCastUnaryExpressionsWhichGetsRemovedInBytecode() throws Exception {
        assertEquals(parseExpression("o"), parseViaASM("(Object) o", Object.class, param(Object.class, "o")));
    }

    @Test
    public void parseMethodCalls() throws Exception {
        assertEquals(parseExpression("System.currentTimeMillis()"), parseViaASM("System.currentTimeMillis()",
                long.class));
        assertEquals(parseExpression("toString()"), parseViaASM("toString()", String.class));
        assertEquals(parseExpression("s.toUpperCase()"), parseViaASM("s.toUpperCase()", String.class, param(
                String.class, "s")));
        assertEquals(parseExpression("equals(\"Hello World\")"), parseViaASM("equals(\"Hello World\")",
                boolean.class));
        assertEquals(parseExpression("equals(s)"),
                parseViaASM("equals(s)", boolean.class, param(String.class, "s")));
    }

    @Test
    public void parseArithmeticBinaryExpressions() throws Exception {
        assertEquals(parseExpression("i + i"), parseViaASM("i + i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i - i"), parseViaASM("i - i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i * i"), parseViaASM("i * i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i / i"), parseViaASM("i / i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i % i"), parseViaASM("i % i", int.class, param(int.class, "i")));

        assertEquals(parseExpression("d + d"), parseViaASM("d + d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("d - d"), parseViaASM("d - d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("d * d"), parseViaASM("d * d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("d / d"), parseViaASM("d / d", double.class, param(double.class, "d")));
        assertEquals(parseExpression("d % d"), parseViaASM("d % d", double.class, param(double.class, "d")));

        assertEquals(parseExpression("f + f"), parseViaASM("f + f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("f - f"), parseViaASM("f - f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("f * f"), parseViaASM("f * f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("f / f"), parseViaASM("f / f", float.class, param(float.class, "f")));
        assertEquals(parseExpression("f % f"), parseViaASM("f % f", float.class, param(float.class, "f")));

        assertEquals(parseExpression("l + l"), parseViaASM("l + l", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l - l"), parseViaASM("l - l", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l * l"), parseViaASM("l * l", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l / l"), parseViaASM("l / l", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l % l"), parseViaASM("l % l", long.class, param(long.class, "l")));
    }

    @Test
    public void parseShiftBinaryExpressions() throws Exception {
        assertEquals(parseExpression("i << 2"), parseViaASM("i << 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i >> 2"), parseViaASM("i >> 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i >>> 2"), parseViaASM("i >>> 2", int.class, param(int.class, "i")));

        assertEquals(parseExpression("l << 2"), parseViaASM("l << 2", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l >> 2"), parseViaASM("l >> 2", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l >>> 2"), parseViaASM("l >>> 2", long.class, param(long.class, "l")));
    }

    @Test
    public void parseBitwiseBinaryExpressions() throws Exception {
        assertEquals(parseExpression("i | i"), parseViaASM("i | i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i & i"), parseViaASM("i & i", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i ^ i"), parseViaASM("i ^ i", int.class, param(int.class, "i")));

        assertEquals(parseExpression("l | l"), parseViaASM("l | l", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l & l"), parseViaASM("l & l", long.class, param(long.class, "l")));
        assertEquals(parseExpression("l ^ l"), parseViaASM("l ^ l", long.class, param(long.class, "l")));
    }

    @Test
    public void parseAssignExpressions() throws Exception {
        assertEquals(parseExpression("i = 2"), parseViaASM("i = 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("Math.max(i = 2, i)"), parseViaASM("Math.max(i = 2, i)", int.class, param(
                int.class, "i")));
    }

    @Test
    public void parseAssignExpressionsWhichAreExpandedInBytecode() throws Exception {
        assertEquals(parseExpression("i -= 2"), parseViaASM("i -= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i *= 2"), parseViaASM("i *= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i /= 2"), parseViaASM("i /= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i &= 2"), parseViaASM("i &= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i |= 2"), parseViaASM("i |= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i ^= 2"), parseViaASM("i ^= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i %= 2"), parseViaASM("i %= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i <<= 2"), parseViaASM("i <<= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i >>= 2"), parseViaASM("i >>= 2", int.class, param(int.class, "i")));
        assertEquals(parseExpression("i >>>= 2"), parseViaASM("i >>>= 2", int.class, param(int.class, "i")));
    }

    @Test
    public void parseArrayAccessExpressions() throws Exception {
        assertEquals(parseExpression("i[0]"), parseViaASM("i[0]", int.class, param(int[].class, "i")));
        assertEquals(parseExpression("d[0]"), parseViaASM("d[0]", double.class, param(double[].class, "d")));
        assertEquals(parseExpression("f[0]"), parseViaASM("f[0]", float.class, param(float[].class, "f")));
        assertEquals(parseExpression("l[0]"), parseViaASM("l[0]", long.class, param(long[].class, "l")));
        assertEquals(parseExpression("i[0] = 2"), parseViaASM("i[0] = 2", int.class, param(int[].class, "i")));
        assertEquals(parseExpression("d[0] = 2.0"), parseViaASM("d[0] = 2.0", double.class, param(double[].class,
                "d")));
        assertEquals(parseExpression("f[0] = 2.0f"), parseViaASM("f[0] = 2.0f", float.class, param(float[].class,
                "f")));
        assertEquals(parseExpression("l[0] = 2L"), parseViaASM("l[0] = 2L", long.class, param(long[].class, "l")));
    }

    @Test
    public void parseArrayLengthExpression() throws Exception {
        assertEquals(parseExpression("i.length"), parseViaASM("i.length", int.class, param(int[].class, "i")));
    }

    @Test
    public void parseFieldExpressions() throws Exception {
        assertEquals(parseExpression("d.width"), parseViaASM("d.width", int.class, param(Dimension.class, "d")));
        assertEquals(parseExpression("System.out"), parseViaASM("System.out", PrintStream.class));
        assertEquals(parseExpression("d.width = 1"), parseViaASM("d.width = 1", int.class, param(Dimension.class,
                "d")));
    }

    @Test
    public void parseNewExpression() throws Exception {
        assertEquals(parseExpression("new String()"), parseViaASM("new String()", String.class));
        assertEquals(parseExpression("new String(\"Hello World\")"), parseViaASM("new String(\"Hello World\")",
                String.class));
    }

    @Test
    public void parseNewArrayExpression() throws Exception {
        assertEquals(parseExpression("new String[0]"), parseViaASM("new String[0]", String[].class));
        assertEquals(parseExpression("new String[] {\"Hello World\"}"), parseViaASM(
                "new String[] {\"Hello World\"}", String[].class));
        assertEquals(parseExpression("new String[] {\"Hello\", \"World\"}"), parseViaASM(
                "new String[] {\"Hello\", \"World\"}", String[].class));
    }

    @Test
    public void parseNewPrimtiveArrayExpression() throws Exception {
        assertEquals(parseExpression("new int[0]"), parseViaASM("new int[0]", int[].class));
        assertEquals(parseExpression("new int[] {1}"), parseViaASM("new int[] {1}", int[].class));
        assertEquals(parseExpression("new int[] {1, 2}"), parseViaASM("new int[] {1, 2}", int[].class));

        assertEquals(parseExpression("new double[0]"), parseViaASM("new double[0]", double[].class));
        assertEquals(parseExpression("new double[] {1.0}"), parseViaASM("new double[] {1.0}", double[].class));
        assertEquals(parseExpression("new double[] {1.0, 2.0}"), parseViaASM("new double[] {1.0, 2.0}",
                double[].class));

        assertEquals(parseExpression("new float[0]"), parseViaASM("new float[0]", float[].class));
        assertEquals(parseExpression("new float[] {1.0f}"), parseViaASM("new float[] {1.0f}", float[].class));
        assertEquals(parseExpression("new float[] {1.0f, 2.0f}"), parseViaASM("new float[] {1.0f, 2.0f}",
                float[].class));

        assertEquals(parseExpression("new long[0]"), parseViaASM("new long[0]", long[].class));
        assertEquals(parseExpression("new long[] {1L}"), parseViaASM("new long[] {1L}", long[].class));
        assertEquals(parseExpression("new long[] {1L, 2L}"), parseViaASM("new long[] {1L, 2L}", long[].class));
    }

    @Test
    public void parseNewArrayExpressionWithEmptyInitilizerWhichGetsRemovedInBytecode() throws Exception {
        assertEquals(parseExpression("new String[0]"), parseViaASM("new String[] {}", String[].class));
    }

    @Test
    public void parseNotUnaryExpression() throws Exception {
        assertEquals(parseExpression("!b"), parseViaASM("!b", boolean.class, param(boolean.class, "b")));
    }

    @Test
    public void parseIntComparsionBinaryExpression() throws Exception {
        assertEquals(parseExpression("i == 1"), parseViaASM("i == 1", boolean.class, param(int.class, "i")));
        assertEquals(parseExpression("i != 1"), parseViaASM("i != 1", boolean.class, param(int.class, "i")));
        assertEquals(parseExpression("i < 1"), parseViaASM("i < 1", boolean.class, param(int.class, "i")));
        assertEquals(parseExpression("i > 1"), parseViaASM("i > 1", boolean.class, param(int.class, "i")));
        assertEquals(parseExpression("i <= 1"), parseViaASM("i <= 1", boolean.class, param(int.class, "i")));
        assertEquals(parseExpression("i >= 1"), parseViaASM("i >= 1", boolean.class, param(int.class, "i")));
    }

    @Test
    public void parseReferenceComparsionBinaryExpression() throws Exception {
        assertEquals(parseExpression("o1 == o2"), parseViaASM("o1 == o2", boolean.class, param(Object.class, "o1"),
                param(Object.class, "o2")));
        assertEquals(parseExpression("o1 != o2"), parseViaASM("o1 != o2", boolean.class, param(Object.class, "o1"),
                param(Object.class, "o2")));
    }

    @Test
    public void parseLongComparsionBinaryExpression() throws Exception {
        assertEquals(parseExpression("l == 1L"), parseViaASM("l == 1L", boolean.class, param(long.class, "l")));
        assertEquals(parseExpression("l != 1L"), parseViaASM("l != 1L", boolean.class, param(long.class, "l")));
        assertEquals(parseExpression("l < 1L"), parseViaASM("l < 1L", boolean.class, param(long.class, "l")));
        assertEquals(parseExpression("l > 1L"), parseViaASM("l > 1L", boolean.class, param(long.class, "l")));
        assertEquals(parseExpression("l <= 1L"), parseViaASM("l <= 1L", boolean.class, param(long.class, "l")));
        assertEquals(parseExpression("l >= 1L"), parseViaASM("l >= 1L", boolean.class, param(long.class, "l")));
    }

    @Test
    public void parseFloatComparsionBinaryExpression() throws Exception {
        assertEquals(parseExpression("f == 1.0f"), parseViaASM("f == 1.0f", boolean.class, param(float.class, "f")));
        assertEquals(parseExpression("f != 1.0f"), parseViaASM("f != 1.0f", boolean.class, param(float.class, "f")));
        assertEquals(parseExpression("f < 1.0f"), parseViaASM("f < 1.0f", boolean.class, param(float.class, "f")));
        assertEquals(parseExpression("f > 1.0f"), parseViaASM("f > 1.0f", boolean.class, param(float.class, "f")));
        assertEquals(parseExpression("f <= 1.0f"), parseViaASM("f <= 1.0f", boolean.class, param(float.class, "f")));
        assertEquals(parseExpression("f >= 1.0f"), parseViaASM("f >= 1.0f", boolean.class, param(float.class, "f")));
    }

    @Test
    public void parseDoubleComparsionBinaryExpression() throws Exception {
        assertEquals(parseExpression("d == 1.0"), parseViaASM("d == 1.0", boolean.class, param(double.class, "d")));
        assertEquals(parseExpression("d != 1.0"), parseViaASM("d != 1.0", boolean.class, param(double.class, "d")));
        assertEquals(parseExpression("d < 1.0"), parseViaASM("d < 1.0", boolean.class, param(double.class, "d")));
        assertEquals(parseExpression("d > 1.0"), parseViaASM("d > 1.0", boolean.class, param(double.class, "d")));
        assertEquals(parseExpression("d <= 1.0"), parseViaASM("d <= 1.0", boolean.class, param(double.class, "d")));
        assertEquals(parseExpression("d >= 1.0"), parseViaASM("d >= 1.0", boolean.class, param(double.class, "d")));
    }

    @Test
    public void parseConditionalTernaryExpression() throws Exception {
        assertEquals(parseExpression("b ? \"Hello\" : \"World\""), parseViaASM("b ? \"Hello\" : \"World\"",
                String.class, param(boolean.class, "b")));
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
            if (type.isArray())
                return type.getComponentType() + "[] " + name;
            return type.getName() + " " + name;
        }
    }

    Expression parseViaASM(String expression, Class<?> returnValue, Param... parameters) throws Exception {
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

        Expression result = parseExpressionFromSingleMethodClass(c, parameterNames);

        debug("decompiled: " + result + (result == null ? "" : " // " + result.getClass()));
        debug("");

        return result;
    }

    Class<?> compileExpression(String expression, Class<?> returnValue, Param... parameters) throws IOException {
        String className = "Expression" + expressionId++;
        String parametersString = asList(parameters).toString();
        parametersString = "(" + parametersString.substring(1, parametersString.length() - 1) + ")";
        parametersString = parametersString.substring(0, parametersString.length() - 1) + ")";
        String returnType = returnValue.isArray() ? returnValue.getComponentType().getName() + "[]" : returnValue
                .getName();
        String source = "class " + className + " { " + returnType + " eval" + parametersString + " { return "
                + expression + "; }}";
        return (Class<?>) compiler.compile(className, source);
    }

    void debug(String msg) {
        if (debug)
            out.println(msg);
    }
}
