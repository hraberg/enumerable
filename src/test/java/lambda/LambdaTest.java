package lambda;

import static java.lang.Thread.*;
import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static lambda.primitives.LambdaPrimitives.*;
import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.swing.JButton;

import lambda.annotation.LambdaParameter;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import lambda.primitives.Fn1ItoB;
import lambda.primitives.Fn1ItoI;

import org.junit.Test;

public class LambdaTest extends TestBase {
    @Test
    public void lambdaWithOnePrimitiveArgument() throws Exception {
        Fn1<Integer, Integer> nTimesTwo = λ(n, n * 2);
        assertTrue(nTimesTwo instanceof Fn1ItoI);
        assertEquals(4, (int) nTimesTwo.call(2));
    }

    @Test
    public void creatingLambdaWithNoArguments() throws Exception {
        Fn0<String> hello = λ("hello");
        assertEquals("hello", hello.call());
    }

    @Test
    public void creatingLambdaWithNoExpression() throws Exception {
        Fn0<?> none = λ();
        assertNull(none.call());
    }

    @Test
    public void nestedLambdas() throws Exception {
        int four = λ(n, λ(n, n * n).call(n)).call(2);
        assertEquals(4, four);
    }

    @Test
    public void nestedNestedLambdas() throws Exception {
        int eight = λ(n, λ(n, n * λ(n, n * n).call(n)).call(n)).call(2);
        assertEquals(8, eight);
    }

    @Test
    public void nestedLambdasClosingOverLocalVariable() throws Exception {
        int two = 2;
        int eight = λ(n, λ(n, n * two).call(n)).call(4);
        assertEquals(8, eight);
    }

    @Test
    public void nestedNestedLambdasClosingOverLocalVariable() throws Exception {
        int two = 2;
        int four = 4;
        int thirtytwo = λ(n, λ(n, two * n * λ(n, n * four).call(n)).call(n)).call(2);
        assertEquals(32, thirtytwo);
    }

    @Test
    public void nestedLambdasShadowingParentLambdaParameter() throws Exception {
        int sixteen = λ(n, λ(n, n * n).call(n * 2)).call(2);
        assertEquals(16, sixteen);
    }

    @Test
    public void nestedLambdasClosingOverParentLambdaParameter() throws Exception {
        int four = λ(n, λ(m, n * m).call(n)).call(2);
        assertEquals(4, four);
    }

    @Test
    public void nestedNestedLambdasClosingOverParentLambdaParameter() throws Exception {
        int two = 2;
        int four = 4;
        int thirtytwo = λ(n, λ(m, two * n * λ(m * four).call()).call(n)).call(2);
        assertEquals(32, thirtytwo);
    }

    @Test
    public void nestedZeroArgumentLambdasInOneArgumentLambda() throws Exception {
        int four = λ(n, λ(4).call()).call(0);
        assertEquals(4, four);
    }

    @Test
    public void nestedZeroArgumentLambdas() throws Exception {
        int four = λ(λ(4).call()).call();
        assertEquals(4, four);
    }

    @Test
    public void nestedLambdasClosingOverMutableParentLambdaParameter() throws Exception {
        int eight = λ(n, λ(m, n = n * m).call(n) + n).call(2);
        assertEquals(8, eight);
    }

    @Test
    public void nestedNesterLambdasClosingOverMutableParentLambdaParameter() throws Exception {
        int eight = λ(n, λ(m, λ(n = n * m).call()).call(n) + n).call(2);
        assertEquals(8, eight);
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
        Fn2<Integer, Object, Integer> firstArgument = λ(n, obj, n);
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
        Fn0<Integer> firstArgument = λ(2);
        assertEquals(2, (int) firstArgument.apply());
    }

    @Test(expected = NullPointerException.class)
    public void applyWithNoArgumentWhenArgumentIsPrimitiveThrowsNullPointerException() throws Exception {
        Fn0<Integer> firstArgument = λ(n, 2);
        assertEquals(2, (int) firstArgument.apply());
    }

    @Test
    public void applyWithNoArgumentSetsArgumentsToNull() throws Exception {
        Fn1<String, String> firstArgument = λ(s, s);
        assertNull(firstArgument.apply());
        Fn2<String, String, String> secondArgument = λ(s, t, t);
        assertNull(secondArgument.apply());
        Fn3<Object, String, String, String> thirdArgument = λ(obj, s, t, t);
        assertNull(thirdArgument.apply());
    }

    @Test
    public void defaultValueForSecondArgument() throws Exception {
        Fn2<Integer, Integer, Integer> nTimesM = λ(n, m = 3, n * m);
        assertEquals(4, (int) nTimesM.call(2, 2));
        assertEquals(6, (int) nTimesM.call(2));
    }

