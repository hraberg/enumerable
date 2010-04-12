package lambda.enumerable.primitives;

import static lambda.Parameters.*;
import static lambda.enumerable.primitives.EnumerableLongs.*;
import static lambda.primitives.LambdaPrimitives.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import lambda.TestBase;
import lambda.annotation.LambdaParameter;
import lambda.enumerable.EnumerableArrays;

import org.junit.Test;

public class EnumerableLongsTest extends TestBase {
    long[] longsOneToFive = new long[] { 1, 2, 3, 4, 5 };

    @Test
    public void callsBlockOnceForEachElement() throws Exception {
        List<Long> actual = list();
        each(longsOneToFive, λ(l, actual.add(l)));
        assertEquals(toList(longsOneToFive), actual);
    }

    @Test
    public void canUsePrimitiveLambdaWithMatchingObjectVersion() throws Exception {
        List<Long> actual = list();
        EnumerableArrays.each(new Long[] { 1L, 2L, 3L, 4L, 5L }, λ(l, actual.add(l)));
        assertEquals(toList(longsOneToFive), actual);
    }

    @Test
    public void callsBlockOnceForEachElementWithLongReturn() throws Exception {
        long result = 1;
        each(longsOneToFive, λ(l, result *= 314 * l));
        assertEquals(366293731418880L, result);
    }

    @Test
    public void eachReturnsArray() throws Exception {
        assertArrayEquals(longsOneToFive, each(longsOneToFive, λ(l, l)));
    }

    @LambdaParameter
    public static int[] ints;

    @Test
    public void collectElementsToDifferentType() throws Exception {
        String[] expected = new String[] { "#1", "#2", "#3", "#4", "#5" };
        Object[] actual = collect(longsOneToFive, λ(l, "#" + l));
        assertArrayEquals(expected, actual);
        assertFalse(expected.getClass().equals(actual.getClass()));

        String[] actualStrings = collect(longsOneToFive, λ(l, "#" + l), String.class);
        assertArrayEquals(expected, actualStrings);
    }

    @Test
    public void collectElementsToDifferentTypeWithEmptyArray() throws Exception {
        String[] expected = new String[] {};
        Object[] actuals = collect(new long[0], λ(l, "#" + l));
        assertArrayEquals(expected, actuals);
    }

    @Test
    public void eachWithIndexUsingMixedPrimitivesLongAndInt() throws Exception {
        int totalIndex = 0;
        eachWithIndex(longsOneToFive, λ(l, idx, totalIndex += idx));
        assertEquals(10, totalIndex);
    }

    @Test
    public void eachWithIndexUsingMixedPrimitivesIntAndLong() throws Exception {
        int total = 0;
        eachWithIndex(longsOneToFive, λ(i, l, total += i));
        assertEquals(15, total);
    }

    @LambdaParameter
    static short aShort;

