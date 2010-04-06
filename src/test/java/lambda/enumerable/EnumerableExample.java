package lambda.enumerable;

import static java.lang.Math.*;
import static java.util.Arrays.*;
import static lambda.Lambda.*;
import static lambda.Lambda.Primitives.*;
import static lambda.enumerable.Enumerable.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lambda.Fn1;
import lambda.Lambda;
import lambda.primitives.Fn1ItoI;

public class EnumerableExample {
    public void example(PrintStream out) {
        /*
         * While λ looks good, it is cumbersome to insert. There are three
         * (ignoring arity versions) static methods in Lambda that all are
         * marked with @NewLambda and all do the same thing: λ, fn and lambda.
         */
        assert Character.isJavaIdentifierStart('λ');

        List<String> strings = asList("malaysia", "thailand", "india", "people's republic of china");

        /*
         * each will evaluate the block for every element in the array. The
         * block has to be an expression, printf returns a PrintStream which we
         * ignore.
         */
        each(strings, λ(s, out.printf("Country: %s\n", s)));

        /*
         * eachWithIndex binds the second parameter to the current index, we use
         * the int @LambdaParameter idx here. The local variable map is wrapped
         * in an array to enable closure.
         */
        Map<String, Integer> map = new HashMap<String, Integer>();
        eachWithIndex(strings, λ(s, idx, map.put(s, idx)));
        out.println(map);

        /*
         * Basic collect expression, s is a String @LambdaParameter.
         */
        List<String> upperCase = collect(strings, λ(s, s.toUpperCase()));
        out.println(upperCase);

        /*
         * Contrived example of both collecting and modifying a local variable
         * at the same time. StringBuffer#append returns the buffer, so each
         * element of twoStrings will contain the buffer up to that point.
         */
        StringBuffer hello = new StringBuffer("");
        List<String> twoStrings = collect(strings, λ(s, hello.append(s).toString()));
        out.println(twoStrings);
        out.println(hello);

        List<Integer> ints = asList(5, 1, 8, 4, 6, 3, 10, 2, 7, 9);
        out.println(ints);

        /*
         * Squares a list of integers. The @LambdaParameter n is accessed a
         * total of 3 times during block construction. The first time is to
         * define a block argument, the two next times to access the first
         * parameter to Fn1#call(arg) While actually executing n is not accessed
         * at all, the arg parameter of the new Fn1 is.
         */
        List<Integer> squares = collect(ints, λ(n, n * n));
        out.println(squares);

        /*
         * Special case of wrapping a block in a java interface
         * (java.util.Comparator). For a general, proxy based solution, see the
         * Lambda.as methods.
         */
        List<Integer> sortedDescending = sort(ints, λ(n, m, m - n));
        out.println(sortedDescending);

        /*
         * Sorts the collection based on the natural order of the result of an
         * expression.
         */
        List<String> sortedAscending = sortBy(strings, λ(s, s.length()));
        out.println(sortedAscending);

        /*
         * Closing over the variable two.
         */
        int two = 2;
        List<Integer> odd = select(ints, λ(n, n % two == 1));
        out.println(odd);

        /*
         * Example of different primitives than int. Demonstrates call to static
         * method Math.sqrt.
         */
        List<Double> squareRoots = collect(ints, Lambda.λ(n, sqrt(n)));
        out.println(squareRoots);

        /*
         * This ignores the input collection, returning "hello" for each
         * element. Collect transforms, so the input type doesn't have to match
         * the output type.
         */
        List<String> hellos = collect(ints, λ(n, "hello"));
        out.println(hellos);

        /*
         * Rejects elements based on a boolean expression.
         */
        List<Integer> smallerThanFive = reject(ints, λ(n, n >= 5));
        out.println(smallerThanFive);

        /*
         * Detect and return the first element matching a boolean expression.
         */
        int largerThanFive = detect(ints, λ(n, n > 5));
        out.println(largerThanFive);

        /*
         * Detect using ifNone, which returns a constant default value.
         */
        int defaultValue = detect(ints, λ(n, n > 10), ifNone(-1));
        out.println(defaultValue);

        /*
         * Any returns true if any element matches the boolean expression.
         */
        boolean hasSmallerThanFive = any(ints, λ(n, n < 5));
        out.println(hasSmallerThanFive);

        /*
         * All returns true if all elements match the boolean expression.
         */
        boolean allSmallerThanEleven = all(ints, λ(n, n < 11));
        out.println(allSmallerThanEleven);

        /*
         * Count is the same as select(..).size()
         */
        int divisableByThree = select(ints, λ(n, n % 3 == 0)).size();
        out.println(divisableByThree);

        /*
         * Partition is select and reject rolled into one, returning a list with
         * two collections, [selected, rejected].
         */
        List<List<Integer>> partitioned = partition(ints, λ(n, n % 2 == 0));
        out.println(partitioned);

        /*
         * Inject accumulates a value over a collection. n is the accumulator
         * and 0 is the initial value. n is set to the result of the expression
         * after each evaluation. m is the current element.
         */
        int sum = inject(ints, 0, λ(n, m, n + m));
        out.println(sum);

        /*
         * Inject without an initial value,, requires the collection to be at
         * least two elements.
         */
        int factorial = inject(ints, λ(n, m, n * m));
        out.println(factorial);

        /*
         * The ternary operator can be used to evaluate more expressions in
         * certain cases. You can chain more than one, sacrificing readability.
         */
        String longest = inject(strings, λ(s, t, s.length() > t.length() ? s : t));
        out.println(longest);

        /*
         * A more complex example of using the ternary operator to evaluate
         * different expressions based on the input.
         */
        String text = inject(ints, "", λ(s, n, s.length() > 0 ? s + ", " + n : s + n));
        out.println(text);

        /*
         * Partial application, very limited support as the blocks of this
         * library are primarily meant to support the Enumerable operations.
         * Also shows that the blocks are compiled into normal Java classes, an
         * instance of Fn1 here. These can be passed around like any other
         * instance.
         */
        Fn1<Integer, Integer> add2 = λ(n, m, n + m).partial(2);
        out.println(add2.call(5));
        out.println(add2.call(-5));

        /*
         * You can use function composition to chain functions like this.
         */
        Fn1<Integer, Integer> negatedAdd2 = λ(n, -n).compose(add2);
        out.println(negatedAdd2.call(4));

        /*
         * Once defined, lambdas can call themselves recursively.
         */
        fib = fn(n, n <= 1 ? n : fib.call(n - 1) + fib.call(n - 2));
        out.println(fib.call(10));

        List<Integer> moreInts = asList(-1, 0, 1);

        /*
         * More demonstration of calling different static methods, Math.abs in
         * this case.
         */
        Fn1<Integer, Integer> abs = λ(n, abs(n));
        List<Integer> absolutes = collect(moreInts, abs);
        out.println(absolutes);

        /*
         * You cannot nest lambda constructs as the weaver doesn't support it,
         * but you can use more than one in the same larger expression like
         * this.
         */
        List<Integer> oddTimesSum = select(collect(ints, λ(n, n * sum)), λ(n, n % 2 == 1));
        out.println(oddTimesSum);

        /*
         * Another example of closure, the Fn1 instance can still read and write
         * the local variable x if we would to pass it along to another scope.
         */
        int x = 3;
        Fn1<Integer, Integer> closure = λ(n, x + n);
        out.println(collect(ints, closure));

        /*
         * Shows that local variable changes are visible inside the closure.
         */
        x = 6;
        out.println(collect(ints, closure));

        /*
         * And finally, setting the local variable from inside a new block.
         */
        λ(n, x = n).call(2);
        out.println(x);
    }

    Fn1ItoI fib;

    public static void main(String[] args) {
        new EnumerableExample().example(System.out);
    }
}