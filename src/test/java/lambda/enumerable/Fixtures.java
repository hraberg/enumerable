package lambda.enumerable;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.List;

public class Fixtures {
	public static List<Integer> oneToTen = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	public static <E> List<E> list(E... elements) {
		return new ArrayList<E>(asList(elements));
	}

	public static <E> List<E> list(Class<E> type) {
		return new ArrayList<E>();
	}

	public static List<Integer> range(int from, int to) {
		List<Integer> range = list();
		for (int i = from; i <= to; i++)
			range.add(i);
		return range;
	}
}
