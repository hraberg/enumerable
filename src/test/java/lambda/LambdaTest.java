package lambda;

import static lambda.Lambda.*;
import static lambda.enumerable.EnumerableTest.*;
import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class LambdaTest {
    @Test
    public void partialApplication() throws Exception {
        Fn2<Integer, Integer, Integer> add = λ(n, m, n + m);
        assertEquals(2, (int) add.call(1, 1));

        Fn1<Integer, Integer> add2 = partial(add, 2);
        assertEquals(4, (int) add2.call(2));

        Fn0<Integer> six = partial(add2, 4);
        assertEquals(6, (int) six.call());
    }

    @Test
    public void useLambdaInLambda() throws Exception {
        Fn1<Integer, Integer> timesTwo = λ(n, n * 2);
        assertEquals(6, (int) λ(n, m, timesTwo.call(n) + m).call(2, 2));
    }

    @LambdaParameter
    static ActionEvent e;

    @Test
    public void oneArgumentLambdaAsInterface() throws Exception {
        ActionEvent actual;
        ActionListener a = as(ActionListener.class, λ(e, actual = e));
        ActionEvent event = new ActionEvent(this, 1, "command");
        a.actionPerformed(event);
        assertSame(event, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void twoArgumentLambdaAsInterface() throws Exception {
        Comparator<Integer> c = as(Comparator.class, λ(n, m, m - n));
        List<Integer> list = list(1, 2, 3);
        Collections.sort(list, c);
        assertEquals(list(3, 2, 1), list);
    }

    @Test
    public void oneArgumentLambdaAsInterfaceWithZeroArgumentMethod() throws Exception {
        String string = "";
        Runnable runnable = as(Runnable.class, λ(_, string = "hello"));
        runnable.run();
        assertEquals("hello", string);
    }
}
