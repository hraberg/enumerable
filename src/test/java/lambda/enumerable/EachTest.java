package lambda.enumerable;

import static lambda.Fixtures.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class EachTest {
	@Test
	public void callsBlockOnceForEachElement() throws Exception {
		List<Integer> actual = list();
		each(oneToTen, λ(n, actual.add(n)));
		assertEquals(oneToTen, actual);
	}

	@Test
	public void emptyCollectionDoesNotCallBlock() throws Exception {
		List<Integer> actual = list();
		each(list(int.class), λ(n, actual.add(n)));
		assertTrue(actual.isEmpty());
	}

	@Test
	public void returnsLastValue() throws Exception {
		assertEquals(10, (int) each(oneToTen, λ(n, n)));
	}

	@Test
	public void returnsNullForEmptyCollection() throws Exception {
		assertNull(each(list(Object.class), λ(o, "hello")));
	}

	@Test
	public void indexIsZeroBased() throws Exception {
		List<Integer> actual = list();
		eachWithIndex(oneToTen, λ(n, idx, actual.add(idx + 1)));
		assertEquals(oneToTen, actual);
	}
}
