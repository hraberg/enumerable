package org.enumerable.lambda.enumerable.primitives;

import static java.lang.System.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

import org.enumerable.lambda.enumerable.Enumerable;
import org.enumerable.lambda.enumerable.collection.EList;
import org.enumerable.lambda.enumerable.collection.ESet;
import org.enumerable.lambda.primitives.Fn1DtoB;
import org.enumerable.lambda.primitives.Fn1DtoD;
import org.enumerable.lambda.primitives.Fn1DtoI;
import org.enumerable.lambda.primitives.Fn1DtoL;
import org.enumerable.lambda.primitives.Fn1DtoO;
import org.enumerable.lambda.primitives.Fn2DDtoD;
import org.enumerable.lambda.primitives.Fn2DDtoO;


/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module in 1.8.6</a>
 * </p>
 */
public class EnumerableDoubles {
    // If changing this file, remember to run PrimitiveEnumerableGenerator.
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
    public static/* don't change */double[] collect(double[] array, Fn1DtoD block) {
        /* don't change */double[] result = new /* don't change */double[array.length];
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
    // * not false. If no value matches, it returns null.
    // */
    // public static <E> E detect(double[] array, Fn1DtoB block) {
    // return Enumerable.detect(asList(array), block);
    // }

    /**
     * Passes each entry in array to block. Returns the first for which block is
     * not false. If no value matches, it returns ifNone.
     */
    public static double detect(double[] array, double ifNone, Fn1DtoB block) {
        for (double each : array)
            if (block.call(each))
                return each;
        return ifNone;
    }

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
    public static <E> EList<Double> entries(double[] array) {
        return toList(array);
    }

    // /**
    // * @see #detect(double[], Fn1)
    // */
    // public static <E> E find(double[] array, Fn1DtoB block) {
    // return Enumerable.detect(asList(array), block);
    // }
    //

    /**
     * @see #detect(double[], double, Fn1DtoB)
     */
    public static double find(double[] array, double ifNone, Fn1DtoB block) {
        return detect(array, ifNone, block);
    }

    /**
     * @see #select(double[], Fn1DtoB)
     */
    public static double[] findAll(double[] array, Fn1DtoB block) {
        return select(array, block);
    }

    /**
     * Named parameter for detect.
     * 
     * @see #detect(double[], double, Fn1DtoB)
     * @see #find(double[], double, Fn1DtoB)
     */
    public static double ifNone(double defaultValue) {
        return defaultValue;
    }

    /**
     * @see #member(double[], double)
     */
    public static boolean include(double[] array, double value) {
        return member(array, value);
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
    public static/* don't change */double[] map(double[] array, Fn1DtoD block) {
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
     * Returns the maximum value in array.
     */
    public static double max(double[] array) {
        return min(array, new ReverseNaturalOrderDoubleComparator(new NaturalOrderPrimitiveComparator()));
    }

    /**
     * Returns the maximum value in array. This form uses the block to
     * {@link Comparator#compare}.
     */
    public static double max(double[] array, Fn2DDtoD block) {
        return min(array, new ReverseNaturalOrderDoubleComparator(new BlockDoubleComparator(block)));
    }

    /**
     * Returns true if any member of array equals value. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    public static boolean member(double[] array, double value) {
        return Arrays.binarySearch(sort(array), value) >= 0;
    }

    /**
     * Returns the minimum value in array.
     */
    public static double min(double[] array) {
        return min(array, new NaturalOrderPrimitiveComparator());
    }

    /**
     * Returns the minimum value in array. This form uses the block to
     * {@link Comparator#compare}.
     */
    public static double min(double[] array, Fn2DDtoD block) {
        return min(array, new BlockDoubleComparator(block));
    }

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

    // /**
    // * Returns an array containing the items in array sorted by using the
    // * results of the supplied block.
    // */
    // public static double[] sort(double[] array, Fn2DDtoD block) {
    // }
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
    public static EList<Double> toList(double[] array) {
        EList<Double> result = new EList<Double>(array.length);
        for (double each : array)
            result.add(each);
        return result;
    }

    /**
     * Creates a new Set containing the elements of the given array.
     */
    public static ESet<Double> toSet(double[] array) {
        return Enumerable.toSet(toList(array));
    }

    /**
     * Creates a new Set containing the elements of the given array, the
     * elements are preprocessed by the given block.
     */
    public static <R> ESet<R> toSet(double[] array, Fn1DtoO<R> block) {
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

    static double min(double[] array, DoubleComparator comparator) {
        double result = array[0];
        for (int i = 1; i < array.length; i++) {
            double each = array[i];
            if (comparator.compare(each, result) < 0)
                result = each;
        }
        return result;
    }

    static interface DoubleComparator {
        int compare(double a, double b);
    }

    static class NaturalOrderPrimitiveComparator implements DoubleComparator {
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

    static class BlockDoubleComparator implements DoubleComparator {
        Fn2DDtoD block;

        BlockDoubleComparator(Fn2DDtoD block) {
            this.block = block;
        }

        public int compare(double a, double b) {
            return (int) block.call(a, b);
        }
    }

    static class ReverseNaturalOrderDoubleComparator implements DoubleComparator {
        DoubleComparator comparator;

        ReverseNaturalOrderDoubleComparator(DoubleComparator comparator) {
            this.comparator = comparator;
        }

        public int compare(double a, double b) {
            return -comparator.compare(a, b);
        }
    }

    static double[] copy(double[] array, int length) {
        double[] result = new double[length];
        arraycopy(array, 0, result, 0, length);
        return result;
    }
}