    @Test
    public void defaultComputatedValueForSecondArgument() throws Exception {
        Fn2<Integer, Integer, Integer> nTimesM = λ(n, m = 3 * 2, n * m);
        assertEquals(4, (int) nTimesM.call(2, 2));
        assertEquals(12, (int) nTimesM.call(2));
    }

    @Test
    public void defaultReferenceValueForSecondArgument() throws Exception {
        Fn2<Integer, String, String> nWithPrefix = λ(n, s = "", s + n);
        assertEquals("prefix 2", nWithPrefix.call(2, "prefix "));
        assertEquals("2", nWithPrefix.call(2));
    }

    @Test
    public void defaultValueForThirdArgument() throws Exception {
        Fn3<String, Integer, Integer, String> addWithPrefixString = λ(s, n, m = 4, s + (n + m));
        assertEquals("total: 4", addWithPrefixString.call("total: ", 2, 2));
        assertEquals("total: 6", addWithPrefixString.call("total: ", 2));
    }

    @Test
    public void defaultValueForSecondAndThirdArgument() throws Exception {
        Fn3<String, Integer, Integer, String> addWithPrefixString = λ(s, n = 8, m = 4, s + (n + m));
        assertEquals("total: 4", addWithPrefixString.call("total: ", 2, 2));
        assertEquals("total: 6", addWithPrefixString.call("total: ", 2));
        assertEquals("total: 12", addWithPrefixString.call("total: "));
    }

    @Test
    public void arities() throws Exception {
        assertEquals(0, λ().arity);
        assertEquals(0, λ(null).arity);
        assertEquals(1, λ(n, null).arity);
        assertEquals(2, λ(n, m, null).arity);
        assertEquals(3, λ(n, m, s, null).arity);
    }

    @Test
    public void assignLambdaParameter() throws Exception {
        assertEquals(1, λ(n, n = 1).call(5));
    }

    @Test
    public void incrementLambdaParameter() throws Exception {
        assertEquals(1, λ(n, ++n).call(0));
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
        assertEquals(6, λ(n, m, timesTwo.call(n) + m).call(2, 2));
    }

    @LambdaParameter
    static Fn1<String, String> stringToString;

    @Test
    public void callLambdaWithLambdaArgument() throws Exception {
        Fn1<String, String> toUpperCase = λ(s, s.toUpperCase());
        assertEquals("HELLO", λ(stringToString, stringToString.call("hello")).call(toUpperCase));
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
        self = λ(self);
        assertSame(self, self.call());
    }

    @Test
    public <R> void returnAnonymousInnerClassFromLambda() throws Exception {
        Fn0<? extends Callable<String>> returnsCallable = λ(new Callable<String>() {
            public String call() throws Exception {
                return "hello";
            }
        });
        Callable<String> callable = returnsCallable.call();
        assertEquals("hello", callable.call());
        assertNotSame(callable, returnsCallable.call());
    }

