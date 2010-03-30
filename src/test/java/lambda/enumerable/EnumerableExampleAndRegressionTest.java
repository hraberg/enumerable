package lambda.enumerable;

import static java.lang.Math.*;
import static java.lang.System.*;
import static java.util.Arrays.*;
import static lambda.enumerable.Enumerable.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lambda.Fn1;
import lambda.Lambda;

import org.junit.Test;

public class EnumerableExampleAndRegressionTest {
	public static void main(String[] args) {
		new EnumerableExampleAndRegressionTest().regressionTest();
	}

	@Test
	public void regressionTest() {
		List<String> strings = asList("malaysia", "thailand", "india", "people's republic of china");

		each(strings, λ(s, out.printf("Country: %s\n", s)));

		Map<String, Integer> map = new HashMap<String, Integer>();
		eachWithIndex(strings, λ(s, idx, map.put(s, idx)));
		out.println(map);

		List<String> upperCase = collect(strings, λ(s, s.toUpperCase()));
		out.println(upperCase);

		StringBuffer hello = new StringBuffer("");
		List<String> twoStrings = collect(strings, λ(s, hello.append(s).toString()));
		out.println(twoStrings);
		out.println(hello);

		List<Integer> ints = asList(5, 1, 8, 4, 6, 3, 10, 2, 7, 9);
		out.println(ints);

		List<Integer> squares = collect(ints, λ(n, n * n));
		out.println(squares);

		List<Integer> sortedDescending = sort(ints, λ(n, m, m - n));
		out.println(sortedDescending);

		List<String> sortedAscending = sortBy(strings, λ(s, s.length()));
		out.println(sortedAscending);

		int two = 2;
		List<Integer> odd = select(ints, λ(n, n % two == 1));
		out.println(odd);

		List<Double> squareRoots = collect(ints, λ(n, sqrt(n)));
		out.println(squareRoots);

		List<String> hellos = collect(ints, λ(n, "hello"));
		out.println(hellos);

		List<Integer> smallerThanFive = reject(ints, λ(n, n < 5));
		out.println(smallerThanFive);

		int largerThanFive = detect(ints, λ(n, n > 5));
		out.println(largerThanFive);

		int defaultValue = detect(ints, λ(n, n > 10), ifNone(-1));
		out.println(defaultValue);

		boolean hasSmallerThanFive = any(ints, λ(n, n < 5));
		out.println(hasSmallerThanFive);

		boolean allSmallerThanEleven = all(ints, λ(n, n < 11));
		out.println(allSmallerThanEleven);

		int divisableByThree = count(ints, λ(n, n % 3 == 0));
		out.println(divisableByThree);

		List<List<Integer>> partitioned = partition(ints, λ(n, n % 2 == 0));
		out.println(partitioned);

		int sum = inject(ints, 0, into(λ(n, m, n + m)));
		out.println(sum);

		int factorial = inject(ints, 1, into(λ(n, m, n * m)));
		out.println(factorial);

		int factorialNoMemo = inject(ints, into(λ(n, m, n * m)));
		out.println(factorialNoMemo);

		String longest = inject(strings, into(λ(s, t, s.length() > t.length() ? s : t)));
		out.println(longest);

		String text = inject(ints, "", into(λ(s, n, s.length() > 0 ? s + ", " + n : s + n)));
		out.println(text);

		Fn1<Integer, Integer> add2 = Lambda.partial(λ(n, m, n + m), 2);
		out.println(add2.call(5));
		out.println(add2.call(-5));

		List<Integer> moreInts = asList(-1, 0, 1);

		Fn1<Integer, Integer> abs = λ(n, abs(n));
		List<Integer> absolutes = collect(moreInts, abs);
		out.println(absolutes);

		List<Integer> oddTimesSum = select(collect(ints, λ(n, n * sum)), λ(n, n % 2 == 1));
		out.println(oddTimesSum);

		int x = 3;
		Fn1<Integer, Integer> closure = λ(n, x + n);
		out.println(collect(ints, closure));

		x = 6;
		out.println(collect(ints, closure));

		λ(n, x = n).call(2);
		out.println(x);
	}
}