    @Test
    public void eachWithIndexUsingMixedPrimitivesLongAndShort() throws Exception {
        short totalIndex = 0;
        eachWithIndex(longsOneToFive, λ(l, aShort, totalIndex += aShort));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static byte aByte;

    @Test
    public void eachWithIndexUsingMixedPrimitivesLongAndByte() throws Exception {
        byte totalIndex = 0;
        eachWithIndex(longsOneToFive, λ(l, aByte, totalIndex += aByte));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static char aChar;

    @Test
    public void eachWithIndexUsingMixedPrimitivesLongAndChar() throws Exception {
        char totalIndex = 0;
        eachWithIndex(longsOneToFive, λ(l, aChar, totalIndex += aChar));
        assertEquals(10, totalIndex);
    }

    @Test
    public void eachWithIndexIsZeroBased() throws Exception {
        List<Long> actual = list();
        eachWithIndex(longsOneToFive, λ(l, k, actual.add(k + 1)));
        assertEquals(list(1L, 2L, 3L, 4L, 5L), actual);
    }

    @Test
    public void eachWithIndexToString() throws Exception {
        String indexes = "";
        eachWithIndex(longsOneToFive, λ(l, k, indexes += k));
        assertEquals("01234", indexes);
    }

    @Test
    public void selectMatchingElements() throws Exception {
        long[] selected = select(longsOneToFive, λ(l, l % 2 == 0));
        assertArrayEquals(new long[] { 2, 4 }, selected);
    }

    @Test
    public void rejectMatchingElements() throws Exception {
        long[] odd = { 1, 3, 5 };
        assertArrayEquals(odd, reject(longsOneToFive, λ(l, l % 2 == 0)));
    }

    @Test
    public void sortUsingNaturalOrder() throws Exception {
        assertArrayEquals(longsOneToFive, sort(new long[] { 5, 4, 3, 2, 1 }));
    }

    @Test
    public void injectUsingInitialValue() throws Exception {
        assertEquals(15, inject(longsOneToFive, 0, λ(l, k, l + k)));
    }

    @Test
    public void injectWithoutInitialValue() throws Exception {
        assertEquals(120, inject(longsOneToFive, λ(l, k, l * k)));
    }

    @Test
    public void injectWithoutInitialValueAndOnlyOneElementReturnsElement() throws Exception {
        assertEquals(1, inject(new long[] { 1 }, λ(l, k, l * k)));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void injectWithoutInitialValueAndEmptyArrayThrowsException() throws Exception {
        inject(new long[0], λ(l, k, l * k));
    }

    @Test
    public void anyOnEmptyArray() throws Exception {
        assertFalse(any(new long[0], λ(l, l > 0)));
    }

    @Test
    public void anyMarchingPredicate() throws Exception {
        assertTrue(any(longsOneToFive, λ(l, l > 3)));
    }

    @Test
    public void anyNotMatchingPredicate() throws Exception {
        assertFalse(any(longsOneToFive, λ(l, l > 5)));
    }

    @Test
    public void allMatchingPredicate() throws Exception {
        assertTrue(all(longsOneToFive, λ(l, l > 0)));
    }

    @Test
    public void allNotMatchingPredicate() throws Exception {
        assertFalse(all(longsOneToFive, λ(l, l > 1)));
    }

    @Test
    public void allOnEmptyList() throws Exception {
        assertTrue(all(new long[0], λ(l, l > 0)));
    }

    @Test(expected = NoSuchElementException.class)
    public void maxThrowsExceptionForEmptyArray() throws Exception {
        max(new long[0]);
    }

    @Test
    public void maxReturnsLastValueUsingNaturalOrder() throws Exception {
        assertEquals(5, max(longsOneToFive));
    }

    @Test
    public void minReturnsFirstValueUsingNaturalOrder() throws Exception {
        assertEquals(1, min(longsOneToFive));
    }

    @Test(expected = NoSuchElementException.class)
    public void minThrowsExceptionForEmptyArray() throws Exception {
        assertEquals(0, min(new long[0]));
    }

    @Test
    public void memberReturnsTrueForExistingElement() throws Exception {
        assertTrue(member(longsOneToFive, 3));
    }

    @Test
    public void memberReturnsFalseForNonExistingElement() throws Exception {
        assertFalse(member(longsOneToFive, 0));
    }

    @Test
    public void toListCreatesIntegerListFromLongArray() throws Exception {
        assertEquals(list(1L, 2L, 3L, 4L, 5L), toList(longsOneToFive));
    }

    @Test
    public void toSetCreatesIntegerSetFromLongArray() throws Exception {
        Set<Long> expected = new HashSet<Long>(list(1L, 2L, 3L, 4L));
        assertEquals(expected, toSet(new long[] { 1, 2, 2, 3, 4, 4 }));
    }

    @Test
    public void toSetCreatesIntegerSetFromIntArrayUsingBlock() throws Exception {
        Set<String> expected = new HashSet<String>(list("1", "2", "3", "4"));
        assertEquals(expected, toSet(new long[] { 1, 2, 2, 3, 4, 4 }, λ(l, l + "")));
    }

    @Test
    public void partitionArrayIntoTwoBasedOnPreducate() throws Exception {
        long[] even = { 2, 4 };
        long[] odd = { 1, 3, 5 };

        long[][] partition = partition(longsOneToFive, λ(l, l % 2 == 0));

        assertArrayEquals(even, partition[0]);
        assertArrayEquals(odd, partition[1]);
    }
}
