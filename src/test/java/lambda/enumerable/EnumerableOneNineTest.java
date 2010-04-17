package lambda.enumerable;

import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lambda.TestBase;
import lambda.annotation.LambdaParameter;
import lambda.enumerable.collection.EList;
import lambda.enumerable.collection.EMap;

import org.junit.Test;

public class EnumerableOneNineTest extends TestBase {
    @Test
    public void countNumberOfElements() throws Exception {
        assertEquals(10, count(oneToTen));
    }

    @Test
    public void countNumberOfOccurances() throws Exception {
        assertEquals(2, count(oneToFiveTwice, 3));
    }

    @Test
    public void countUsingBlock() throws Exception {
        assertEquals(5, count(oneToTen, λ(n, n > 5)));
    }

    @Test
    public void cycleCollectionNTimes() throws Exception {
        assertEquals(oneToFiveTwice, cycle(oneToFive, 2, λ(n, n)));
    }

    @Test
    public void dropNElements() throws Exception {
        assertEquals(list(4, 5), drop(oneToFive, 3));
    }

    @Test
    public void dropNGreaterThanSizeElements() throws Exception {
        assertEquals(list(), drop(oneToFive, 6));
    }

    @Test
    public void dropZeroElements() throws Exception {
        assertEquals(oneToFive, drop(oneToFive, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropNegativeNOfElementsThrowsException() throws Exception {
        drop(oneToFive, -1);
    }

    @Test
    public void dropWhileBlockIsTrue() throws Exception {
        assertEquals(list(4, 5), dropWhile(oneToFive, λ(n, n < 4)));
    }

    @LambdaParameter
    static Map<String, String> memo;

    @Test
    public void eachWithObjectUsingMemo() throws Exception {
        List<String> list = list("cat", "dog", "wombat");
        Map<String, String> map = eachWithObject(list, new LinkedHashMap<String, String>(), λ(s, memo, memo.put(s,
                s.toUpperCase())));

        assertEquals(list, list(map.keySet()));
        assertEquals(list("CAT", "DOG", "WOMBAT"), list(map.values()));
    }

    @Test
    public void findIndexOfFirstElementForWhichBlockReturnsTrue() throws Exception {
        assertEquals(1, findIndex(oneToFive, λ(n, n == 2)));
    }

    @Test
    public void findIndexOfNonExisitngElementReturnsMinusOne() throws Exception {
        assertEquals(-1, findIndex(oneToFive, λ(n, n > 5)));
    }

    @Test
    public void firstReturnsFirstElement() throws Exception {
        assertEquals(1, (int) first(oneToFive));
    }

    @Test
    public void firstReturnsNullForEmptyCollection() throws Exception {
        assertNull(first(list()));
    }

    @Test
    public void firstNReturnsFirstNElements() throws Exception {
        assertEquals(list(1, 2), first(oneToFive, 2));
    }

    @Test
    public void groupByResultOfBlock() throws Exception {
        EMap<String, EList<Integer>> groupBy = groupBy(oneToFive, λ(n, n % 2 == 0 ? "even" : "odd"));
        assertEquals(2, groupBy.size());
        assertEquals(list(1, 3, 5), groupBy.get("odd"));
        assertEquals(list(2, 4), groupBy.get("even"));
    }

    @Test
    public void maxByResultOfBlock() throws Exception {
        assertEquals("albatross", maxBy(animals, λ(s, s.length())));
        assertEquals("fox", maxBy(animals, λ(s, new StringBuilder(s).reverse().toString())));
    }

    @Test
    public void minByResultOfBlock() throws Exception {
        assertEquals("dog", minBy(animals, λ(s, s.length())));
        assertEquals("horse", minBy(animals, λ(s, new StringBuilder(s).reverse().toString())));
    }

    @Test
    public void minMaxReturnsListWithTwoItems() throws Exception {
        assertEquals(list("albatross", "horse"), minMax(animals));
    }

    @Test
    public void minMaxReturnsListWithTwoItemsBasedOnResultOfBlockComparator() throws Exception {
        assertEquals(list("dog", "albatross"), minMax(animals, λ(s, t, s.length() - t.length())));
    }

    @Test
    public void minMaxByResultOfBlock() throws Exception {
        assertEquals(list("dog", "albatross"), minMaxBy(animals, λ(s, s.length())));
        assertEquals(list("horse", "fox"), minMaxBy(animals, λ(s, new StringBuilder(s).reverse().toString())));
    }

    @Test
    public void noneReturnsTrueIfBlockIsNeverOtherThanFalseOrNull() throws Exception {
        assertTrue(none(oneToFive, λ(n, n > 5)));
    }

    @Test
    public void noneReturnsTrueIfBlockIsNull() throws Exception {
        assertTrue(none(list((Object) null), λ(obj, obj)));
    }

    @Test
    public void noneReturnsTrueIfBlockIsFalse() throws Exception {
        assertTrue(none(list(false), λ(b, b)));
    }

    @Test
    public void noneReturnsTrueIfBlockIsFalseOrNull() throws Exception {
        assertTrue(none(list(false, (Object) null), λ(obj, obj)));
    }

    @Test
    public void noneReturnsTrueForEmptyList() throws Exception {
        assertTrue(none(list(), λ(obj, obj)));
    }

    @Test
    public void noneReturnsFalseIfBlockIsEverNonNull() throws Exception {
        assertFalse(none(list((Object) null, ""), λ(obj, obj)));
    }

    @Test
    public void noneReturnsFalseIfBlockIsEverTrue() throws Exception {
        assertFalse(none(list(false, true), λ(b, b)));
    }

    @Test
    public void oneReturnsTrueForBlockReturningTrueASingleTime() throws Exception {
        assertTrue(one(oneToFive, λ(n, n == 3)));
    }

    @Test
    public void oneReturnsTrueForBlockReturningNotNullASingleTime() throws Exception {
        assertTrue(one(list("", (Object) null), λ(obj, obj)));
    }

    @Test
    public void oneReturnsFalseForBlockReturningNotNullOnceAndNullTwice() throws Exception {
        assertTrue(one(list("", (Object) null, (Object) null), λ(obj, obj)));
    }

    @Test
    public void oneReturnsFalseForBlockNeverReturningTrue() throws Exception {
        assertFalse(one(oneToFive, λ(n, n > 5)));
    }

    @Test
    public void oneReturnsFalseForBlockReturningTrueTwice() throws Exception {
        assertFalse(one(oneToFive, λ(n, n > 3)));
    }

    @Test
    public void oneReturnsFalseForBlockReturningTrueMoreThanOnce() throws Exception {
        assertFalse(one(oneToFive, λ(n, n % 2 == 0)));
    }

    @Test
    public void oneReturnsFalseForEmptyList() throws Exception {
        assertFalse(one(list(), λ(obj, true)));
    }

    @Test
    public void reverseEachEvaluatesBlockForEachElementInReverseOrder() throws Exception {
        List<Integer> actual = list();
        reverseEach(oneToFive, λ(n, actual.add(n)));
        assertEquals(list(5, 4, 3, 2, 1), actual);
    }

    @Test
    public void takeNElements() throws Exception {
        assertEquals(list(1, 2, 3), take(oneToFive, 3));
    }

    @Test
    public void takeNGreaterThanSizeElements() throws Exception {
        assertEquals(oneToFive, take(oneToFive, 6));
    }

    @Test
    public void takeZeroElements() throws Exception {
        assertEquals(list(), take(oneToFive, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeNegativeNOfElementsThrowsException() throws Exception {
        take(oneToFive, -1);
    }

    @Test
    public void takeWhileBlockIsTrue() throws Exception {
        assertEquals(list(1, 2), takeWhile(oneToFive, λ(n, n < 3)));
    }
}