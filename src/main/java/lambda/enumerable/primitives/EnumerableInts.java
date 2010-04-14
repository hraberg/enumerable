package lambda.enumerable.primitives;

import static java.lang.System.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

import lambda.enumerable.Enumerable;
import lambda.enumerable.collection.EList;
import lambda.enumerable.collection.ESet;
import lambda.primitives.Fn1ItoB;
import lambda.primitives.Fn1ItoD;
import lambda.primitives.Fn1ItoI;
import lambda.primitives.Fn1ItoL;
import lambda.primitives.Fn1ItoO;
import lambda.primitives.Fn2IItoI;
import lambda.primitives.Fn2IItoO;

/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http: * module in 1.8.6</a>
 * </p>
 * <i>This file was generated by
 * lambda.enumerable.primitives.PrimitiveEnumerableGenerator from
 * EnumerableDoubles.java.</i>
 */
public class EnumerableInts {
    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block never returns false.
     */
    public static <E> boolean all(int[] array, Fn1ItoB block) {
        for (int each : array)
            if (!block.call(each))
                return false;
        return true;
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block ever returns a value other than false.
     */
    public static <E> boolean any(int[] array, Fn1ItoB block) {
        for (int each : array)
            if (block.call(each))
                return true;
        return false;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    public static <R> Object[] collect(int[] array, Fn1ItoO<R> block) {
        Object[] result = new Object[array.length];
        int i = 0;
        for (int each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static/* don't change */double[] collect(int[] array, Fn1ItoD block) {
        /* don't change */double[] result = new /* don't change */double[array.length];
        int i = 0;
        for (int each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static int[] collect(int[] array, Fn1ItoI block) {
        int[] result = new int[array.length];
        int i = 0;
        for (int each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static long[] collect(int[] array, Fn1ItoL block) {
        long[] result = new long[array.length];
        int i = 0;
        for (int each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    @SuppressWarnings("unchecked")
    public static <R> R[] collect(int[] array, Fn1ItoO<R> block, Class<R> type) {
        R[] result = (R[]) Array.newInstance(type, array.length);
        int i = 0;
        for (int each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * Passes each entry in array to block. Returns the first for which block is
     * not false. If no object matches, it returns ifNone.
     */
    public static int detect(int[] array, int ifNone, Fn1ItoB block) {
        for (int each : array)
            if (block.call(each))
                return each;
        return ifNone;
    }

    /**
     * Calls block for each item in array.
     */
    public static <R> int[] each(int[] array, Fn1ItoO<R> block) {
        for (int each : array)
            block.call(each);
        return array;
    }

    /**
     * @see #each(int[], Fn1ItoO)
     */
    public static int[] each(int[] array, Fn1ItoD block) {
        for (int each : array)
            block.call(each);
        return array;
    }

    /**
     * @see #each(int[], Fn1ItoO)
     */
    public static int[] each(int[] array, Fn1ItoI block) {
        for (int each : array)
            block.call(each);
        return array;
    }

    /**
     * @see #each(int[], Fn1ItoO)
     */
    public static int[] each(int[] array, Fn1ItoL block) {
        for (int each : array)
            block.call(each);
        return array;
    }

    /**
     * @see #each(int[], Fn1ItoO)
     */
    public static int[] each(int[] array, Fn1ItoB block) {
        for (int each : array)
            block.call(each);
        return array;
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * array.
     */
    public static <R> int[] eachWithIndex(int[] array, Fn2IItoO<R> block) {
        int idx = 0;
        for (int each : array)
            block.call(each, idx++);
        return array;
    }

    /**
     * @see #eachWithIndex(int[], Fn2IItoO)
     */
    public static int[] eachWithIndex(int[] array, Fn2IItoI block) {
        int idx = 0;
        for (int each : array)
            block.call(each, idx++);
        return array;
    }

    /**
     * @see #toList(int[])
     */
    public static <E> EList<Integer> entries(int[] array) {
        return toList(array);
    }

    /**
     * @see #detect(int[], int, Fn1ItoB)
     */
    public static int find(int[] array, int ifNone, Fn1ItoB block) {
        return detect(array, ifNone, block);
    }

    /**
     * @see #select(int[], Fn1ItoB)
     */
    public static int[] findAll(int[] array, Fn1ItoB block) {
        return select(array, block);
    }

    /**
     * Named parameter for detect.
     * 
     * @see #detect(int[], int, Fn1ItoB)
     * @see #find(int[], int, Fn1ItoB)
     */
    public static int ifNone(int defaultValue) {
        return defaultValue;
    }

    /**
     * @see #member(int[], int)
     */
    public static boolean includes(int[] array, int value) {
        return member(array, value);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form uses the first element of the
     * array as a the initial value (and skips that element while iterating).
     */
    public static int inject(int[] array, Fn2IItoI block) {
        int initial = array[0];
        for (int i = 1; i < array.length; i++)
            initial = block.call(initial, array[i]);
        return initial;
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form lets you supply an initial value
     * for memo.
     */
    public static int inject(int[] array, int initial, Fn2IItoI block) {
        for (int each : array)
            initial = block.call(initial, each);
        return initial;
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static <R> Object[] map(int[] array, Fn1ItoO<R> block) {
        return collect(array, block);
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static/* don't change */double[] map(int[] array, Fn1ItoD block) {
        return collect(array, block);
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static int[] map(int[] array, Fn1ItoI block) {
        return collect(array, block);
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static long[] map(int[] array, Fn1ItoL block) {
        return collect(array, block);
    }

    /**
     * @see #collect(int[], Fn1ItoO, Class)
     */
    public static <R> R[] map(int[] array, Fn1ItoO<R> block, Class<R> type) {
        return collect(array, block, type);
    }

    /**
     * Returns the int in array with the maximum value.
     */
    public static int max(int[] array) {
        return min(array, new ReverseNaturalOrderIntegerComparator(new NaturalOrderPrimitiveComparator()));
    }

    /**
     * Returns the int in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static int max(int[] array, Fn2IItoI block) {
        return min(array, new ReverseNaturalOrderIntegerComparator(new BlockIntegerComparator(block)));
    }

    /**
     * Returns true if any member of array equals value. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    public static boolean member(int[] array, int value) {
        return Arrays.binarySearch(sort(array), value) >= 0;
    }

    /**
     * Returns the int in array with the minimum value.
     */
    public static int min(int[] array) {
        return min(array, new NaturalOrderPrimitiveComparator());
    }

    /**
     * Returns the int in collection with the minimum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static int min(int[] array, Fn2IItoI block) {
        return min(array, new BlockIntegerComparator(block));
    }

    /**
     * Returns two lists, the first containing the elements of array for which
     * the block evaluates to true, the second containing the rest.
     */
    public static int[][] partition(int[] array, Fn1ItoB block) {
        int[][] result = new int[2][];

        result[0] = select(array, block);
        result[1] = reject(array, block);

        return result;
    }

    /**
     * Returns an array containing all elements of array for which block is
     * false.
     */
    public static int[] reject(int[] array, Fn1ItoB block) {
        return selectOrReject(array, block, false);
    }

    /**
     * Returns an array containing all elements of array for which block is not
     * false.
     */
    public static int[] select(int[] array, Fn1ItoB block) {
        return selectOrReject(array, block, true);
    }

    private static int[] selectOrReject(int[] array, Fn1ItoB block, boolean select) {
        int[] result = new int[array.length];
        int i = 0;
        for (int each : array)
            if (block.call(each) == select)
                result[i++] = each;
        return copy(result, i);
    }

    /**
     * Returns an array containing the items in array sorted, according to their
     * own compareTo method.
     */
    public static int[] sort(int[] array) {
        int[] result = copy(array, array.length);
        Arrays.sort(result);
        return result;
    }

    /**
     * Returns a list containing the items in array.
     */
    public static EList<Integer> toList(int[] array) {
        EList<Integer> result = new EList<Integer>(array.length);
        for (int each : array)
            result.add(each);
        return result;
    }

    /**
     * Creates a new Set containing the elements of the given array.
     */
    public static ESet<Integer> toSet(int[] array) {
        return Enumerable.toSet(toList(array));
    }

    /**
     * Creates a new Set containing the elements of the given array, the
     * elements are preprocessed by the given block.
     */
    public static <R> ESet<R> toSet(int[] array, Fn1ItoO<R> block) {
        return Enumerable.toSet(toList(array), block);
    }

    static int min(int[] array, IntegerComparator comparator) {
        int result = array[0];
        for (int i = 1; i < array.length; i++) {
            int each = array[i];
            if (comparator.compare(each, result) < 0)
                result = each;
        }
        return result;
    }

    static interface IntegerComparator {
        int compare(int a, int b);
    }

    static class NaturalOrderPrimitiveComparator implements IntegerComparator {
        public int compare(/* don't change */double a, /* don't change */double b) {
            return /* don't change */Double.compare(a, b);
        }

        public int compare(long a, long b) {
            return (a < b) ? -1 : ((a > b) ? 1 : 0);
        }

        public int compare(int a, int b) {
            return (a < b) ? -1 : ((a > b) ? 1 : 0);
        }
    }

    static class BlockIntegerComparator implements IntegerComparator {
        Fn2IItoI block;

        BlockIntegerComparator(Fn2IItoI block) {
            this.block = block;
        }

        public int compare(int a, int b) {
            return (int) block.call(a, b);
        }
    }

    static class ReverseNaturalOrderIntegerComparator implements IntegerComparator {
        IntegerComparator comparator;

        ReverseNaturalOrderIntegerComparator(IntegerComparator comparator) {
            this.comparator = comparator;
        }

        public int compare(int a, int b) {
            return -comparator.compare(a, b);
        }
    }

    static int[] copy(int[] array, int length) {
        int[] result = new int[length];
        arraycopy(array, 0, result, 0, length);
        return result;
    }
}
