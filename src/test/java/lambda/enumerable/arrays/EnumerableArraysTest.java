package lambda.enumerable.arrays;

import static java.util.Arrays.*;
import static lambda.Lambda.*;
import static lambda.enumerable.arrays.EnumerableArrays.*;
import static org.junit.Assert.*;

import java.util.List;

import lambda.TestBase;

import org.junit.Test;

public class EnumerableArraysTest extends TestBase {
    Integer[] oneToFive = new Integer[] { 1, 2, 3, 4, 5 };

    @Test
    public void callsBlockOnceForEachElement() throws Exception {
        List<Integer> actual = list();
        each(oneToFive, λ(n, actual.add(n)));
        assertArrayEquals(oneToFive, actual.toArray());
    }

    @Test
    public void collectElementsToDifferentType() throws Exception {
        String[] expected = new String[] { "#1", "#2", "#3", "#4", "#5" };
        Object[] actual = collect(oneToFive, λ(n, "#" + n));
        assertArrayEquals(expected, actual);
        assertFalse(expected.getClass().equals(actual.getClass()));

        String[] strings = copyOf(actual, actual.length, String[].class);
        assertArrayEquals(expected, strings);
        assertEquals(expected.getClass(), strings.getClass());
    }

    @Test
    public void collectElementsToDifferentTypeWithEmptyArray() throws Exception {
        String[] expected = new String[] {};
        Object[] actuals = collect(new Integer[0], λ(n, "#" + n));
        assertArrayEquals(expected, actuals);
    }

    @Test
    public void selectMatchingElements() throws Exception {
        Integer[] selected = select(oneToFive, λ(n, n % 2 == 0));
        assertArrayEquals(new Integer[] { 2, 4 }, selected);
    }

    @Test
    public void detectFirstMatchingElement() throws Exception {
        assertEquals(2, (int) detect(oneToFive, λ(n, n % 2 == 0)));
    }

    @Test
    public void anyOnEmptyArray() throws Exception {
        assertFalse(any(new Integer[0], λ(n, n > 0)));
    }

    @Test
    public void maxReturnsLastNullForEmptyArray() throws Exception {
        assertNull(max(new String[0]));
    }

    @Test
    public void partitionArrayIntoTwoBasedOnPreducate() throws Exception {
        Integer[] even = { 2, 4 };
        Integer[] odd = { 1, 3, 5 };

        Integer[][] partition = partition(oneToFive, λ(n, n % 2 == 0));

        assertArrayEquals(even, partition[0]);
        assertArrayEquals(odd, partition[1]);
    }

    @Test
    public void zipMoreThanOneCollection() throws Exception {
        Integer[] array = new Integer[] { 1, 2, 3 };
        Integer[] a = new Integer[] { 4, 5, 6 };
        Integer[] b = new Integer[] { 7, 8, 9 };

        Object[][] result = zip(array, a, b);

        assertArrayEquals(list(1, 4, 7).toArray(), result[0]);
        assertArrayEquals(list(2, 5, 8).toArray(), result[1]);
        assertArrayEquals(list(3, 6, 9).toArray(), result[2]);
    }
}