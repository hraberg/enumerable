package lambda.enumerable;

import static java.util.Arrays.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class EachTest {
	List<Integer> list = asList(1, 2, 3, 4, 5);

	@Test
	public void eachCallsBlockOnceForEachElement() throws Exception {
		List<Integer> actual = new ArrayList<Integer>();
		List<Integer> expected = new ArrayList<Integer>(list);
		each(list, λ(n, actual.add(n)));
		assertEquals(expected, actual);
	}
}
