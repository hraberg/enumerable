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
    public void applyWithOneArgumentWhenTwoIsNeededMayThrowException() throws Exception {
        Fn2<Integer, Integer, Integer> nTimesMtimesTwo = λ(n, m, n * m * 2);
        assertEquals(8, (int) nTimesMtimesTwo.apply(2));
    }

    @Test
    public void callLambdaInLambda() throws Exception {
        Fn1<Integer, Integer> timesTwo = λ(n, n * 2);
        assertEquals(6, (int) λ(n, m, timesTwo.call(n) + m).call(2, 2));
    }

    @Test
    public <R> void returnAnonymousInnerClassFromLambda() throws Exception {
        Fn1<?, ? extends Callable<String>> returningCallable = λ(_, new Callable<String>() {
            public String call() throws Exception {
                return "hello";
            }
        });
        Callable<String> callable = returningCallable.call();
        assertEquals("hello", callable.call());
        assertNotSame(callable, returningCallable.call());
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
