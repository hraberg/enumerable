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
	public void canCloseOverLocalPrimitiveVarible() throws Exception {
		int i = 0;
		λ(n, i += n).call(10);
		assertEquals(10, i);
	}

	@Test
	public void canCloseOverLocalReferenceVarible() throws Exception {
		String hello = "hello";
		λ(s, hello += s).call(" world");
		assertEquals("hello world", hello);
	}

	@Test
	public void canCloseOverThis() throws Exception {
		assertSame(this, λ(n, this).call(null));
	}

	@Test
	public void canCallInstanceMethodOnThis() throws Exception {
		assertEquals(hello(), λ(n, hello()).call(null));
	}

	public String hello() {
		return "hello";
	}

	static int staticInt = 0;

	@Test
	public void canCloseOverPrimitiveStaticField() throws Exception {
		λ(n, staticInt += n).call(10);
		assertEquals(10, staticInt);
	}

	static String staticString = "world";

	@Test
	public void canCloseOverStaticField() throws Exception {
		λ(s, staticString = s + staticString).call("hello ");
		assertEquals("hello world", staticString);
	}

	double instanceDouble = 0;

	@Test
	public void canCloseOverPrimitveInstanceField() throws Exception {
		λ(d, instanceDouble += d).call(3.14);
		assertEquals(3.14, instanceDouble, 0.0);
	}

	static String instanceString = "";

	@Test
	public void canCloseOverInstanceField() throws Exception {
		λ(s, instanceString = s).call("hello world");
		assertEquals("hello world", instanceString);
	}
}
