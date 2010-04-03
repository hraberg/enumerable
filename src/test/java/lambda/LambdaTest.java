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
        Fn3<String, Integer, Integer, String> addWithPrefixString = λ(s, n, m, s + (n + m));
        assertEquals("prefix: 5", addWithPrefixString.call("prefix: ", 2, 3));

        Fn2<Integer, Integer, String> add = addWithPrefixString.partial("result: ");
        assertEquals("result: 2", add.call(1, 1));

        Fn1<Integer, String> add2 = add.partial(2);
        assertEquals("result: 4", add2.call(2));

        Fn0<String> six = add2.partial(4);
        assertEquals("result: 6", six.call());
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

    @Test
    public void applyWithThreeArguments() throws Exception {
        Fn3<String, Integer, Integer, String> addWithPrefixString = λ(s, n, m, s + (n + m));
        assertEquals("total: 4", addWithPrefixString.apply("total: ", 2, 2));
        assertEquals("total: 12", (addWithPrefixString.apply(new Object[] { "total: ", 4, 8 })));
    }

    @Test(expected = NullPointerException.class)
    public void applyWithOneArgumentWhenTwoAreUsedMayThrowException() throws Exception {
        Fn2<Integer, Integer, Integer> nTimesMtimesTwo = λ(n, m, n * m * 2);
        nTimesMtimesTwo.apply(2);
    }

    @Test(expected = NullPointerException.class)
    public void applyWithTwoArgumentsWhenThreeAreUsedMayThrowException() throws Exception {
        Fn3<String, Integer, Integer, String> nTimesMtimesTwoPlusS = λ(s, n, m, n * m * 2 + s);
        nTimesMtimesTwoPlusS.apply(2, 4);
    }

    @Test
    public void applyWithTwoArgumentsWhenThirdArgumentIsNotUsed() throws Exception {
        Fn3<Integer, Integer, String, Integer> nTimesMtimesTwo = λ(n, m, s, n * m);
        assertEquals(8, (int) nTimesMtimesTwo.apply(2, 4));
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
        Fn3<Integer, String, String, String> thirdArgument = λ(n, s, t, t);
        assertNull(thirdArgument.apply());
    }

    @Test
    public void assignLambdaParameter() throws Exception {
        assertEquals(1, (int) λ(n, n = 1).call());
    }

    @Test
    public void incrementLambdaParameter() throws Exception {
        assertEquals(1, (int) λ(n, ++n).call(0));
    }

    @LambdaParameter
    static int[] ints;

    @Test
    public void callLambdaWithArrayArgument() throws Exception {
        Fn3<int[], Integer, Integer, Integer> storeInArray = λ(ints, idx, n, ints[idx] = n);
        int[] array = { 2 };
        storeInArray.call(array, 0, 4);
        assertEquals(4, array[0]);
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
    @SuppressWarnings("null")
    public void recursion() throws Exception {
        Fn1<Integer, Integer> fib = null;
        fib = λ(n, n <= 1 ? n : fib.call(n - 1) + fib.call(n - 2));
        assertEquals(55, (int) fib.call(10));
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
        Callable<?> callable = λ(_, string = "hello").as(Callable.class);
        assertEquals("hello", callable.call());
        assertEquals("hello", string);
    }

    @Test
    public void lambdaAsRunnable() throws Exception {
        String string = "";
        Thread thread = new Thread(λ(_, string = "hello").as(Runnable.class));
        assertEquals("", string);
        thread.start();
        thread.join();
        assertEquals("hello", string);
    }

    @Test
    public void fn1OfFn1FunctionComposition() throws Exception {
        Fn1<Boolean, Boolean> not = λ(b, !b);
        Fn1<Integer, Boolean> even = λ(n, n % 2 == 0);
        assertTrue(even.call(2));

        Fn1<Integer, Boolean> notEven = not.compose(even);
        assertTrue(notEven.call(3));
    }

    @Test
    public void fn1OfFn2FunctionComposition() throws Exception {
        Fn1<Object, String> toString = λ(obj, obj.toString());
        Fn2<Integer, Integer, Integer> times = λ(n, m, n * m);

        Fn2<Integer, Integer, String> toStringTimes = toString.compose(times);
        assertEquals("12", toStringTimes.call(3, 4));
    }
}