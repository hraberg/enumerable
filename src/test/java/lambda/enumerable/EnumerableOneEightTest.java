package lambda.enumerable;

import static java.util.Arrays.*;
import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import lambda.TestBase;
import lambda.annotation.LambdaParameter;
import lambda.enumerable.EnumerableArrays.ArrayIterable;
import lambda.enumerable.collection.EList;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class EnumerableOneEightTest extends TestBase {
    @Test
    public void callsBlockOnceForEachElement() throws Exception {
        List<Integer> actual = list();
        each(oneToTen, λ(n, actual.add(n)));
        assertEquals(oneToTen, actual);
    }

    @Test
    public void callsBlockOnceForEachKeyValuePairInMap() throws Exception {
        String result = "";
        each(stringsToInts, λ(s, n, result += n + ": " + s + "\n"));
        assertEquals("1: hello\n2: world\n", result);
    }

    @Test
    public void callsBlockOnceForEachKey() throws Exception {
        List<String> actual = list();
        eachKey(stringsToInts, λ(s, actual.add(s)));
        assertEquals(list("hello", "world"), actual);
    }

    @Test
    public void callsBlockOnceForEachValue() throws Exception {
        List<Integer> actual = list();
        eachValue(stringsToInts, λ(n, actual.add(n)));
        assertEquals(list(1, 2), actual);
    }

    @Test
    public void callsBlockOnceForEachLine() throws Exception {
        List<String> actual = list();
        eachLine("hello\nworld", λ(s, actual.add(s)));
        assertEquals(list("hello", "world"), actual);
    }

    @Test
    public void eachOnEmptyCollectionDoesNotCallBlock() throws Exception {
        List<Integer> actual = list();
        each(list(int.class), λ(n, actual.add(n)));
        assertTrue(actual.isEmpty());
    }

    @Test
    public void eachReturnsIterable() throws Exception {
        assertEquals(new EList(oneToTen), each(oneToTen, λ(n, n)));
    }

    @Test
    public void eachReturnsEmptyIterableForEmptyCollection() throws Exception {
        assertFalse(each(list(), λ(obj, "hello")).iterator().hasNext());
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
    public void selectMatchingMapEntries() throws Exception {
        List<Entry<String, Integer>> selected = select(stringsToInts, λ(s, n, n % 2 == 0));
        assertEquals(1, selected.size());
        assertEquals(2, (int) selected.get(0).getValue());
        assertEquals("world", selected.get(0).getKey());
    }

    @Test
    public void selectMatchingElements() throws Exception {
        List<Integer> even = list(2, 4, 6, 8, 10);
        assertEquals(even, select(oneToTen, λ(n, n % 2 == 0)));
    }

    @Test
    public void inclusiveRanges() throws Exception {
        assertEquals(oneToFive, collect(range(1, 5), λ(n, n)));
        assertEquals(list(), collect(range(-1, -5), λ(n, n)));
        assertEquals(list(-5, -4, -3, -2, -1), collect(range(-5, -1), λ(n, n)));
        assertEquals(list(1), collect(range(1, 1), λ(n, n)));
        assertEquals(list(-1), collect(range(-1, -1), λ(n, n)));
    }

    @Test
    public void exclusiveRanges() throws Exception {
        assertEquals(list(1, 2, 3, 4), collect(range(1, 5, true), λ(n, n)));
        assertEquals(list(), collect(range(-1, -5, true), λ(n, n)));
        assertEquals(list(-5, -4, -3, -2), collect(range(-5, -1, true), λ(n, n)));
        assertEquals(list(), collect(range(1, 1, true), λ(n, n)));
        assertEquals(list(), collect(range(-1, -1, true), λ(n, n)));
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
        assertEquals(-1, (int) detect(oneToTen, ifNone(-1), λ(n, n < 0)));
    }

    @Test
    public void partitionListIntoTwoBasedOnPreducate() throws Exception {
        List<Integer> even = list(2, 4, 6, 8, 10);
        List<Integer> odd = list(1, 3, 5, 7, 9);

        List<EList<Integer>> partition = partition(oneToTen, λ(n, n % 2 == 0));

        assertEquals(even, partition.get(0));
        assertEquals(odd, partition.get(1));
    }

    @Test
    public void anyOnEmptyList() throws Exception {
        assertFalse(any(list(int.class), λ(n, n > 0)));
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
    public void anyNotNull() throws Exception {
        assertTrue(any(list("hello", null), λ(s, s)));
    }

    @Test
    public void anyNull() throws Exception {
        assertFalse(any(list((Object) null), λ(obj, obj)));
    }

    @Test
    public void anyFalse() throws Exception {
        assertFalse(any(list(false), λ(b, b)));
    }

    @Test
    public void anyTrue() throws Exception {
        assertTrue(any(list(false, true), λ(b, b)));
    }

    @Test
    public void anyFalseAndNull() throws Exception {
        assertFalse(any(list((Object) null, false), λ(obj, obj)));
    }

    @Test
    public void anyNotNullAndFalse() throws Exception {
        assertTrue(any(asList(new Object[] { "hello", false }), λ(obj, obj)));
    }

    @Test
    public void anyNotNullAndTrue() throws Exception {
        assertTrue(any(asList(new Object[] { "hello", true }), λ(obj, obj)));
    }

    @Test
    public void anyNullAndTrue() throws Exception {
        assertTrue(any(asList(new Object[] { null, true }), λ(obj, obj)));
    }

    @Test
    public void allOnEmptyList() throws Exception {
        assertTrue(all(list(int.class), λ(n, n > 0)));
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
    public void allNotNull() throws Exception {
        assertTrue(all(list("hello", "world"), λ(s, s)));
    }

    @Test
    public void allNotNullAndTrue() throws Exception {
        assertTrue(all(asList(new Object[] { "hello", true }), λ(obj, obj)));
    }

    @Test
    public void allNotNullAndFalse() throws Exception {
        assertFalse(all(asList(new Object[] { "hello", false }), λ(obj, obj)));
    }

    @Test
    public void allWithNull() throws Exception {
        assertFalse(all(list("hello", null), λ(s, s)));
    }

    @Test
    public void allWithFalse() throws Exception {
        assertFalse(all(list(false, true), λ(b, b)));
    }

    @Test
    public void allTrueOnly() throws Exception {
        assertTrue(all(list(true), λ(b, b)));
    }

    @Test
    public void allTrue() throws Exception {
        assertTrue(all(list(true, true), λ(b, b)));
    }

    @Test
    public void allFalseAndNullOnly() throws Exception {
        assertFalse(all(list((Object) null, false), λ(obj, obj)));
    }

    @Test
    public void sortUsingNaturalOrder() throws Exception {
        assertEquals(oneToFive, sort(list(5, 4, 3, 2, 1)));
    }

    @Test
    public void sortUsingBlock() throws Exception {
        assertEquals(list(5, 4, 3, 2, 1), sort(oneToFive, λ(n, m, m - n)));
    }

    @Test
    public void sortByUsingBlock() throws Exception {
        List<String> actual = sortBy(list("10", "100", "1"), λ(s, s.length()));
        assertEquals(list("1", "10", "100"), actual);
    }

    @Test
    public void injectUsingInitialValue() throws Exception {
        assertEquals(55, (int) inject(oneToTen, 0, λ(n, m, n + m)));
    }

    @Test
    public void injectWithoutInitialValue() throws Exception {
        assertEquals(3628800, (int) inject(oneToTen, λ(n, m, n * m)));
    }

    @Test
    public void injectWithoutInitialValueAndOnlyOneElementReturnsElement() throws Exception {
        assertEquals(1, (int) inject(list(1), λ(n, m, n * m)));
    }

    @Test
    public void injectWithoutInitialValueAndEmptyListReturnsNull() throws Exception {
        assertNull(inject(list(int.class), λ(n, m, n * m)));
    }

    @LambdaParameter
    static List<Integer> list;

    @Test
    public void eachConsCallsBlockForEachListOfNConsecutiveElements() throws Exception {
        List<List<Integer>> result = list();
        eachCons(oneToFive, 3, λ(list, result.add(list)));
        assertEquals(3, result.size());
        assertEquals(list(1, 2, 3), result.get(0));
        assertEquals(list(2, 3, 4), result.get(1));
        assertEquals(list(3, 4, 5), result.get(2));
    }

    @Test
    public void eachConsDoesNothingIfNIsGreaterThanListSize() throws Exception {
        List<List<Integer>> result = list();
        eachCons(oneToFive, 6, λ(list, result.add(list)));
        assertTrue(result.isEmpty());
    }

    @Test
    public void eachSliceCallsBlockForEachSliceOfNElements() throws Exception {
        List<List<Integer>> result = list();
        eachSlice(oneToTen, 3, λ(list, result.add(list)));
        assertEquals(4, result.size());
        assertEquals(list(1, 2, 3), result.get(0));
        assertEquals(list(4, 5, 6), result.get(1));
        assertEquals(list(7, 8, 9), result.get(2));
        assertEquals(list(10), result.get(3));
    }

    @Test
    public void eachSliceDoesReturnRestOfListIfNIsGreaterThanAvailableElements() throws Exception {
        List<List<Integer>> result = list();
        eachSlice(oneToFive, 6, λ(list, result.add(list)));
        assertEquals(1, result.size());
        assertEquals(oneToFive, result.get(0));
    }

    @Test
    public void grepFiltersBasedOnStringRegexp() throws Exception {
        List<String> strings = list("java", "javadoc", "jar");
        List<String> result = grep(strings, ".*doc.*");
        assertEquals(list("javadoc"), result);
    }

    @Test
    public void grepFiltersBasedOnPattern() throws Exception {
        List<String> strings = list("java", "javadoc", "jar");
        List<String> result = grep(strings, Pattern.compile("^jav.*"));
        assertEquals(list("java", "javadoc"), result);
    }

    @Test
    public void grepFiltersBasedOnStringRegexpAndMapsUsingBlock() throws Exception {
        List<String> strings = list("java", "javadoc", "jar");
        List<Integer> result = grep(strings, ".*doc.*", λ(s, s.length()));
        assertEquals(list("javadoc".length()), result);
    }

    @Test
    public void grepFiltersBasedOnPatternAndMapsUsingBlock() throws Exception {
        List<String> strings = list("java", "javadoc", "jar");
        List<Integer> result = grep(strings, Pattern.compile("^jav.*"), λ(s, s.length()));
        assertEquals(list("java".length(), "javadoc".length()), result);
    }

    @Test
    public void maxReturnsLastValueUsingNaturalOrder() throws Exception {
        List<String> strings = list("albatross", "dog", "horse");
        assertEquals("horse", max(strings));
    }

    @Test
    public void maxReturnsLastValueUsingBlockAsComparator() throws Exception {
        List<String> strings = list("albatross", "dog", "horse");
        assertEquals("albatross", max(strings, λ(s, t, s.length() - t.length())));
    }

    @Test
    public void maxReturnsLastNullForEmptyList() throws Exception {
        assertNull(max(list(String.class)));
    }

    @Test
    public void minReturnsFirstValueUsingNaturalOrder() throws Exception {
        assertEquals("albatross", min(animals));
    }

    @Test
    public void minReturnsFistValueUsingBlockAsComparator() throws Exception {
        assertEquals("dog", min(animals, λ(s, t, s.length() - t.length())));
    }

    @Test
    public void minReturnsLastNullForEmptyList() throws Exception {
        assertNull(min(list(String.class)));
    }

    @Test
    public void memberReturnsTrueForExistingElement() throws Exception {
        assertTrue(member(list("hello"), "hello"));
    }

    @Test
    public void memberReturnsFalseForNonExistingElement() throws Exception {
        assertFalse(member(list("hello"), "world"));
    }

    @Test
    public void toListCreatesListFromIterable() throws Exception {
        Iterable<String> iterable = new ArrayIterable("hello", "world");
        assertEquals(list("hello", "world"), toList(iterable));
    }

    @Test
    public void toSetCreatesSetFromIterable() throws Exception {
        Iterable<String> iterable = new ArrayIterable("hello", "world");
        assertEquals(new HashSet<String>(list("hello", "world")), toSet(iterable));
    }

    @Test
    public void toSetCreatesSetFromIterableUsingBlock() throws Exception {
        Iterable<String> iterable = new ArrayIterable("hello", "world");
        Set<Integer> set = toSet(iterable, λ(s, s.length()));
        assertEquals(new HashSet<Integer>(list(5)), set);
    }

    @Test
    public void zipSingleCollection() throws Exception {
        assertEquals(list(list(1), list(2), list(3)), zip(list(1, 2, 3)));
    }

    @Test
    public void zipMoreThanOneCollection() throws Exception {
        List<Integer> a = list(4, 5, 6);
        List<Integer> b = list(7, 8, 9);

        List<EList<?>> result = zip(list(1, 2, 3), a, b);

        assertEquals(list(1, 4, 7), result.get(0));
        assertEquals(list(2, 5, 8), result.get(1));
        assertEquals(list(3, 6, 9), result.get(2));
    }

    @Test
    public void zipCollectionsOfDifferentLengthsPadsWithNull() throws Exception {
        List<Integer> a = list(4, 5);
        List<Integer> b = list(7);

        List<EList<?>> result = zip(list(1, 2, 3), a, b);

        assertEquals(list(1, 4, 7), result.get(0));
        assertEquals(list(2, 5, null), result.get(1));
        assertEquals(list(3, null, null), result.get(2));
    }

    @Test
    public void zipReturnsLengthOfFirstCollectionNumberOfCollections() throws Exception {
        List<Integer> a = list(4);
        List<Integer> b = list(7, 8, 9);

        List<EList<?>> result = zip(list(1, 2), a, b);
        assertEquals(2, result.size());

        assertEquals(list(1, 4, 7), result.get(0));
        assertEquals(list(2, null, 8), result.get(1));
    }

    @Test
    public void zipCollectionsOfDifferentTypes() throws Exception {
        List<String> a = list("4", "5", "6");
        List<Boolean> b = list(true, false, true);

        List<EList<?>> result = zip(list(1, 2, 3), a, b);

        assertEquals(list(1, "4", true), result.get(0));
        assertEquals(list(2, "5", false), result.get(1));
        assertEquals(list(3, "6", true), result.get(2));
    }

    @Test
    public void zipEmptyCollectionReturnsEmptyCollection() throws Exception {
        assertTrue(zip(list()).isEmpty());
    }
}
