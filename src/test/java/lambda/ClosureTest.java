package lambda;

import static java.lang.Math.PI;
import static lambda.Lambda._;
import static lambda.Lambda.c;
import static lambda.Lambda.d;
import static lambda.Lambda.delegate;
import static lambda.Lambda.l;
import static lambda.Lambda.n;
import static lambda.Lambda.s;
import static lambda.Lambda.λ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.PrintStream;
import java.io.Serializable;

import org.junit.Test;

@SuppressWarnings("serial")
public class ClosureTest extends TestBase implements Serializable {
    @Test(expected = ArithmeticException.class)
    public void uncheckedExceptionInBlockPropagetsOut() throws Exception {
        λ(n, n / 0).call(0);
    }

    @Test(expected = ClassNotFoundException.class)
    public void checkedExceptionInBlockPropagetsOut() throws Exception {
        Fn1<String, ? extends Class<?>> classForName = λ(s, Class.forName(s));
        assertEquals(String.class, classForName.call(String.class.getName()));
        classForName.call("class.not.Found");
    }

    @Test
    public void closeOverLocalPrimitiveVarible() throws Exception {
        int i = 0;
        λ(n, i += n).call(10);
        assertEquals(10, i);
    }

    @Test
    public void closeOverLocalDoubleVaribleWithoutModification() throws Exception {
        double i = 0;
        assertEquals(3.14, λ(d, i + d).call(3.14), 0);
    }

    @Test
    public void closeOverLocalDoubleWithModification() throws Exception {
        double i = 0;
        λ(d, i = d).call(3.14);
        assertEquals(3.14, i, 0);
    }

    @Test
    public void closeOverLocalLongVaribleWithoutModification() throws Exception {
        long i = 0;
        assertEquals(10, (long) λ(l, i + l).call(10L));
    }

    @Test
    public void closeOverLocalLongWithModification() throws Exception {
        long i = 0;
        λ(l, i += l).call(10L);
        assertEquals(10, i);
    }

    @Test
    public void closeOverLocalReferenceVarible() throws Exception {
        String hello = "hello";
        λ(s, hello += s).call(" world");
        assertEquals("hello world", hello);
        assertEquals(String.class, hello.getClass());
    }

    @Test
    public void closeOverLocalArrayVarible() throws Exception {
        String[] hello = new String[] { "hello" };
        String[] original = hello;
        λ(s, hello[0] += s).call(" world");
        assertEquals("hello world", hello[0]);
        assertSame(original, hello);
    }

    @Test
    public void closeOverLocalArrayVaribleWhichCanBeSet() throws Exception {
        String[] hello = new String[] { "hello" };
        String[] original = hello;
        λ(s, hello = new String[] { s }).call("world");
        assertEquals("world", hello[0]);
        assertNotSame(original, hello);
    }

    @Test
    public void closeOverLocalFinalPrimitiveVarible() throws Exception {
        final int i = 10;
        assertEquals(20, (int) λ(n, i + n).call(10));
    }

    @Test
    public void closeOverLocalFinalReferenceVarible() throws Exception {
        final String hello = "hello";
        assertEquals("hello world", λ(s, hello + s).call(" world"));
    }

    @Test
    public void closeOverLocalFinalArrayVarible() throws Exception {
        final String[] hello = new String[] { "hello" };
        λ(s, hello[0] += s).call(" world");
        assertEquals("hello world", hello[0]);
    }

    // This test fails when the CheckClassAdapter is used with 'duplicate class
    // definition for name: lambda/ClosureTest'

    // @Test
    // public void closeOverThis() throws Exception {
    // assertSame(this, λ(_, this).call());
    // }

    @Test
    public void closeOverMethodParameter() throws Exception {
        methodCall("Hello");
    }

    void methodCall(String string) {
        assertSame(string, λ(_, string).call());
        λ(_, string = "world").call();
        assertEquals("world", string);
    }

    @Test
    public void closingOverLocalVaribleDoesNotChangeIt() throws Exception {
        int i = 0;
        λ(n, i += n);
        assertEquals(0, i);
    }

    @Test
    public void closeOverLocalVaribleAndRunInDifferentMethod() throws Exception {
        int i = 0;
        otherMethod(λ(n, i += n), 10);
        assertEquals(10, i);
    }

    @Test
    public void closingOverLocalPrimitveVaribleCanStillIncrementOutsideClosure() throws Exception {
        int i = 0;
        Fn1<Integer, Integer> add = λ(n, i += n);
        i += 10;
        assertEquals(10, i);
        add.call((int) Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE + 10, i);
        i += Integer.MAX_VALUE / 2;
        assertEquals(Short.MAX_VALUE + 10 + Integer.MAX_VALUE / 2, i);
    }

    @Test
    public void closingOverLocalReferenceVaribleWorksLikeNormalOutsideClosure() throws Exception {
        String string = "hello";
        Fn1<String, String> toUpCase = λ(s, s.toUpperCase());
        string += " world";
        assertEquals("hello world", string);
        string = toUpCase.call(string);
        assertEquals("HELLO WORLD", string);
        assertEquals(String.class, string.getClass());
    }

    void otherMethod(Fn1<Integer, Integer> add, int x) {
        add.call(x);
    }

