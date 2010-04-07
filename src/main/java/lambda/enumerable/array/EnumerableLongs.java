package lambda.enumerable.array;

import java.lang.reflect.Array;
import java.util.*;

import lambda.Fn1;
import lambda.enumerable.Enumerable;
import lambda.primitives.*;

/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module</a>
 * </p>
 */
public class EnumerableLongs {
    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block never returns false.
     */
    public static <E> boolean all(long[] array, Fn1LtoB block) {
        for (long each : array)
            if (!block.call(each))
                return false;
        return true;
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block ever returns a value other than false.
     */
    public static <E> boolean any(long[] array, Fn1LtoB block) {
        for (long each : array)
            if (block.call(each))
                return true;
        return false;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    public static <R> Object[] collect(long[] array, Fn1LtoO<R> block) {
        Object[] result = new Object[array.length];
        int i = 0;
        for (long each : array)
            result[i++] = block.call(each);
        return result;
    }

    public static long[] collect(long[] array, Fn1LtoL block) {
        long[] result = new long[array.length];
        int i = 0;
        for (long each : array)
            result[i++] = block.call(each);
        return result;
    }

    public static double[] collect(long[] array, Fn1LtoD block) {
        double[] result = new double[array.length];
        int i = 0;
        for (long each : array)
            result[i++] = block.call(each);
        return result;
    }

    public static int[] collect(long[] array, Fn1LtoI block) {
        int[] result = new int[array.length];
        int i = 0;
        for (long each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    @SuppressWarnings("unchecked")
    public static <R> R[] collect(long[] array, Fn1LtoO<R> block, Class<R> type) {
        R[] result = (R[]) Array.newInstance(type, array.length);
        int i = 0;
        for (long each : array)
            result[i++] = block.call(each);
        return result;
    }

    // /**
    // * Passes each entry in array to block. Returns the first for which block
    // is
    // * not false. If no object matches, it returns null.
    // */
    // public static <E> E detect(long[] array, Fn1<E, Boolean> block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    // /**
    // * Passes each entry in array to block. Returns the first for which block
    // is
    // * not false. If no object matches, it returns ifNone.
    // */
    // public static <E> E detect(long[] array, Fn1<E, Boolean> block, E ifNone)
    // {
    // return Enumerable.detect(asList(array), block);
    // }

    /**
     * Calls block for each item in array.
     */
    public static <R> long[] each(long[] array, Fn1LtoO<R> block) {
        for (long each : array)
            block.call(each);
        return array;
    }

    public static long[] each(long[] array, Fn1LtoI block) {
        for (long each : array)
            block.call(each);
        return array;
    }

    public static long[] each(long[] array, Fn1LtoD block) {
        for (long each : array)
            block.call(each);
        return array;
    }

    public static long[] each(long[] array, Fn1LtoL block) {
        for (long each : array)
            block.call(each);
        return array;
    }

    public static long[] each(long[] array, Fn1LtoB block) {
        for (long each : array)
            block.call(each);
        return array;
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * array.
     */
    public static <R> long[] eachWithIndex(long[] array, Fn2LLtoO<R> block) {
        int idx = 0;
        for (long each : array)
            block.call(each, idx++);
        return array;
    }

    public static long[] eachWithIndex(long[] array, Fn2LLtoL block) {
        int idx = 0;
        for (long each : array)
            block.call(each, idx++);
        return array;
    }

    /**
     * @see #toList(Iterable)
     */
    public static <E> List<Long> entries(long[] array) {
        return toList(array);
    }

    // /**
    // * @see #detect(Iterable, Fn1)
    // */
    // public static <E> E find(long[] array, Fn1<E, Boolean> block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    // /**
    // * @see #detect(Iterable, Fn1, Object)
    // */
    // public static <E> E find(long[] array, Fn1<E, Boolean> block, E ifNone) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    /**
     * @see #select(Iterable, Fn1)
     */
    public static long[] findAll(long[] array, Fn1LtoB block) {
        return select(array, block);
    }

    /**
     * @see #member(Iterable, Object)
     */
    public static boolean includes(long[] array, int i) {
        return member(array, i);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form uses the first element of the
     * array as a the initial value (and skips that element while iterating).
     */
    public static long inject(long[] array, Fn2LLtoL block) {
        long initial = array[0];
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
    public static long inject(long[] array, long initial, Fn2LLtoL block) {
        for (long each : array)
            initial = block.call(initial, each);
        return initial;
    }

    /**
     * @see #collect(Iterable, Fn1)
     */
    public static <R> Object[] map(long[] array, Fn1LtoO<R> block) {
        return collect(array, block);
    }

    public static long[] map(long[] array, Fn1LtoL block) {
        return collect(array, block);
    }

    public static double[] map(long[] array, Fn1LtoD block) {
        return collect(array, block);
    }
    
    public static int[] map(long[] array, Fn1LtoI block) {
        return collect(array, block);
    }

    public static <R> R[] map(long[] array, Fn1LtoO<R> block, Class<R> type) {
        return collect(array, block, type);
    }

    /**
     * Returns the object in array with the maximum value. This form assumes all
     * objects implement {@link Comparable}
     */
    public static long max(long[] array) {
        if (array.length == 0)
            return 0;
        long[] result = sort(array);
        return result[result.length - 1];
    }

    // /**
    // * Returns the object in array with the maximum value. This form uses the
    // * block to {@link Comparator#compare}.
    // */
    // public static <E> E max(long[] array, Fn2<E, E, Integer> block) {
    // return Enumerable.max(asList(array), block);
    // }
    //

    /**
     * Returns true if any member of array equals obj. Equality is tested using
     * {@link Object#equals(Object)}.
     */
    public static boolean member(long[] array, int i) {
        return Arrays.binarySearch(sort(array), i) >= 0;
    }

    /**
     * Returns the object in array with the minimum value. This form assumes all
     * objects implement {@link Comparable}.
     */
    public static long min(long[] array) {
        if (array.length == 0)
            return 0;
        return sort(array)[0];
    }

    //
    // /**
    // * Returns the object in array with the minimum value. This form uses the
    // * block to {@link Comparator#compare}.
    // */
    // public static <E> E min(long[] array, Fn2<E, E, Integer> block) {
    // return Enumerable.min(asList(array), block);
    // }
    //
    /**
     * Returns two lists, the first containing the elements of array for which
     * the block evaluates to true, the second containing the rest.
     */
    public static long[][] partition(long[] array, Fn1LtoB block) {
        long[][] result = new long[2][];

        result[0] = select(array, block);
        result[1] = reject(array, block);

        return result;
    }

    /**
     * Returns an array containing all elements of array for which block is
     * false.
     */
    public static long[] reject(long[] array, Fn1LtoB block) {
        return selectOrReject(array, block, false);
    }

    /**
     * Returns an array containing all elements of array for which block is not
     * false.
     */
    public static long[] select(long[] array, Fn1LtoB block) {
        return selectOrReject(array, block, true);
    }

    private static long[] selectOrReject(long[] array, Fn1LtoB block, boolean select) {
        long[] result = new long[array.length];
        int i = 0;
        for (long each : array)
            if (block.call(each) == select)
                result[i++] = each;
        return copy(result, i);
    }

    /**
     * Returns an array containing the items in array sorted, according to their
     * own compareTo method.
     */
    public static long[] sort(long[] array) {
        long[] result = copy(array, array.length);
        Arrays.sort(result);
        return result;
    }

//     /**
//     * Returns an array containing the items in array sorted by using the
//     * results of the supplied block.
//     */
//     public static long[] sort(long[] array, Fn2IItoI block) {
//     }
    //
    // /**
    // * Sorts array using a set of keys generated by mapping the values in
    // array
    // * through the given block.
    // */
    // public static <E, R extends Object & Comparable<? super R>> long[]
    // sortBy(long[] array, final Fn1<E, R> block) {
    // return Enumerable.sortBy(asList(array),
    // block).toArray(newEmptyArray(array));
    // }
    //
    /**
     * Returns a list containing the items in array.
     */
    public static List<Long> toList(long[] array) {
        List<Long> result = new ArrayList<Long>(array.length);
        for (long each : array)
            result.add(each);
        return result;
    }

    /**
     * Creates a new Set containing the elements of the given array.
     */
    public static Set<Long> toSet(long[] array) {
        return Enumerable.toSet(toList(array));
    }

    /**
     * Creates a new Set containing the elements of the given array, the
     * elements are preprocessed by the given block.
     */
    public static <R> Set<R> toSet(long[] array, Fn1LtoO<R> block) {
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
    // public static <E> Object[][] zip(long[] array, long[]... args) {
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

    private static long[] copy(long[] array, int length) {
        long[] result = new long[length];
        System.arraycopy(array, 0, result, 0, length);
        return result;
    }
}
