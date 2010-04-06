package lambda.enumerable.arrays;

import static lambda.Lambda.*;
import static lambda.Lambda.Primitives.*;
import static lambda.enumerable.array.EnumerableDoubles.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lambda.TestBase;
import lambda.Lambda.Primitives;
import lambda.annotation.LambdaParameter;

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
    public void eachWithIndexUsingMixedPrimitives() throws Exception {
        int totalIndex = 0;
        eachWithIndex(doublesOneToFive, Primitives.λ(d, idx, totalIndex += idx));
        assertEquals(10, totalIndex);
    }

    @Test
    public void eachWithIndexIsZeroBased() throws Exception {
        List<Integer> actual = list();
        eachWithIndex(doublesOneToFive, Primitives.λ(d, x, actual.add((int) x + 1)));
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
        double[] odd = {1, 3, 5};
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

    @Test
    public void maxReturnsZeroForEmptyArray() throws Exception {
        assertEquals(0, max(new double[0]), 0.0);
    }

    @Test
    public void maxReturnsLastValueUsingNaturalOrder() throws Exception {
        assertEquals(5, max(doublesOneToFive), 0.0);
    }

    @Test
    public void minReturnsFirstValueUsingNaturalOrder() throws Exception {
        assertEquals(1, min(doublesOneToFive), 0.0);
    }

    @Test
    public void minReturnsZeroForEmptyList() throws Exception {
        assertEquals(0, min(new double[0]), 0.0);
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
    public void toListCreatesIntegerListFromIntArray() throws Exception {
        assertEquals(list(1.0, 2.0, 3.0, 4.0, 5.0), toList(doublesOneToFive));
    }

    @Test
    public void toSetCreatesIntegerSetFromIntArray() throws Exception {
        Set<Double> expected = new HashSet<Double>(list(1.0, 2.0, 3.0, 4.0));
        assertEquals(expected, toSet(new double[] {1, 2, 2, 3, 4, 4}));
    }

    @Test
    public void toSetCreatesIntegerSetFromIntArrayUsingBlock() throws Exception {
        Set<String> expected = new HashSet<String>(list("1.0", "2.0", "3.0", "4.0"));
        assertEquals(expected, toSet(new double[] {1, 2, 2, 3, 4, 4}, λ(d, d + "")));
    }

    @Test
    public void partitionArrayIntoTwoBasedOnPreducate() throws Exception {
        double[] even = { 2, 4 };
        double[] odd = { 1, 3, 5 };

        double[][] partition = partition(doublesOneToFive, λ(d, d % 2 == 0));

        assertArrayEquals(even, partition[0], 0.0);
        assertArrayEquals(odd, partition[1], 0.0);
    }
}