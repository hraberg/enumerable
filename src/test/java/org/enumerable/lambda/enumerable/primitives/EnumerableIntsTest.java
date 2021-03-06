package org.enumerable.lambda.enumerable.primitives;

import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.enumerable.primitives.EnumerableInts.*;
import static org.enumerable.lambda.primitives.LambdaPrimitives.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.enumerable.lambda.TestBase;
import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

public class EnumerableIntsTest extends TestBase {
    int[] intsOneToFive = new int[] { 1, 2, 3, 4, 5 };

    @Test
    public void callsBlockOnceForEachElement() throws Exception {
        List<Integer> actual = list();
        each(intsOneToFive, λ(n, actual.add(n)));
        assertEquals(toList(intsOneToFive), actual);
    }

    @Test
    public void callsBlockOnceForEachElementWithDoubleReturn() throws Exception {
        double result = 1;
        each(intsOneToFive, λ(n, result *= 3.14 * n));
        assertEquals(36629.373141888005, result, 0);
    }

    @Test
    public void eachReturnsArray() throws Exception {
        assertArrayEquals(intsOneToFive, each(intsOneToFive, λ(n, n)));
    }

    @LambdaParameter
    public static int[] ints;

    @Test
    public void collectElementsToDifferentType() throws Exception {
        String[] expected = new String[] { "#1", "#2", "#3", "#4", "#5" };
        Object[] actual = collect(intsOneToFive, λ(n, "#" + n));
        assertArrayEquals(expected, actual);
        assertFalse(expected.getClass().equals(actual.getClass()));

        String[] actualStrings = collect(intsOneToFive, λ(n, "#" + n), String.class);
        assertArrayEquals(expected, actualStrings);
    }

    @Test
    public void collectElementsToDifferentTypeWithEmptyArray() throws Exception {
        String[] expected = new String[] {};
        Object[] actuals = collect(new int[0], λ(n, "#" + n));
        assertArrayEquals(expected, actuals);
    }

    @Test
    public void detectFirstMatchingElement() throws Exception {
        assertEquals(2, detect(intsOneToFive, ifNone(-1), λ(n, n % 2 == 0)));
    }

    @Test
    public void detectReturnsIfNoneValueIfNoMatch() throws Exception {
        assertEquals(-1, detect(intsOneToFive, ifNone(-1), λ(n, n > 5)));
    }