    @Test
    public void closingOverLocalVaribleSeesChangesToLocalVariable() throws Exception {
        int i = 0;
        Fn1<Integer, Integer> plus = λ(n, i += n);
        plus.call(1);
        assertEquals(1, i);
        i++;
        assertEquals(2, i);
        plus.call(1);
        assertEquals(3, i);
    }

    @Test
    public void closingOverLocalVaribleAfterMethodReturn() throws Exception {
        Fn1<Integer, Integer> plus = methodReturn();
        assertEquals(2, (int) plus.call(1));
        assertEquals(3, (int) plus.call(1));
    }

    public Fn1<Integer, Integer> methodReturn() throws Exception {
        int i = 1;
        return λ(n, i += n);
    }

    @Test
    public void closeOverForLoopVariable() throws Exception {
        int sum = 0;
        for (int i = 1; i <= 10; i++)
            sum = λ(n, n + i).call(sum);
        assertEquals(55, sum);
    }

    @Test
    public void callInstanceMethodOnThis() throws Exception {
        assertEquals(hello(), λ(_, hello()).call());
    }

    public String hello() {
        return "hello";
    }

    @Test
    public void callStaticMethod() throws Exception {
        assertEquals(ClosureTest.world(), λ(_, ClosureTest.world()).call());
    }

    public static String world() {
        return "hello";
    }

    @Test
    public void callInstanceMethodOnArgument() throws Exception {
        assertEquals("HELLO", λ(s, s.toUpperCase()).call(hello()));
    }

    @Test
    public void callStaticMethodOnDifferentClass() throws Exception {
        assertTrue(λ(c, Character.isUpperCase(c)).call('C'));
    }

    @Test
    public void accessingEnclosingMethodArgument() throws Exception {
        instanceArgumentMethodCall("Hello");
    }

    void instanceArgumentMethodCall(String string) {
        assertEquals("Hello", string);
        assertEquals(string.toUpperCase(), λ(_, string.toUpperCase()).call());
    }

    @Test
    public void canAccessMethodArgumentInClosureFirst() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        printOnStream(new PrintStream(out));
        assertEquals("word: hello\n", out.toString());
    }

    public void printOnStream(PrintStream out) {
        λ(s, out.printf("word: %s\n", s)).call("hello");
    }

    @Test
    public void accessingEnclosingMethodPrimitiveArgument() throws Exception {
        primitiveArgumentMethodCall(10);
        primitiveArgumentIncMethodCall(10);
    }

    void primitiveArgumentMethodCall(int x) {
        assertEquals(10, x);
        assertEquals(x, (int) λ(n, x).call(0));
    }

    void primitiveArgumentIncMethodCall(int x) {
        λ(n, x++).call(0);
    }

    @Test
    public void returnSameArgument() throws Exception {
        String hello = hello();
        assertSame(hello, λ(s, s).call(hello));
    }

    static int staticInt = 0;

    @Test
    public void accessingPrimitiveStaticField() throws Exception {
        λ(n, staticInt += n).call(10);
        assertEquals(10, staticInt);
    }

    static String staticString = "world";

    @Test
    public void accessingStaticField() throws Exception {
        λ(s, staticString = s + staticString).call("hello ");
        assertEquals("hello world", staticString);
    }

    double instanceDouble = 0;

    @Test
    public void accessingPrimitveInstanceField() throws Exception {
        λ(d, instanceDouble += d).call(3.14);
        assertEquals(3.14, instanceDouble, 0.0);
    }

    String instanceString = "";

    @Test
    public void accessingInstanceField() throws Exception {
        λ(s, instanceString = s).call("hello world");
        assertEquals("hello world", instanceString);
    }

    @Test
    public void accessStaticFieldOnDifferentClass() throws Exception {
        assertEquals(PI, λ(d, PI).call(0.0), 0.0);
    }

    @Test
    public void serializingOfClosure() throws Exception {
        int x = 5;

        Fn1<Integer, Integer> inc = λ(n, x = n + x);
        assertEquals(10, (int) inc.call(5));
        assertEquals(10, x);

        byte[] bytes = serialze(inc);
        Fn1<Integer, Integer> deserializedInc = deserialize(bytes);
        assertNotSame(inc, deserializedInc);

        assertEquals(15, (int) deserializedInc.call(5));
        assertEquals(10, x);

        assertEquals(12, (int) inc.call(2));
        assertEquals(12, x);

        assertEquals(20, (int) deserializedInc.call(5));
    }

    int x = 5;

    @Test
    public void serializingWhenClosingOverThis() throws Exception {
        Fn1<Integer, Integer> inc = λ(n, this.x = n + x);
        assertEquals(10, (int) inc.call(5));
        assertEquals(10, x);

        byte[] bytes = serialze(inc);
        Fn1<Integer, Integer> deserializedInc = deserialize(bytes);
        assertNotSame(inc, deserializedInc);

        assertEquals(15, (int) deserializedInc.call(5));
        assertEquals(10, x);
    }

    @Test(expected = NotSerializableException.class)
    public void lambdaMustBeExplicitlySerializable() throws Exception {
        Runnable runnable = delegate(_, x = 1);
        runnable.run();
        assertEquals(1, x);
        serialze(runnable);
    }
}