    @NewLambda
    static Runnable runnable(Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    @Test
    public void createSingleMethodInterfaceUsingNewLambda() throws Exception {
        String hello = "";
        Runnable runnable = runnable(hello = "hello");
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
        Runnable runnable = delegate(hello = "hello");
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
    public void createSingleMethodInterfaceTakingTwoArgumentsAndReturningPrimitiveUsingGenericDelegate()
            throws Exception {
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
        SingleAbstractMethodNoArgumentsClass m = delegate("hello");
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

        TimerTask t = delegate(x = 1);
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
        Callable<?> callable = λ(string = "hello").as(Callable.class);
        assertEquals("hello", callable.call());
        assertEquals("hello", string);
    }

    @Test
    public void lambdaAsRunnable() throws Exception {
        String string = "";
        Thread thread = new Thread(λ(string = "hello").as(Runnable.class));
        assertEquals("", string);
        thread.start();
        thread.join();
        assertEquals("hello", string);
    }

    @LambdaParameter
    static KeyEvent ke;

    @Test
    public void lambdaAsKeyListener() throws Exception {
        List<KeyEvent> events = new ArrayList<KeyEvent>();
        KeyListener keyListener = λ(ke, events.add(ke)).as(KeyListener.class);
        KeyEvent event = new KeyEvent(new JButton(), 0, 0, 0, 0, (char) 0);

        keyListener.keyPressed(event);
        assertEquals(1, events.size());
    }

    @Test
    public void lambdaAsKeyListenerWithRegex() throws Exception {
        List<KeyEvent> events = new ArrayList<KeyEvent>();
        KeyListener keyListener = λ(ke, events.add(ke)).as(KeyListener.class, ".*Typed");
        KeyEvent event = new KeyEvent(new JButton(), 0, 0, 0, 0, (char) 0);

        keyListener.keyPressed(event);
        assertTrue(events.isEmpty());
        keyListener.keyReleased(event);
        assertTrue(events.isEmpty());
        keyListener.keyTyped(event);
        assertEquals(1, events.size());
    }

    @Test
    public void lambdaAsKeyListenerWithExactMatchAndMatchingParameterType() throws Exception {
        List<KeyEvent> events = new ArrayList<KeyEvent>();
        KeyListener keyListener = λ(ke, events.add(ke)).as(KeyListener.class, "keyPressed", EventObject.class);
        KeyEvent event = new KeyEvent(new JButton(), 0, 0, 0, 0, (char) 0);

        keyListener.keyPressed(event);
        assertEquals(1, events.size());
        keyListener.keyReleased(event);
        assertEquals(1, events.size());
        keyListener.keyPressed(null);
        assertEquals(2, events.size());
    }

    @Test
    public void lambdaAsKeyListenerWithExactMatchAndNonMatchingParameterType() throws Exception {
        List<KeyEvent> events = new ArrayList<KeyEvent>();
        KeyListener keyListener = λ(ke, events.add(ke)).as(KeyListener.class, "keyPressed", ActionEvent.class);
        KeyEvent event = new KeyEvent(new JButton(), 0, 0, 0, 0, (char) 0);

        keyListener.keyPressed(event);
        assertTrue(events.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void lambdaAsInterfaceWithExactMatchAndPrimitiveParameterTypes() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("world");
        List<String> proxy = λ(idx, s, list.set(idx, s)).as(List.class, "set", Integer.class, String.class);
        proxy.set(0, "hello");
        assertEquals("hello", list.get(0));
        proxy.clear();
        assertFalse(list.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void lambdaAsInterfaceWithExactMatchAndNullArgumentPassesFilter() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("world");
        List<String> proxy = λ(idx, s, list.set(idx, s)).as(List.class, "set", Integer.class, String.class);
        proxy.set(0, null);
        assertNull(list.get(0));
    }

    @Test
    public void lambdaInConstructor() throws Exception {
        class ConstructorClass {
            public String fromConstructor;
            {
                λ(fromConstructor = "hello").call();
            }
        }
        assertEquals("hello", new ConstructorClass().fromConstructor);
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
        assertEquals(getClass().getMethod("reflectionOnLambda"), addToIClass.getEnclosingMethod());

        assertEquals(1, addToIClass.getDeclaredMethods().length);
        assertEquals(1, addToIClass.getDeclaredFields().length);

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
    public void fn1OfFn0FunctionComposition() throws Exception {
        Fn1<Object, String> toString = λ(obj, obj.toString());
        Fn0<Integer> ten = λ(10);
        assertEquals(10, (int) ten.call());

        Fn0<String> tenToString = toString.compose(ten);
        assertEquals("10", tenToString.call());
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

    @Test
    public void complementOfFn() throws Exception {
        Fn0<Boolean> truth = λ(true);
        assertTrue(truth.call());
        assertFalse(truth.complement().call());

        Fn0<Object> nullFn = λ(null);
        assertNull(nullFn.call());
        assertTrue(nullFn.complement().call());

        Fn1<Integer, Boolean> isOdd = λ(n, n % 2 == 1);
        assertTrue(isOdd.call(1));
        assertFalse(isOdd.complement().call(1));

        Fn2<Integer, Integer, Boolean> isSumOdd = λ(n, m, (Boolean) ((m + n) % 2 == 1));
        assertTrue(isSumOdd.call(1, 2));
        assertFalse(isSumOdd.complement().call(1, 2));

        Fn3<Integer, Integer, Integer, Boolean> isSumEven = λ(n, m, i, (m + n + i) % 2 == 0);
        assertTrue(isSumEven.call(1, 2, 1));
        assertFalse(isSumEven.complement().call(1, 2, 1));
    }

    @Test
    public void comparsionsInZeroArgumentLambdas() throws Exception {
        assertTrue(λ(true).call());
        assertFalse(λ(false).call());

        String s = null;
        assertTrue(λ(s == null).call());
        s = "";
        assertTrue(λ(s != null).call());

        assertTrue(λ(1 != 2).call());

        int i = 5;
        assertTrue(λ(i > 0).call());
        assertFalse(λ(i < 0).call());
        assertTrue(λ(i / 5 == 1).call());

        assertTrue(λ(i == 5).call());
        assertFalse(λ(i == 2).call());
    }

    @Test
    public void comparsiosWithMutlipleLables() throws Exception {
        int p = 2;
        Fn1ItoB notDivisbleBy = λ(n, n <= p || (n % p) != 0);
        assertTrue(notDivisbleBy.call(1));
        assertTrue(notDivisbleBy.call(2));
        assertTrue(notDivisbleBy.call(3));
        assertFalse(notDivisbleBy.call(4));
    }

    @SuppressWarnings("rawtypes")
    @LambdaParameter
    static List l;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
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