    @Test
    public void eachWithIndexUsingAllPrimitives() throws Exception {
        int totalIndex = 0;
        eachWithIndex(intsOneToFive, λ(n, idx, totalIndex += idx));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static short aShort;

    @Test
    public void eachWithIndexUsingMixedPrimitivesIntAndShort() throws Exception {
        short totalIndex = 0;
        eachWithIndex(intsOneToFive, λ(n, aShort, totalIndex += aShort));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static byte aByte;

    @Test
    public void eachWithIndexUsingMixedPrimitivesIntAndByte() throws Exception {
        byte totalIndex = 0;
        eachWithIndex(intsOneToFive, λ(n, aByte, totalIndex += aByte));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static char aChar;

    @Test
    public void eachWithIndexUsingMixedPrimitivesIntAndChar() throws Exception {
        char totalIndex = 0;
        eachWithIndex(intsOneToFive, λ(n, aChar, totalIndex += aChar));
        assertEquals(10, totalIndex);
    }

    @Test
    public void eachWithIndexIsZeroBased() throws Exception {
        List<Integer> actual = list();
        eachWithIndex(intsOneToFive, λ(n, idx, actual.add(idx + 1)));
        assertEquals(toList(intsOneToFive), actual);
    }

    @Test
    public void eachWithIndexToString() throws Exception {
        String indexes = "";
        eachWithIndex(intsOneToFive, λ(n, idx, indexes += idx));
        assertEquals("01234", indexes);
    }

    @Test
    public void selectMatchingElements() throws Exception {
        int[] selected = select(intsOneToFive, λ(n, n % 2 == 0));
        assertArrayEquals(new int[] { 2, 4 }, selected);
    }

    @Test
    public void rejectMatchingElements() throws Exception {
        int[] odd = { 1, 3, 5 };
        assertArrayEquals(odd, reject(intsOneToFive, λ(n, n % 2 == 0)));
    }

    @Test
    public void sortUsingNaturalOrder() throws Exception {
        assertArrayEquals(intsOneToFive, sort(new int[] { 5, 4, 3, 2, 1 }));
    }

    @Test
    public void injectUsingInitialValue() throws Exception {
        assertEquals(15, inject(intsOneToFive, 0, λ(n, m, n + m)));
    }

    @Test
    public void injectWithoutInitialValue() throws Exception {
        assertEquals(120, inject(intsOneToFive, λ(n, m, n * m)));
    }

    @Test
    public void injectWithoutInitialValueAndOnlyOneElementReturnsElement() throws Exception {
        assertEquals(1, inject(new int[] { 1 }, λ(n, m, n * m)));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void injectWithoutInitialValueAndEmptyArrayThrowsException() throws Exception {
        inject(new int[0], λ(n, m, n * m));
    }

    @Test
    public void anyOnEmptyArray() throws Exception {
        assertFalse(any(new int[0], λ(n, n > 0)));
    }

    @Test
    public void anyMarchingPredicate() throws Exception {
        assertTrue(any(intsOneToFive, λ(n, n > 3)));
    }

    @Test
    public void anyNotMatchingPredicate() throws Exception {
        assertFalse(any(intsOneToFive, λ(n, n > 5)));
    }

    @Test
    public void allMatchingPredicate() throws Exception {
        assertTrue(all(intsOneToFive, λ(n, n > 0)));
    }

    @Test
    public void allNotMatchingPredicate() throws Exception {
        assertFalse(all(intsOneToFive, λ(n, n > 1)));
    }

    @Test
    public void allOnEmptyList() throws Exception {
        assertTrue(all(new int[0], λ(n, n > 0)));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void maxThrowsExceptionForEmptyArray() throws Exception {
        max(new int[0]);
    }

    @Test
    public void maxReturnsLastValueUsingNaturalOrder() throws Exception {
        assertEquals(5, max(intsOneToFive));
    }

    @Test
    public void maxReturnsFirstValueUsingReverseNaturalOrder() throws Exception {
        assertEquals(1, max(intsOneToFive, λ(n, m, m - n)));
    }

    @Test
    public void minReturnsFirstValueUsingNaturalOrder() throws Exception {
        assertEquals(1, min(intsOneToFive));
    }

    @Test
    public void minReturnsLastValueUsingReverseNaturalOrder() throws Exception {
        assertEquals(5, min(intsOneToFive, λ(n, m, m - n)));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void minThrowsExceptionForEmptyArray() throws Exception {
        min(new int[0]);
    }

    @Test
    public void memberReturnsTrueForExistingElement() throws Exception {
        assertTrue(member(intsOneToFive, 3));
    }

    @Test
    public void memberReturnsFalseForNonExistingElement() throws Exception {
        assertFalse(member(intsOneToFive, 0));
    }

    @Test
    public void toListCreatesIntegerListFromIntArray() throws Exception {
        assertEquals(list(1, 2, 3, 4, 5), toList(intsOneToFive));
    }

    @Test
    public void toSetCreatesIntegerSetFromIntArray() throws Exception {
        Set<Integer> expected = new HashSet<Integer>(list(1, 2, 3, 4));
        assertEquals(expected, toSet(new int[] { 1, 2, 2, 3, 4, 4 }));
    }

    @Test
    public void toSetCreatesIntegerSetFromIntArrayUsingBlock() throws Exception {
        Set<String> expected = new HashSet<String>(list("1", "2", "3", "4"));
        assertEquals(expected, toSet(new int[] { 1, 2, 2, 3, 4, 4 }, λ(n, n + "")));
    }

    @Test
    public void partitionArrayIntoTwoBasedOnPredicate() throws Exception {
        int[] even = { 2, 4 };
        int[] odd = { 1, 3, 5 };

        int[][] partition = partition(intsOneToFive, λ(n, n % 2 == 0));

        assertArrayEquals(even, partition[0]);
        assertArrayEquals(odd, partition[1]);
    }
}
