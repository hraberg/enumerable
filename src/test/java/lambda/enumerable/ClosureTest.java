package lambda.enumerable;

import static lambda.enumerable.Enumerable.*;
import static lambda.enumerable.Fixtures.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ClosureTest {
	@Test(expected = ArithmeticException.class)
	public void exceptionInBlockPropagetsOut() throws Exception {
		each(oneToTen, λ(n, n / 0));
	}

	@Test
	public void canCloseOverLocalPrimitiveVarible() throws Exception {
		int s = 0;
		each(oneToTen, λ(n, s += n));
		assertEquals(55, s);
	}

	@Test
	public void canCloseOverLocalReferenceVarible() throws Exception {
		String s = "hello";
		each(list(1), λ(n, s += " world"));
		assertEquals("hello world", s);
	}

	@Test
	public void canCloseOverThis() throws Exception {
		assertSame(this, each(oneToTen, λ(n, this)));
	}

	@Test
	public void canCallInstanceMethodOnThis() throws Exception {
		assertEquals(hello(), each(oneToTen, λ(n, hello())));
	}

	public String hello() {
		return "hello";
	}

	static int staticInt = 0;

	@Test
	public void canCloseOverPrimitiveStaticField() throws Exception {
		each(oneToTen, λ(n, staticInt += n));
		assertEquals(55, staticInt);
	}

	static String staticString = "world";

	@Test
	public void canCloseOverStaticField() throws Exception {
		each(list(1), λ(n, staticString = "hello " + staticString));
		assertEquals("hello world", staticString);
	}

	int instanceInt = 0;

	@Test
	public void canCloseOverPrimitveInstanceField() throws Exception {
		each(oneToTen, λ(n, instanceInt += n));
		assertEquals(55, instanceInt);
	}

	static String instanceString = "";

	@Test
	public void canCloseOverInstanceField() throws Exception {
		each(list(1), λ(n, staticString = instanceString = "hello world"));
		assertEquals("hello world", instanceString);
	}
}
