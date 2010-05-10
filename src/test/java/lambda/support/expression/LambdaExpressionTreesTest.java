package lambda.support.expression;

import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static lambda.primitives.LambdaPrimitives.*;
import static lambda.support.expression.LambdaExpressionTrees.*;
import static org.junit.Assert.*;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;

import org.junit.Test;

public class LambdaExpressionTreesTest {
    @Test
    public void turnLambdasIntoExpressions() throws Exception {
        assertEquals(new NullLiteralExpr(), toExpression(λ(null)));
        assertEquals(parseExpression("n * 2"), toExpression(λ(n, n * 2)));
        assertEquals(parseExpression("n * m"), toExpression(λ(n, m, n * m)));
    }

    @Test
    public void turnExpressionsIntoLambdas() throws Exception {
        Fn0<Object> fn0 = toFn0(Object.class, new NullLiteralExpr());
        assertNull(fn0.call());

        Fn1<Integer, Integer> fn1 = toFn1(Integer.class, Integer.class, "n", parseExpression("n * 2"));
        assertEquals(4, (int) fn1.call(2));

        Fn2<Integer, Integer, Integer> fn2 = toFn2(Integer.class, Integer.class, "n", Integer.class, "m",
                parseExpression("n * m"));
        assertEquals(8, (int) fn2.call(2, 4));
    }

    @Test
    public void turnLambdasIntoExpressionsAndThenBackToLambdaAfterModification() throws Exception {
        BinaryExpr expression = toExpression(λ(n, n * 2));
        expression.setOperator(BinaryExpr.Operator.divide);

        assertEquals(parseExpression("n / 2"), expression);
        Fn1<Integer, Integer> half = toFn1(Integer.class, Integer.class, "n", expression);
        assertEquals(2, (int) half.call(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void turnClosureIntoExpressionThrowsException() throws Exception {
        int i = 1;
        toExpression(λ(i));
    }

}
