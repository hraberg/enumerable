package lambda;

import static java.lang.Math.*;
import static java.util.Arrays.*;
import static lambda.Lambda.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Test;

public class ClosureTest {
	@Test(expected = ArithmeticException.class)
	public void exceptionInBlockPropagetsOut() throws Exception {
		λ(n, n / 0).call(0);
	}

	@Test
	public void closeOverLocalPrimitiveVarible() throws Exception {
		int i = 0;
		λ(n, i += n).call(10);
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
		λ(s, hello = new String[] { "world" }).call(" world");
		assertEquals("world", hello[0]);
		assertNotSame(original, hello);
	}

	@Test
	public void closeOverThis() throws Exception {
		assertSame(this, λ(n, this).call(null));
	}

	@Test
	public void closeOverMethodParameter() throws Exception {
		methodCall("Hello");
	}

	void methodCall(String string) {
		assertSame(string, λ(n, string).call(null));
		λ(n, string = "world").call(null);
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

	private void otherMethod(Fn1<Integer, Integer> add, int x) {
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
	public void callInstanceMethodOnThis() throws Exception {
		assertEquals(hello(), λ(n, hello()).call(null));
	}

	public String hello() {
		return "hello";
	}

	@Test
	public void callStaticMethod() throws Exception {
		assertEquals(ClosureTest.world(), λ(n, ClosureTest.world()).call(null));
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
		assertEquals(string.toUpperCase(), λ(n, string.toUpperCase()).call(null));
	}

	@Test
	public void canAccessMethodArgumentInClosureFirst() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		printOnStream(new PrintStream(out));
		assertEquals("word: hello\nword: world\n", out.toString());
	}

	public void printOnStream(PrintStream out) {
		List<String> strings = asList("hello", "world");
		each(strings, λ(s, out.printf("word: %s\n", s)));
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
}
