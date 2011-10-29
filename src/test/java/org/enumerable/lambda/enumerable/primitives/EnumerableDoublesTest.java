package org.enumerable.lambda.enumerable.primitives;

import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.enumerable.primitives.EnumerableDoubles.*;
import static org.enumerable.lambda.primitives.LambdaPrimitives.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.enumerable.lambda.TestBase;
import org.enumerable.lambda.annotation.LambdaParameter;
import org.enumerable.lambda.enumerable.EnumerableArrays;
import org.junit.Test;

public class EnumerableDoublesTest extends TestBase {
    double[] doublesOneToFive = new double[] { 1, 2, 3, 4, 5 };

    @Test
    public void callsBlockOnceForEachElement() throws Exception {
        List<Double> actual = list();
        each(doublesOneToFive, λ(d, actual.add(d)));
        assertEquals(toList(doublesOneToFive), actual);
    }

    @Test
    public void canUsePrimitiveLambdaWithMatchingObjectVersion() throws Exception {
        List<Double> actual = list();
        EnumerableArrays.each(new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 }, λ(d, actual.add(d)));
        assertEquals(toList(doublesOneToFive), actual);
    }

    @Test
    public void callsBlockOnceForEachElementWithDoubleReturn() throws Exception {
        double result = 1;
        each(doublesOneToFive, λ(d, result *= 3.14 * d));
        assertEquals(36629.373141888005, result, 0);
    }

    @Test
    public void eachReturnsArray() throws Exception {
        assertArrayEquals(doublesOneToFive, each(doublesOneToFive, λ(d, d)), 0.0);
    }

    @LambdaParameter
    public static int[] ints;

    @Test
    public void collectElementsToDifferentType() throws Exception {
        String[] expected = new String[] { "#1.0", "#2.0", "#3.0", "#4.0", "#5.0" };
        Object[] actual = collect(doublesOneToFive, λ(d, "#" + d));
        assertArrayEquals(expected, actual);
        assertFalse(expected.getClass().equals(actual.getClass()));

        String[] actualStrings = collect(doublesOneToFive, λ(d, "#" + d), String.class);
        assertArrayEquals(expected, actualStrings);
    }

    @Test
    public void collectElementsToDifferentTypeWithEmptyArray() throws Exception {
        String[] expected = new String[] {};
        Object[] actuals = collect(new double[0], λ(d, "#" + d));
        assertArrayEquals(expected, actuals);
    }

    @Test
    public void detectFirstMatchingElement() throws Exception {
        assertEquals(2.0, detect(doublesOneToFive, ifNone(-1), λ(d, d % 2 == 0)), 0.0);
    }

    @Test
    public void detectReturnsIfNoneValueIfNoMatch() throws Exception {
        assertEquals(-1.0, detect(doublesOneToFive, ifNone(-1), λ(d, d > 5)), 0.0);
    }

    @Test
    public void eachWithIndexUsingMixedPrimitivesDoubleAndInt() throws Exception {
        int totalIndex = 0;
        eachWithIndex(doublesOneToFive, λ(d, idx, totalIndex += idx));
        assertEquals(10, totalIndex);
    }

    @Test
    public void eachWithIndexUsingMixedPrimitivesDoubleAndLong() throws Exception {
        int totalIndex = 0;
        eachWithIndex(doublesOneToFive, λ(d, l, totalIndex += l));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static float aFloat;

    @Test
    public void eachWithIndexUsingMixedPrimitivesDoubleAndFloat() throws Exception {
        float totalIndex = 0;
        eachWithIndex(doublesOneToFive, λ(d, aFloat, totalIndex += aFloat));
        assertEquals(10, totalIndex, 0.0);
    }

    @LambdaParameter
    static short aShort;

    @Test
    public void eachWithIndexUsingMixedPrimitivesDoubleAndShort() throws Exception {
        short totalIndex = 0;
        eachWithIndex(doublesOneToFive, λ(d, aShort, totalIndex += aShort));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static byte aByte;

    @Test
    public void eachWithIndexUsingMixedPrimitivesDoubleAndByte() throws Exception {
        byte totalIndex = 0;
        eachWithIndex(doublesOneToFive, λ(d, aByte, totalIndex += aByte));
        assertEquals(10, totalIndex);
    }

    @LambdaParameter
    static char aChar;

    @Test
    public void eachWithIndexUsingMixedPrimitivesDoubleAndChar() throws Exception {
        char totalIndex = 0;
        eachWithIndex(doublesOneToFive, λ(d, aChar, totalIndex += aChar));
        assertEquals(10, totalIndex);
    }

    @Test
    public void eachWithIndexUsingMixedPrimitivesIntAndDouble() throws Exception {
        int total = 0;
        eachWithIndex(doublesOneToFive, λ(i, d, total += i));
        assertEquals(15, total);
    }

    @Test
    public void eachWithIndexUsingMixedPrimitivesLongAndDouble() throws Exception {
        int total = 0;
        eachWithIndex(doublesOneToFive, λ(l, d, total += l));
        assertEquals(15, total);
    }

    @Test
    public void eachWithIndexIsZeroBased() throws Exception {
        List<Integer> actual = list();
        eachWithIndex(doublesOneToFive, λ(d, x, actual.add((int) x + 1)));
        assertEquals(list(1, 2, 3, 4, 5), actual);
    }

    @Test
    public void eachWithIndexToString() throws Exception {
        String indexes = "";
        eachWithIndex(doublesOneToFive, λ(d, x, indexes += x));
        assertEquals("0.01.02.03.04.0", indexes);
    }

    @Test
    public void selectMatchingElements() throws Exception {
        double[] selected = select(doublesOneToFive, λ(d, d % 2 == 0));
        assertArrayEquals(new double[] { 2, 4 }, selected, 0.0);
    }

    @Test
    public void rejectMatchingElements() throws Exception {
        double[] odd = { 1, 3, 5 };
        assertArrayEquals(odd, reject(doublesOneToFive, λ(d, d % 2 == 0)), 0.0);
    }

    @Test
    public void sortUsingNaturalOrder() throws Exception {
        assertArrayEquals(doublesOneToFive, sort(new double[] { 5, 4, 3, 2, 1 }), 0.0);
    }

    @Test
    public void injectUsingInitialValue() throws Exception {
        assertEquals(15, inject(doublesOneToFive, 0, λ(x, y, x + y)), 0.0);
    }

    @Test
    public void injectWithoutInitialValue() throws Exception {
        assertEquals(120, inject(doublesOneToFive, λ(x, y, x * y)), 0.0);
    }

    @Test
    public void injectWithoutInitialValueAndOnlyOneElementReturnsElement() throws Exception {
        assertEquals(1.0, inject(new double[] { 1.0 }, λ(x, y, x * y)), 0.0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void injectWithoutInitialValueAndEmptyArrayThrowsException() throws Exception {
        inject(new double[0], λ(x, y, x * y));
    }

    @Test
    public void anyOnEmptyArray() throws Exception {
        assertFalse(any(new double[0], λ(d, d > 0)));
    }

    @Test
    public void anyMarchingPredicate() throws Exception {
        assertTrue(any(doublesOneToFive, λ(d, d > 3)));
    }

    @Test
    public void anyNotMatchingPredicate() throws Exception {
        assertFalse(any(doublesOneToFive, λ(d, d > 5)));
    }

    @Test
    public void allMatchingPredicate() throws Exception {
        assertTrue(all(doublesOneToFive, λ(d, d > 0)));
    }

    @Test
    public void allNotMatchingPredicate() throws Exception {
        assertFalse(all(doublesOneToFive, λ(d, d > 1)));
    }

    @Test
    public void allOnEmptyList() throws Exception {
        assertTrue(all(new double[0], λ(d, d > 0)));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void maxThrowsExceptionForEmptyArray() throws Exception {
        max(new double[0]);
    }

    @Test
    public void maxReturnsLastValueUsingNaturalOrder() throws Exception {
        assertEquals(5, max(doublesOneToFive), 0.0);
    }

    @Test
    public void maxReturnsFirstValueUsingReverseNaturalOrder() throws Exception {
        assertEquals(1, max(doublesOneToFive, λ(x, y, y - x)), 0.0);
    }

    @Test
    public void minReturnsFirstValueUsingNaturalOrder() throws Exception {
        assertEquals(1, min(doublesOneToFive), 0.0);
    }

    @Test
    public void minReturnsLastValueUsingReverseNaturalOrder() throws Exception {
        assertEquals(5, min(doublesOneToFive, λ(x, y, y - x)), 0.0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void minThrowsExceptionForEmptyArray() throws Exception {
        min(new double[0]);
    }

    @Test
    public void memberReturnsTrueForExistingElement() throws Exception {
        assertTrue(member(doublesOneToFive, 3));
    }

    @Test
    public void memberReturnsFalseForNonExistingElement() throws Exception {
        assertFalse(member(doublesOneToFive, 0));
    }

    @Test
    public void toListCreatesIntegerListFromDoubleArray() throws Exception {
        assertEquals(list(1.0, 2.0, 3.0, 4.0, 5.0), toList(doublesOneToFive));
    }

    @Test
    public void toSetCreatesIntegerSetFromDoubleArray() throws Exception {
        Set<Double> expected = new HashSet<Double>(list(1.0, 2.0, 3.0, 4.0));
        assertEquals(expected, toSet(new double[] { 1, 2, 2, 3, 4, 4 }));
    }

    @Test
    public void toSetCreatesIntegerSetFromIntArrayUsingBlock() throws Exception {
        Set<String> expected = new HashSet<String>(list("1.0", "2.0", "3.0", "4.0"));
        assertEquals(expected, toSet(new double[] { 1, 2, 2, 3, 4, 4 }, λ(d, d + "")));
    }

    @Test
    public void partitionArrayIntoTwoBasedOnPredicate() throws Exception {
        double[] even = { 2, 4 };
        double[] odd = { 1, 3, 5 };

        double[][] partition = partition(doublesOneToFive, λ(d, d % 2 == 0));

        assertArrayEquals(even, partition[0], 0.0);
        assertArrayEquals(odd, partition[1], 0.0);
    }
}
