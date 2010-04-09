package lambda.enumerable.primitives;

import java.lang.reflect.Array;
import java.util.*;

import static java.lang.System.*;

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
public class EnumerableDoubles {
    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block never returns false.
     */
    public static <E> boolean all(double[] array, Fn1DtoB block) {
        for (double each : array)
            if (!block.call(each))
                return false;
        return true;
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block ever returns a value other than false.
     */
    public static <E> boolean any(double[] array, Fn1DtoB block) {
        for (double each : array)
            if (block.call(each))
                return true;
        return false;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    public static <R> Object[] collect(double[] array, Fn1DtoO<R> block) {
        Object[] result = new Object[array.length];
        int i = 0;
        for (double each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static double[] collect(double[] array, Fn1DtoD block) {
        double[] result = new double[array.length];
        int i = 0;
        for (double each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static int[] collect(double[] array, Fn1DtoI block) {
        int[] result = new int[array.length];
        int i = 0;
        for (double each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static long[] collect(double[] array, Fn1DtoL block) {
        long[] result = new long[array.length];
        int i = 0;
        for (double each : array)
            result[i++] = block.call(each);
        return result;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    @SuppressWarnings("unchecked")
    public static <R> R[] collect(double[] array, Fn1DtoO<R> block, Class<R> type) {
        R[] result = (R[]) Array.newInstance(type, array.length);
        int i = 0;
        for (double each : array)
            result[i++] = block.call(each);
        return result;
    }

    // /**
    // * Passes each entry in array to block. Returns the first for which block
    // is
    // * not false. If no object matches, it returns null.
    // */
    // public static <E> E detect(double[] array, Fn1<E, Boolean> block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    // /**
    // * Passes each entry in array to block. Returns the first for which block
    // is
    // * not false. If no object matches, it returns ifNone.
    // */
    // public static <E> E detect(double[] array, Fn1<E, Boolean> block, E ifNone)
    // {
    // return Enumerable.detect(asList(array), block);
    // }

    /**
     * Calls block for each item in array.
     */
    public static <R> double[] each(double[] array, Fn1DtoO<R> block) {
        for (double each : array)
            block.call(each);
        return array;
    }

    /**
     * @see #each(double[], Fn1DtoO)
     */
    public static double[] each(double[] array, Fn1DtoD block) {
        for (double each : array)
            block.call(each);
        return array;
    }

    /**
     * @see #each(double[], Fn1DtoO)
     */
    public static double[] each(double[] array, Fn1DtoI block) {
        for (double each : array)
            block.call(each);
        return array;
    }
    
    /**
     * @see #each(double[], Fn1DtoO)
     */
    public static double[] each(double[] array, Fn1DtoL block) {
        for (double each : array)
            block.call(each);
        return array;
    }
    
    /**
     * @see #each(double[], Fn1DtoO)
     */
    public static double[] each(double[] array, Fn1DtoB block) {
        for (double each : array)
            block.call(each);
        return array;
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * array.
     */
    public static <R> double[] eachWithIndex(double[] array, Fn2DDtoO<R> block) {
        double idx = 0;
        for (double each : array)
            block.call(each, idx++);
        return array;
    }

    /**
     * @see #eachWithIndex(double[], Fn2DDtoO)
     */
    public static double[] eachWithIndex(double[] array, Fn2DDtoD block) {
        double idx = 0;
        for (double each : array)
            block.call(each, idx++);
        return array;
    }

    /**
     * @see #toList(double[])
     */
    public static <E> List<Double> entries(double[] array) {
        return toList(array);
    }

    // /**
    // * @see #detect(double[], Fn1)
    // */
    // public static <E> E find(double[] array, Fn1<E, Boolean> block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    // /**
    // * @see #detect(double[], Fn1, Object)
    // */
    // public static <E> E find(double[] array, Fn1<E, Boolean> block, E ifNone) {
    // return Enumerable.detect(asList(array), block);
    // }
    //
    /**
     * @see #select(double[], Fn1DtoB)
     */
    public static double[] findAll(double[] array, Fn1DtoB block) {
        return select(array, block);
    }

    /**
     * @see #member(double[], double)
     */
    public static boolean includes(double[] array, double d) {
        return member(array, d);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form uses the first element of the
     * array as a the initial value (and skips that element while iterating).
     */
    public static double inject(double[] array, Fn2DDtoD block) {
        double initial = array[0];
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
    public static double inject(double[] array, double initial, Fn2DDtoD block) {
        for (double each : array)
            initial = block.call(initial, each);
        return initial;
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static <R> Object[] map(double[] array, Fn1DtoO<R> block) {
        return collect(array, block);
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static double[] map(double[] array, Fn1DtoD block) {
        return collect(array, block);
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static int[] map(double[] array, Fn1DtoI block) {
        return collect(array, block);
    }

    /**
     * @see #collect(double[], Fn1DtoO)
     */
    public static long[] map(double[] array, Fn1DtoL block) {
        return collect(array, block);
    }

    /**
     * @see #collect(double[], Fn1DtoO, Class)
     */
    public static <R> R[] map(double[] array, Fn1DtoO<R> block, Class<R> type) {
        return collect(array, block, type);
    }
    
    /**
     * Returns the object in array with the maximum value. This form assumes all
     * objects implement {@link Comparable}
     */
    public static double max(double[] array) {
        if (array.length == 0)
            return 0;
        double[] result = sort(array);
        return result[result.length - 1];
    }

    // /**
    // * Returns the object in array with the maximum value. This form uses the
    // * block to {@link Comparator#compare}.
    // */
    // public static <E> E max(double[] array, Fn2<E, E, doubleeger> block) {
    // return Enumerable.max(asList(array), block);
    // }
    //

    /**
     * Returns true if any member of array equals obj. Equality is tested using
     * {@link Object#equals(Object)}.
     */
    public static boolean member(double[] array, double d) {
        return Arrays.binarySearch(sort(array), d) >= 0;
    }

    /**
     * Returns the object in array with the minimum value. This form assumes all
     * objects implement {@link Comparable}.
     */
    public static double min(double[] array) {
        if (array.length == 0)
            return 0;
        return sort(array)[0];
    }

    //
    // /**
    // * Returns the object in array with the minimum value. This form uses the
    // * block to {@link Comparator#compare}.
    // */
    // public static <E> E min(double[] array, Fn2<E, E, doubleeger> block) {
    // return Enumerable.min(asList(array), block);
    // }
    //
    /**
     * Returns two lists, the first containing the elements of array for which
     * the block evaluates to true, the second containing the rest.
     */
    public static double[][] partition(double[] array, Fn1DtoB block) {
        double[][] result = new double[2][];

        result[0] = select(array, block);
        result[1] = reject(array, block);

        return result;
    }

    /**
     * Returns an array containing all elements of array for which block is
     * false.
     */
    public static double[] reject(double[] array, Fn1DtoB block) {
        return selectOrReject(array, block, false);
    }

    /**
     * Returns an array containing all elements of array for which block is not
     * false.
     */
    public static double[] select(double[] array, Fn1DtoB block) {
        return selectOrReject(array, block, true);
    }

    private static double[] selectOrReject(double[] array, Fn1DtoB block, boolean select) {
        double[] result = new double[array.length];
        int i = 0;
        for (double each : array)
            if (block.call(each) == select)
                result[i++] = each;
        return copy(result, i);
    }

    /**
     * Returns an array containing the items in array sorted, according to their
     * own compareTo method.
     */
    public static double[] sort(double[] array) {
        double[] result = copy(array, array.length);
        Arrays.sort(result);
        return result;
    }

//     /**
//     * Returns an array containing the items in array sorted by using the
//     * results of the supplied block.
//     */
//     public static double[] sort(double[] array, Fn2IItoI block) {
//     }
    //
    // /**
    // * Sorts array using a set of keys generated by mapping the values in
    // array
    // * through the given block.
    // */
    // public static <E, R extends Object & Comparable<? super R>> double[]
    // sortBy(double[] array, final Fn1<E, R> block) {
    // return Enumerable.sortBy(asList(array),
    // block).toArray(newEmptyArray(array));
    // }
    //
    /**
     * Returns a list containing the items in array.
     */
    public static List<Double> toList(double[] array) {
        List<Double> result = new ArrayList<Double>(array.length);
        for (double each : array)
            result.add(each);
        return result;
    }

    /**
     * Creates a new Set containing the elements of the given array.
     */
    public static Set<Double> toSet(double[] array) {
        return Enumerable.toSet(toList(array));
    }

    /**
     * Creates a new Set containing the elements of the given array, the
     * elements are preprocessed by the given block.
     */
    public static <R> Set<R> toSet(double[] array, Fn1DtoO<R> block) {
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
    // * Feed the result into {@link #collect(double[], Fn1) to achieve the same
    // * effect.
    // * </p>
    // */
    // public static <E> Object[][] zip(double[] array, double[]... args) {
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

    private static double[] copy(double[] array, int length) {
        double[] result = new double[length];
        arraycopy(array, 0, result, 0, length);
        return result;
    }
}
