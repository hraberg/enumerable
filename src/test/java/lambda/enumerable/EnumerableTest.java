package lambda.enumerable;

import static java.util.Arrays.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class EnumerableTest {
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
	public void eachReturnsLastValue() throws Exception {
		assertEquals(10, (int) each(oneToTen, λ(n, n)));
	}

	@Test
	public void eachReturnsNullForEmptyCollection() throws Exception {
		assertNull(each(list(Object.class), λ(o, "hello")));
	}

	@Test
	public void eachWithIndexIsZeroBased() throws Exception {
		List<Integer> actual = list();
		eachWithIndex(oneToTen, λ(n, idx, actual.add(idx + 1)));
		assertEquals(oneToTen, actual);
	}

	@Test
	public void collectElements() throws Exception {
		assertEquals(list(2, 4, 6, 8, 10), collect(oneToFive, λ(n, n * 2)));
	}

	@Test
	public void collectElementsToDifferentType() throws Exception {
		assertEquals(list("#1", "#2", "#3", "#4", "#5"), collect(oneToFive, λ(n, "#" + n)));
	}

	@Test
	public void selectMatchingElements() throws Exception {
		List<Integer> even = list(2, 4, 6, 8, 10);
		assertEquals(even, select(oneToTen, λ(n, n % 2 == 0)));
	}

	@Test
	public void rejectMatchingElements() throws Exception {
		List<Integer> odd = list(1, 3, 5, 7, 9);
		assertEquals(odd, reject(oneToTen, λ(n, n % 2 == 0)));
	}

	@Test
	public void detectFirstMatchingElement() throws Exception {
		assertEquals(2, (int) detect(oneToTen, λ(n, n % 2 == 0)));
	}

	@Test
	public void detectReturnsIfNoneValueIfNoMatch() throws Exception {
		assertEquals(-1, (int) detect(oneToTen, λ(n, n < 0), ifNone(-1)));
	}

	@Test
	public void partitionListIntoTwoBasedOnPreducate() throws Exception {
		List<Integer> even = list(2, 4, 6, 8, 10);
		List<Integer> odd = list(1, 3, 5, 7, 9);

		List<List<Integer>> partition = partition(oneToTen, λ(n, n % 2 == 0));

		assertEquals(even, partition.get(0));
		assertEquals(odd, partition.get(1));
	}

	@Test
	public void anyMarchingPredicate() throws Exception {
		assertTrue(any(oneToTen, λ(n, n > 5)));
	}

	@Test
	public void anyNotMatchingPredicate() throws Exception {
		assertFalse(any(oneToTen, λ(n, n > 10)));
	}

	@Test
	public void allMatchingPredicate() throws Exception {
		assertTrue(all(oneToTen, λ(n, n > 0)));
	}

	@Test
	public void allNotMatchingPredicate() throws Exception {
		assertFalse(all(oneToTen, λ(n, n > 1)));
	}

	@Test
	public void countMatchingPredicate() throws Exception {
		assertEquals(5, (int) count(oneToTen, λ(n, n > 5)));
	}

	@Test
	public void sortUsingBlock() throws Exception {
		assertEquals(list(5, 4, 3, 2, 1), sort(oneToFive, λ(n, m, m - n)));
	}

	@Test
	public void sortByUsingBlock() throws Exception {
		List<String> actual = sortBy(list("longer", "long"), λ(s, s.length()));
		assertEquals(list("long", "longer"), actual);
	}

	@Test
	public void injectUsingMemo() throws Exception {
		assertEquals(55, (int) inject(oneToTen, 0, into(λ(n, m, n + m))));
	}

	@Test
	public void injectWithoutMemo() throws Exception {
		assertEquals(3628800, (int) inject(oneToTen, into(λ(n, m, n * m))));
	}

	public static List<Integer> oneToTen = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
	public static List<Integer> oneToFive = oneToTen.subList(0, 5);

	public static <E> List<E> list(E... elements) {
		return new ArrayList<E>(asList(elements));
	}

	public static <E> List<E> list(Class<E> type) {
		return new ArrayList<E>();
	}
}
