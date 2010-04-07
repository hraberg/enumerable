package lambda.enumerable.array;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;
import lambda.enumerable.collection.EList;
import lambda.enumerable.collection.ESet;
import static java.util.Arrays.*;

/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module</a>
 * </p>
 */
public class EnumerableArrays {
    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block never returns false or null.
     */
    public static <E> boolean all(E[] array, Fn1<E, ?> block) {
        return Enumerable.all(asList(array), block);
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block ever returns a value other than false or null.
     */
    public static <E> boolean any(E[] array, Fn1<E, ?> block) {
        return Enumerable.any(asList(array), block);
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    public static <E, R> Object[] collect(E[] array, Fn1<E, R> block) {
        return Enumerable.collect(asList(array), block).toArray();
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array. Takes an extra type parameter to handle empty arrays.
     */
    @SuppressWarnings("unchecked")
    public static <E, R> R[] collect(E[] array, Fn1<E, R> block, Class<R> type) {
        return Enumerable.collect(asList(array), block).toArray((R[]) Array.newInstance(type, 0));
    }

    /**
     * Passes each entry in array to block. Returns the first for which block is
     * not false. If no object matches, it returns null.
     */
    public static <E> E detect(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.detect(asList(array), block);
    }

    /**
     * Passes each entry in array to block. Returns the first for which block is
     * not false. If no object matches, it returns ifNone.
     */
    public static <E> E detect(E[] array, Fn1<E, Boolean> block, E ifNone) {
        return Enumerable.detect(asList(array), block);
    }

    /**
     * Calls block for each item in array.
     */
    public static <E, R> E[] each(E[] array, Fn1<E, R> block) {
        return Enumerable.toList(Enumerable.each(asList(array), block)).toArray(newEmptyArray(array));
    }

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    public static <E, R> Object eachCons(E[] array, int n, Fn1<List<E>, R> block) {
        return Enumerable.eachCons(asList(array), n, block);
    }

    /**
     * Iterates the given block for each slice of n elements.
     */
    public static <E, R> Object eachSlice(E[] array, int n, Fn1<List<E>, R> block) {
        return Enumerable.eachSlice(asList(array), n, block);
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * array.
     */
    public static <E, R> E[] eachWithIndex(E[] array, Fn2<E, Integer, R> block) {
        return Enumerable.toList(Enumerable.eachWithIndex(asList(array), block)).toArray(newEmptyArray(array));
    }

    /**
     * @see #toList(Iterable)
     */
    public static <E> E[] entries(E[] array) {
        return Enumerable.toList(asList(array)).toArray(newEmptyArray(array));
    }

    /**
     * @see #detect(Iterable, Fn1)
     */
    public static <E> E find(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.detect(asList(array), block);
    }

    /**
     * @see #detect(Iterable, Fn1, Object)
     */
    public static <E> E find(E[] array, Fn1<E, Boolean> block, E ifNone) {
        return Enumerable.detect(asList(array), block);
    }

    /**
     * @see #select(Iterable, Fn1)
     */
    public static <E> E[] findAll(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.findAll(asList(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array of every element in array for which pattern matches.
     */
    public static <E> E[] grep(E[] array, Pattern pattern) {
        return Enumerable.grep(asList(array), pattern).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array of every element in array for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    public static <E, R> Object[] grep(E[] array, Pattern pattern, Fn1<E, R> block) {
        return Enumerable.grep(asList(array), pattern, block).toArray();
    }

    /**
     * @see #grep(Iterable, Pattern)
     */
    public static <E> E[] grep(E[] array, String pattern) {
        return Enumerable.grep(asList(array), pattern).toArray(newEmptyArray(array));
    }

    /**
     * @see #grep(Iterable, Pattern, Fn1)
     */
    public static <E, R> Object[] grep(E[] array, String pattern, Fn1<E, R> block) {
        return Enumerable.grep(asList(array), pattern, block).toArray();
    }

    /**
     * @see #member(Iterable, Object)
     */
    public static <E> boolean includes(E[] array, Object obj) {
        return Enumerable.includes(asList(array), obj);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form uses the first element of the
     * array as a the initial value (and skips that element while iterating).
     */
    public static <E> E inject(E[] array, Fn2<E, E, E> block) {
        return Enumerable.inject(asList(array), block);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form lets you supply an initial value
     * for memo.
     */
    public static <E, R> R inject(E[] array, R initial, Fn2<R, E, R> block) {
        return Enumerable.inject(asList(array), initial, block);
    }

    /**
     * @see #collect(Iterable, Fn1)
     */
    public static <E, R> Object[] map(E[] array, Fn1<E, R> block) {
        return Enumerable.map(asList(array), block).toArray();
    }

    /**
     * @see #collect(Iterable, Fn1)
     */
    public static <E, R> R[] map(E[] array, Fn1<E, R> block, Class<R> type) {
        return collect(array, block, type);
    }

    /**
     * Returns the object in array with the maximum value. This form assumes all
     * objects implement {@link Comparable}
     */
    public static <E extends Object & Comparable<? super E>> E max(E[] array) {
        return Enumerable.max(asList(array));
    }

    /**
     * Returns the object in array with the maximum value. This form uses the
     * block to {@link Comparator#compare}.
     */
    public static <E> E max(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.max(asList(array), block);
    }

    /**
     * Returns true if any member of array equals obj. Equality is tested using
     * {@link Object#equals(Object)}.
     */
    public static <E> boolean member(E[] array, Object obj) {
        return Enumerable.member(asList(array), obj);
    }

    /**
     * Returns the object in array with the minimum value. This form assumes all
     * objects implement {@link Comparable}.
     */
    public static <E extends Object & Comparable<? super E>> E min(E[] array) {
        return Enumerable.min(asList(array));
    }

    /**
     * Returns the object in array with the minimum value. This form uses the
     * block to {@link Comparator#compare}.
     */
    public static <E> E min(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.min(asList(array), block);
    }

    /**
     * Returns two lists, the first containing the elements of array for which
     * the block evaluates to true, the second containing the rest.
     */
    @SuppressWarnings("unchecked")
    public static <E> E[][] partition(E[] array, Fn1<E, Boolean> block) {
        List<EList<E>> partition = Enumerable.partition(asList(array), block);

        E[][] result = (E[][]) Array.newInstance(array.getClass(), 2);

        result[0] = partition.get(0).toArray(newEmptyArray(array));
        result[1] = partition.get(1).toArray(newEmptyArray(array));

        return result;
    }

    /**
     * Constructs a range using the given start and end. The range will include
     * the end object.
     */
    public static Integer[] range(int start, int end) {
        return Enumerable.range(start, end).toArray();
    }

    /**
     * Constructs a range using the given start and end. If the third parameter
     * is false, the range will include the end object; otherwise, it will be
     * excluded.
     */
    public static Integer[] range(int start, int end, boolean exclusive) {
        return Enumerable.range(start, end, exclusive).toArray();
    }
    
    /**
     * Returns an array containing all elements of array for which block is
     * false.
     */
    public static <E> E[] reject(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.reject(asList(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array containing all elements of array for which block is not
     * false.
     */
    public static <E> E[] select(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.select(asList(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array containing the items in array sorted, according to their
     * own compareTo method.
     */
    public static <E extends Object & Comparable<? super E>> E[] sort(E[] array) {
        return Enumerable.sort(asList(array)).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array containing the items in array sorted by using the
     * results of the supplied block.
     */
    public static <E> E[] sort(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.sort(asList(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Sorts array using a set of keys generated by mapping the values in array
     * through the given block.
     */
    public static <E, R extends Object & Comparable<? super R>> E[] sortBy(E[] array, final Fn1<E, R> block) {
        return Enumerable.sortBy(asList(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns a list containing the items in array.
     */
    public static <E> EList<E> toList(E[] array) {
        return Enumerable.toList(asList(array));
    }

    /**
     * Creates a new Set containing the elements of the given array.
     */
    public static <E> ESet<E> toSet(E[] array) {
        return Enumerable.toSet(asList(array));
    }

    /**
     * Creates a new Set containing the elements of the given array, the
     * elements are preprocessed by the given block.
     */
    public static <E, R> ESet<R> toSet(E[] array, Fn1<E, R> block) {
        return Enumerable.toSet(asList(array), block);
    }

    /**
     * Converts any arguments to iterators, then merges elements of array with
     * corresponding elements from each argument. This generates a sequence of
     * array#size n-element list, where n is one more that the count of
     * arguments. If the size of any argument is less than array#length, null
     * values are supplied.
     * 
     * <p>
     * Due to varargs this version doesn't support taking a block like in Ruby.
     * Feed the result into {@link #collect(Iterable, Fn1) to achieve the same
     * effect.
     * </p>
     */
    public static <E> Object[][] zip(E[] array, Object[]... args) {
        Iterable<?>[] lists = new Iterable<?>[args.length];
        for (int i = 0; i < args.length; i++)
            lists[i] = asList(args[i]);

        List<EList<?>> zip = Enumerable.zip(asList(array), lists);

        Object[][] result = new Object[zip.size()][];
        for (int i = 0; i < zip.size(); i++)
            result[i] = zip.get(i).toArray();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] newEmptyArray(T[] array) {
        return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
    }
}
