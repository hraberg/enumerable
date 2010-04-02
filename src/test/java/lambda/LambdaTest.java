package lambda;

import static lambda.Lambda.*;
import static lambda.enumerable.EnumerableTest.*;
import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

public class LambdaTest {
    @Test
    public void partialApplication() throws Exception {
        Fn2<Integer, Integer, Integer> add = λ(n, m, n + m);
        assertEquals(2, (int) add.call(1, 1));

        Fn1<Integer, Integer> add2 = add.partial(2);
        assertEquals(4, (int) add2.call(2));

        Fn0<Integer> six = add2.partial(4);
        assertEquals(6, (int) six.call());
    }

    @Test
    public void applyWithOneArgument() throws Exception {
        Fn1<Integer, Integer> nTimesTwo = λ(n, n * 2);
        assertEquals(4, (int) nTimesTwo.apply(2));
        assertEquals(8, (int) nTimesTwo.apply(new Object[] { 4 }));
    }

    @Test
    public void applyWithTwoArguments() throws Exception {
        Fn2<Integer, Integer, Integer> nTimesMtimesTwo = λ(n, m, n * m * 2);
        assertEquals(8, (int) nTimesMtimesTwo.apply(2, 2));
        assertEquals(64, (int) nTimesMtimesTwo.apply(new Object[] { 4, 8 }));
    }

    @Test(expected = NullPointerException.class)
    public void applyWithOneArgumentWhenTwoAreUsedMayThrowException() throws Exception {
        Fn2<Integer, Integer, Integer> nTimesMtimesTwo = λ(n, m, n * m * 2);
        assertEquals(8, (int) nTimesMtimesTwo.apply(2));
    }

    @Test
    public void applyWithOneArgumentWhenSecondArgumentIsNotUsed() throws Exception {
        Fn2<Integer, Integer, Integer> firstArgument = λ(n, m, n);
        assertEquals(2, (int) firstArgument.apply(2));
    }

    @Test
    public void applyWithOneArgumentSetsSecondArgumentToNull() throws Exception {
        Fn2<String, String, String> secondArgument = λ(s, t, t);
        assertNull(secondArgument.apply("hello"));
    }

    @Test(expected = NullPointerException.class)
    public void applyWithNoArgumentWhenOneIsUsedMayThrowException() throws Exception {
        Fn1<Integer, Integer> inc = λ(n, n + 1);
        inc.apply();
    }

    @Test
    public void applyWithNoArgumentWhenArgumentIsNotUsed() throws Exception {
        Fn1<Integer, Integer> firstArgument = λ(n, 2);
        assertEquals(2, (int) firstArgument.apply());
    }

    @Test
    public void applyWithNoArgumentSetsArgumentsToNull() throws Exception {
        Fn1<String, String> firstArgument = λ(s, s);
        assertNull(firstArgument.apply());
        Fn2<String, String, String> secondArgument = λ(s, t, t);
        assertNull(secondArgument.apply());
    }

    @Test
    public void assignLambdaParameter() throws Exception {
        assertEquals(1, (int) λ(n, n = 1).call());
    }

    @Test
    public void incrementLambdaParameter() throws Exception {
        assertEquals(1, (int) λ(n, ++n).call(0));
    }

    @Test
    public void callLambdaInLambda() throws Exception {
        Fn1<Integer, Integer> timesTwo = λ(n, n * 2);
        assertEquals(6, (int) λ(n, m, timesTwo.call(n) + m).call(2, 2));
    }

    @LambdaParameter
    static Fn1<Integer, Integer> intToInt;

    @Test
    public void callLambdaWithLambdaArgument() throws Exception {
        Fn1<Integer, Integer> timesTwo = λ(n, n * 2);
        assertEquals(4, (int) λ(intToInt, intToInt.call(2)).call(timesTwo));
    }

    @Test
    public <R> void returnAnonymousInnerClassFromLambda() throws Exception {
        Fn1<?, ? extends Callable<String>> returnsCallable = λ(_, new Callable<String>() {
            public String call() throws Exception {
                return "hello";
            }
        });
        Callable<String> callable = returnsCallable.call();
        assertEquals("hello", callable.call());
        assertNotSame(callable, returnsCallable.call());
    }

    @LambdaParameter
    static ActionEvent e;

    @Test
    public void oneArgumentLambdaAsInterface() throws Exception {
        ActionEvent actual;
        ActionListener a = λ(e, actual = e).as(ActionListener.class);
        ActionEvent event = new ActionEvent(this, 1, "command");
        a.actionPerformed(event);
        assertSame(event, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoArgumentLambdaAsInterface() throws Exception {
        Comparator<Integer> c = λ(n, m, m - n).as(Comparator.class);
        List<Integer> list = list(1, 2, 3);
        Collections.sort(list, c);
        assertEquals(list(3, 2, 1), list);
    }

    @Test
    public void oneArgumentLambdaAsInterfaceWithZeroArgumentMethod() throws Exception {
        String string = "";
        Runnable runnable = λ(_, string = "hello").as(Runnable.class);
        runnable.run();
        assertEquals("hello", string);
    }
}
