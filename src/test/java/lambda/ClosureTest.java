package lambda;

import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

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

	static String instanceString = "";

	@Test
	public void accessingInstanceField() throws Exception {
		λ(s, instanceString = s).call("hello world");
		assertEquals("hello world", instanceString);
	}
}
