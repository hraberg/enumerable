package lambda.enumerable.mixin;


public class EnumerableExampleMixin {
	public static void main(String[] args) {
		// List<String> strings = asList("malaysia", "thailand", "india",
		// "people's republic of china");
		//
		// List<String> upperCase = strings.collect(λ(s, s.toUpperCase()));
		// out.println(upperCase);
		//
		// strings.each(λ(s, out.printf("Country: %s\n", s)));
		//
		// Map<String, Integer> map = new HashMap<String, Integer>();
		// strings.eachWithIndex(λ(s, idx, map.put(s, idx)));
		// out.println(map);
		//
		// StringBuffer hello = new StringBuffer("");
		// List<String> twoStrings = strings.collect(λ(s,
		// hello.append(s).toString()));
		// out.println(twoStrings);
		// out.println(hello);
		//
		// List<Integer> ints = asList(5, 1, 8, 4, 6, 3, 10, 2, 7, 9);
		// out.println(ints);
		//
		// List<Integer> squares = ints.collect(λ(n, n * n));
		// out.println(squares);
		//
		// List<Integer> sortedDescending = ints.sort(λ(n, m, m - n));
		// out.println(sortedDescending);
		//
		// List<String> sortedAscending = strings.sortBy(λ(s, s.length()));
		// out.println(sortedAscending);
		//
		// int two = 2;
		// List<Integer> odd = ints.select(λ(n, n % two == 1));
		// out.println(odd);
		//
		// List<Double> squareRoots = ints.collect(λ(n, sqrt(n)));
		// out.println(squareRoots);
		//
		// List<String> hellos = ints.collect(λ(n, "hello"));
		// out.println(hellos);
		//
		// List<Integer> smallerThanFive = ints.reject(λ(n, n < 5));
		// out.println(smallerThanFive);
		//
		// int largerThanFive = ints.detect(λ(n, n > 5));
		// out.println(largerThanFive);
		//
		// int defaultValue = ints.detect(λ(n, n > 10), ifNone(-1));
		// out.println(defaultValue);
		//
		// boolean hasSmallerThanFive = ints.any(λ(n, n < 5));
		// out.println(hasSmallerThanFive);
		//
		// boolean allSmallerThanEleven = ints.all(λ(n, n < 11));
		// out.println(allSmallerThanEleven);
		//
		// int divisableByThree = ints.count(λ(n, n % 3 == 0));
		// out.println(divisableByThree);
		//
		// List<List<Integer>> partitioned = ints.partition(λ(n, n % 2 == 0));
		// out.println(partitioned);
		//
		// int sum = ints.inject(0, into(λ(n, m, n + m)));
		// out.println(sum);
		//
		// int factorial = ints.inject(1, into(λ(n, m, n * m)));
		// out.println(factorial);
		//
		// int factorialNoMemo = ints.inject(into(λ(n, m, n * m)));
		// out.println(factorialNoMemo);
		//
		// String longest = strings.inject(into(λ(s, t, s.length() > t.length()
		// ? s : t)));
		// out.println(longest);
		//
		// String text = ints.inject("", into(λ(s, n, s.length() > 0 ? s + ", "
		// + n : s + n)));
		// out.println(text);
		//
		// List<Integer> moreInts = asList(-1, 0, 1);
		//
		// Fn1<Integer, Integer> abs = λ(n, abs(n));
		// List<Integer> absolutes = moreInts.collect(abs);
		// out.println(absolutes);
		//
		// List<Integer> oddTimesSum = ints.collect(λ(n, n * sum)).select(λ(n, n
		// % 2 == 1));
		// out.println(oddTimesSum);
		//
		// int x = 3;
		// Fn1<Integer, Integer> closure = λ(n, x + n);
		// out.println(ints.collect(closure));
		//
		// x = 6;
		// out.println(ints.collect(closure));
		//
		// λ(n, x = n).call(2);
		// out.println(x);
		//
		// out.println(λ(n, n / 0).call(3));
	}
}