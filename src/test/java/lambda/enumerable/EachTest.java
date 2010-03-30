package lambda.enumerable;

import static java.util.Arrays.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class EachTest {

	// @Test
	// public void eachCallsBlockOnceForEachElement() throws Exception {
	// List<Integer> actual = new ArrayList<Integer>();
	// List<Integer> expected = new ArrayList<Integer>(list);
	// each(list, λ(n, actual.add(n)));
	// assertEquals(expected, actual);
	// }

	public static void main(String... args) throws Exception {
		method();
	}

	public static void method() {
		List<Integer> list = asList(1, 2, 3, 4, 5);
		List<Integer> actual = new ArrayList<Integer>();
		List<Integer> expected = new ArrayList<Integer>(list);
		each(list, λ(n, actual.add(n)));
		assertEquals(expected, actual);
		System.out.println(actual);
	}
}
