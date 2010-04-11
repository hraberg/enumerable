package lambda.enumerable.primitives;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.NoSuchElementException;

import lambda.enumerable.Enumerable;
import lambda.enumerable.collection.EList;
import lambda.enumerable.collection.ESet;
import lambda.primitives.*;
import static java.lang.System.*;

/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module</a>
 * </p>
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
    public static double[] collect(int[] array, Fn1ItoD block) {
        double[] result = new double[array.length];
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

    // /**
    // * Passes each entry in array to block. Returns the first for which block
    // is
    // * not false. If no object matches, it returns null.
    // */
    // public static <E> E detect(int[] array, Fn1<E, Boolean> block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    // /**
    // * Passes each entry in array to block. Returns the first for which block
    // is
    // * not false. If no object matches, it returns ifNone.
    // */
    // public static <E> E detect(int[] array, Fn1<E, Boolean> block, E ifNone)
    // {
    // return Enumerable.detect(asList(array), block);
    // }

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
    public static int[] each(int[] array, Fn1ItoI block) {
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

    // /**
    // * @see #detect(Iterable, Fn1)
    // */
    // public static <E> E find(int[] array, Fn1<E, Boolean> block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    // /**
    // * @see #detect(Iterable, Fn1, Object)
    // */
    // public static <E> E find(int[] array, Fn1<E, Boolean> block, E ifNone) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    /**
     * @see #select(int[], Fn1ItoB)
     */
    public static int[] findAll(int[] array, Fn1ItoB block) {
        return select(array, block);
    }

    /**
     * @see #member(int[], int)
     */
    public static boolean includes(int[] array, int i) {
        return member(array, i);
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
    public static int[] map(int[] array, Fn1ItoI block) {
        return collect(array, block);
    }

    /**
     * @see #collect(int[], Fn1ItoO)
     */
    public static double[] map(int[] array, Fn1ItoD block) {
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
     * Returns the object in array with the maximum value. This form assumes all
     * objects implement {@link Comparable}
     */
    public static int max(int[] array) {
        if (array.length == 0)
            throw new NoSuchElementException();
        int result = Integer.MIN_VALUE;
        for (int each : array)
            if (each > result)
                result = each;
        return result;
    }

    // /**
    // * Returns the object in array with the maximum value. This form uses the
    // * block to {@link Comparator#compare}.
    // */
    // public static <E> E max(int[] array, Fn2<E, E, Integer> block) {
    // return Enumerable.max(asList(array), block);
    // }
    //

    /**
     * Returns true if any member of array equals obj. Equality is tested using
     * {@link Object#equals(Object)}.
     */
    public static boolean member(int[] array, int i) {
        return Arrays.binarySearch(sort(array), i) >= 0;
    }

    /**
     * Returns the object in array with the minimum value. This form assumes all
     * objects implement {@link Comparable}.
     */
    public static int min(int[] array) {
        if (array.length == 0)
            throw new NoSuchElementException();
        int result = Integer.MAX_VALUE;
        for (int each : array)
            if (each < result)
                result = each;
        return result;
    }

    //
    // /**
    // * Returns the object in array with the minimum value. This form uses the
    // * block to {@link Comparator#compare}.
    // */
    // public static <E> E min(int[] array, Fn2<E, E, Integer> block) {
    // return Enumerable.min(asList(array), block);
    // }
    //
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
     * Constructs a range using the given start and end. The range will include
     * the end object.
     */
    public static int[] range(int start, int end) {
        return Enumerable.range(start, end).toArray();
    }

    /**
     * Constructs a range using the given start and end. If the third parameter
     * is false, the range will include the end object; otherwise, it will be
     * excluded.
     */
    public static int[] range(int start, int end, boolean exclusive) {
        return Enumerable.range(start, end, exclusive).toArray();
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

//     /**
//     * Returns an array containing the items in array sorted by using the
//     * results of the supplied block.
//     */
//     public static int[] sort(int[] array, Fn2IItoI block) {
//     }
    //
    // /**
    // * Sorts array using a set of keys generated by mapping the values in
    // array
    // * through the given block.
    // */
    // public static <E, R extends Object & Comparable<? super R>> int[]
    // sortBy(int[] array, final Fn1<E, R> block) {
    // return Enumerable.sortBy(asList(array),
    // block).toArray(newEmptyArray(array));
    // }
    //
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

    // /**
    // * Converts any arguments to iterators, then merges elements of array with
    // * corresponding elements from each argument. This generates a sequence of
    // * array#size n-element list, where n is one more that the count of
    // * arguments. If the size of any argument is less than array#length, null
    // * values are supplied.
    // *
    // * <p>
    // * Due to varargs this version doesn't support taking a block like in
    // Ruby.
    // * Feed the result into {@link #collect(Iterable, Fn1) to achieve the same
    // * effect.
    // * </p>
    // */
    // public static <E> Object[][] zip(int[] array, int[]... args) {
    // Iterable<?>[] lists = new Iterable<?>[args.length];
    // for (int i = 0; i < args.length; i++)
    // lists[i] = asList(args[i]);
    //
    // List<List<?>> zip = Enumerable.zip(asList(array), lists);
    //
    // Object[][] result = new Object[zip.size()][];
    // for (int i = 0; i < zip.size(); i++)
    // result[i] = zip.get(i).toArray();
    // return result;
    // }

    private static int[] copy(int[] array, int length) {
        int[] result = new int[length];
        arraycopy(array, 0, result, 0, length);
        return result;
    }
}
