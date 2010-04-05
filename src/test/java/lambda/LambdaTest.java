package lambda;

import static java.lang.Thread.*;
import static lambda.Lambda.*;
import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import lambda.annotation.LambdaParameter;
import lambda.annotation.NewLambda;
import lambda.annotation.Unused;
import lambda.exception.LambdaWeavingNotEnabledException;

import org.junit.Test;

public class LambdaTest extends TestBase {
    @Test
    public void creatingLambdaWithNoArgumentsUsingUnusedParameterMarker() throws Exception {
        Fn0<String> hello = λ(_, "hello");
        assertEquals("hello", hello.call());
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

    Fn1<Integer, Integer> fib;

    @Test
    public void recursion() throws Exception {
        fib = λ(n, n <= 1 ? n : fib.call(n - 1) + fib.call(n - 2));
        assertEquals(55, (int) fib.call(10));
    }

    Fn0<?> self;

    @Test
    public void returningThisOfLambda() throws Exception {
        self = λ(_, self);
        assertSame(self, self.call());
    }

    @Test
    public <R> void returnAnonymousInnerClassFromLambda() throws Exception {
        Fn0<? extends Callable<String>> returnsCallable = λ(_, new Callable<String>() {
            public String call() throws Exception {
                return "hello";
            }
        });
        Callable<String> callable = returnsCallable.call();
        assertEquals("hello", callable.call());
        assertNotSame(callable, returnsCallable.call());
    }

    @NewLambda
    static Runnable runnable(Unused _, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    @Test
    public void createSingleMethodInterfaceUsingNewLambda() throws Exception {
        String hello = "";
        Runnable runnable = runnable(_, hello = "hello");
        runnable.run();
        assertEquals("hello", hello);
        assertFalse(runnable instanceof Fn0<?>);
    }

    @NewLambda
    static ActionListener action(ActionEvent e, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    @Test
    public void createSingleMethodInterfaceTakingOneArgumentUsingNewLambda() throws Exception {
        ActionEvent actual;
        ActionEvent event = new ActionEvent(this, 1, "command");
        action(e, actual = e).actionPerformed(event);
        assertSame(event, actual);
    }

    @Test
    public void createSingleMethodInterfaceUsingGenereicDelegate() throws Exception {
        String hello = "";
        Runnable runnable = delegate(_, hello = "hello");
        runnable.run();
        assertEquals("hello", hello);
        assertFalse(runnable instanceof Fn0<?>);
    }

    @Test
    public void createSingleMethodInterfaceTakingOneArgumentUsingGenericDelegate() throws Exception {
        ActionEvent actual;
        ActionListener a = delegate(e, actual = e);
        ActionEvent event = new ActionEvent(this, 1, "command");
        a.actionPerformed(event);
        assertSame(event, actual);
    }
    
    @Test
    public void createSingleMethodInterfaceTakingTwoArgumentsAndReturningPrimitiveUsingGenericDelegate() throws Exception {
        Comparator<Integer> c = delegate(n, m, m - n);
        List<Integer> list = list(1, 2, 3);
        Collections.sort(list, c);
        assertEquals(list(3, 2, 1), list);
    }

    static interface TakesAndReturnsPrimtive {
        public double toDouble(int i);
    }

    @Test
    public void createSingleMethodInterfaceWithPrimtiveArgumentAndReturnUsingGenericDelagate() throws Exception {
        TakesAndReturnsPrimtive t = delegate(n, n);
        assertEquals(2.0, t.toDouble(2), 0);
    }

    static abstract class SingleAbstractMethodNoArgumentsClass {
        public abstract String getMessage();
    }

    @Test
    public void createSingleMethodWithNoArgumentsClassUsingGenericDelagate() throws Exception {
        SingleAbstractMethodNoArgumentsClass m = delegate(_, "hello");
        assertEquals("hello", m.getMessage());
    }

    static abstract class SingleAbstractMethodOneArgumentClass {
        public void ignoredAsNotAbstract() {
        }

        public abstract String getHelloMessage(String name);
    }

    @Test
    public void createSingleMethodWithOneArugmentClassUsingGenericDelagate() throws Exception {
        SingleAbstractMethodOneArgumentClass m = delegate(s, "hello: " + s);
        assertEquals("hello: world", m.getHelloMessage("world"));
    }

    @Test
    public void createSingleAbstractMethodWithLibrarySuperClass() throws Exception {
        Timer timer = new Timer();
        int x = 0;

        TimerTask t = delegate(_, x = 1);
        timer.schedule(t, 50);
        assertEquals(0, x);

        sleep(100);
        assertEquals(1, x);
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
    public void reflectionOnLambda() throws Exception {
        int i = 0;
        Fn1<Integer, Integer> addToI = λ(n, i += n);
        Method fn1Call = Fn1.class.getMethod("call", Object.class);
        assertEquals(1, fn1Call.invoke(addToI, 1));
        assertEquals(1, i);

        Class<?> addToIClass = addToI.getClass();
        assertTrue(addToIClass.isSynthetic());
        assertTrue(Modifier.isFinal(addToIClass.getModifiers()));

        assertEquals(getClass(), addToIClass.getEnclosingClass());
        assertTrue(list(getClass().getDeclaredClasses()).contains(addToIClass));

        Field field = addToIClass.getDeclaredField("i$1");
        assertEquals(int[].class, field.getType());
        assertTrue(field.isSynthetic());
        assertTrue(Modifier.isFinal(field.getModifiers()));

        field.setAccessible(true);
        int[] capturedI = (int[]) field.get(addToI);
        assertEquals(1, i);
        capturedI[0] = 2;
        assertEquals(2, i);
        assertEquals(3, (int) addToI.call(1));
    }

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

    @LambdaParameter
    @SuppressWarnings("unchecked")
    static List l;

    @Test
    @SuppressWarnings("unchecked")
    public void toyScheme() throws Exception {
        Fn1<List, Object> car = λ(l, l.get(0));
        Fn1<List, List> cdr = λ(l, l.subList(1, l.size()));
        Fn2<Object, List, List> cons = λ(obj, l, (l = list(l.toArray())).addAll(0, list(obj)) ? l : l);
        Fn1<Object, Boolean> isPair = λ(obj, obj instanceof List);
        Fn1<Object, Boolean> isAtom = λ(b, !b).compose(isPair);

        List ints = list(1, 2, 3);

        assertEquals(1, car.call(ints));
        assertEquals(list(2, 3), cdr.call(ints));

        assertEquals(list(0, 1, 2, 3), cons.call(0, ints));

        assertTrue(isAtom.call(car.call(ints)));
        assertTrue(isPair.call(cdr.call(ints)));
    }
}