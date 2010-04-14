package lambda.enumerable;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.collection.EList;
import lambda.enumerable.collection.EMap;
import lambda.enumerable.collection.ESet;
import lambda.enumerable.collection.IEnumerable;

/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module in 1.8.6</a>
 * </p>
 * <p>
 * <a href="http://ruby-doc.org/ruby-1.9/classes/Enumerable.html"/>Ruby's
 * Enumerable module in 1.9</a>
 * </p>
 */
public class EnumerableArrays {
    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block never returns false or null.
     */
    public static <E> boolean all(E[] array, Fn1<E, ?> block) {
        return Enumerable.all(asIterable(array), block);
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block ever returns a value other than false or null.
     */
    public static <E> boolean any(E[] array, Fn1<E, ?> block) {
        return Enumerable.any(asIterable(array), block);
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array.
     */
    public static <E, R> Object[] collect(E[] array, Fn1<E, R> block) {
        return Enumerable.collect(asIterable(array), block).toArray();
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in array. Takes an extra type parameter to handle empty arrays.
     */
    @SuppressWarnings("unchecked")
    public static <E, R> R[] collect(E[] array, Fn1<E, R> block, Class<R> type) {
        return Enumerable.collect(asIterable(array), block).toArray((R[]) Array.newInstance(type, 0));
    }

    /**
     * Returns the count of all elements in array.
     */
    public static <E> int count(E[] array) {
        return array.length;
    }

    /**
     * Returns the count of objects in arry that equal obj.
     */
    public static <E> int count(E[] array, E obj) {
        return Enumerable.count(asIterable(array), obj);
    }

    /**
     * Returns the count of objects in array for which the block returns a true
     * value.
     */
    public static <E> int count(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.count(asIterable(array), block);
    }

    /**
     * Returns null if array has no elements; otherwise, passes the elements,
     * one at a time to the block. When it reaches the end, it repeats. The
     * number of times it repeats is set by the parameter.
     */
    public static <E, R> Object cycle(E[] array, int times, Fn1<E, R> block) {
        return Enumerable.cycle(asIterable(array), times, block);
    }

    /**
     * Passes each entry in array to block. Returns the first for which block is
     * not false. If no object matches, it returns null.
     */
    public static <E> E detect(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.detect(asIterable(array), block);
    }

    /**
     * Passes each entry in array to block. Returns the first for which block is
     * not false. If no object matches, it returns ifNone.
     */
    public static <E> E detect(E[] array, E ifNone, Fn1<E, Boolean> block) {
        return Enumerable.detect(asIterable(array), ifNone, block);
    }

    /**
     * Calls block for each item in array.
     */
    public static <E, R> E[] each(E[] array, Fn1<E, R> block) {
        Enumerable.each(asIterable(array), block);
        return array;
    }

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    public static <E, R> Object eachCons(E[] array, int n, Fn1<List<E>, R> block) {
        return Enumerable.eachCons(asIterable(array), n, block);
    }

    /**
     * Iterates the given block for each slice of n elements.
     */
    public static <E, R> Object eachSlice(E[] array, int n, Fn1<List<E>, R> block) {
        return Enumerable.eachSlice(asIterable(array), n, block);
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * array.
     */
    public static <E, R> E[] eachWithIndex(E[] array, Fn2<E, Integer, R> block) {
        Enumerable.eachWithIndex(asIterable(array), block);
        return array;
    }

    /**
     * @see #toList(Object...)
     */
    public static <E> EList<E> entries(E... array) {
        return Enumerable.toList(asIterable(array));
    }

    /**
     * @see #detect(Object[], Fn1)
     */
    public static <E> E find(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.find(asIterable(array), block);
    }

    /**
     * @see #detect(Object[], Object, Fn1)
     */
    public static <E> E find(E[] array, E ifNone, Fn1<E, Boolean> block) {
        return Enumerable.find(asIterable(array), ifNone, block);
    }

    /**
     * @see #select(Object[], Fn1)
     */
    public static <E> E[] findAll(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.findAll(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array of every element in array for which pattern matches.
     */
    public static <E> E[] grep(E[] array, Pattern pattern) {
        return Enumerable.grep(asIterable(array), pattern).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array of every element in array for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    public static <E, R> Object[] grep(E[] array, Pattern pattern, Fn1<E, R> block) {
        return Enumerable.grep(asIterable(array), pattern, block).toArray();
    }

    /**
     * @see #grep(Object[], Pattern)
     */
    public static <E> E[] grep(E[] array, String pattern) {
        return Enumerable.grep(asIterable(array), pattern).toArray(newEmptyArray(array));
    }

    /**
     * @see #grep(Object[], Pattern, Fn1)
     */
    public static <E, R> Object[] grep(E[] array, String pattern, Fn1<E, R> block) {
        return Enumerable.grep(asIterable(array), pattern, block).toArray();
    }

    /**
     * Partitions array by calling the block for each item and using the result
     * returned by the block to group the items into buckets. Returns a map
     * where the keys are the objects returned by the block, and the values for
     * a key are those items for which the block returned that object.
     */
    public static <E, R> EMap<R, EList<E>> groupBy(E[] array, Fn1<E, R> block) {
        return Enumerable.groupBy(asIterable(array), block);
    }

    /**
     * @see #member(Object[], Object)
     */
    public static <E> boolean includes(E[] array, Object obj) {
        return Enumerable.includes(asIterable(array), obj);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form uses the first element of the
     * array as a the initial value (and skips that element while iterating).
     */
    public static <E> E inject(E[] array, Fn2<E, E, E> block) {
        return Enumerable.inject(asIterable(array), block);
    }

    /**
     * Combines the elements of array by applying the block to an accumulator
     * value (memo) and each element in turn. At each step, memo is set to the
     * value returned by the block. This form lets you supply an initial value
     * for memo.
     */
    public static <E, R> R inject(E[] array, R initial, Fn2<R, E, R> block) {
        return Enumerable.inject(asIterable(array), initial, block);
    }

    /**
     * @see #collect(Object[], Fn1)
     */
    public static <E, R> Object[] map(E[] array, Fn1<E, R> block) {
        return Enumerable.map(asIterable(array), block).toArray();
    }

    /**
     * @see #collect(Object[], Fn1, Class)
     */
    public static <E, R> R[] map(E[] array, Fn1<E, R> block, Class<R> type) {
        return collect(array, block, type);
    }

    /**
     * Returns the object in array with the maximum value. This form assumes all
     * objects implement {@link Comparable}
     */
    public static <E extends Object & Comparable<? super E>> E max(E[] array) {
        return Enumerable.max(asIterable(array));
    }

    /**
     * Returns the object in array with the maximum value. This form uses the
     * block to {@link Comparator#compare}.
     */
    public static <E> E max(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.max(asIterable(array), block);
    }

    /**
     * Passes each item in the array to the block. Returns the item
     * corresponding to the largest value returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E maxBy(E[] array, Fn1<E, R> block) {
        return Enumerable.maxBy(asIterable(array), block);
    }

    /**
     * Returns true if any member of array equals obj. Equality is tested using
     * {@link Object#equals(Object)}.
     */
    public static <E> boolean member(E[] array, Object obj) {
        return Enumerable.member(asIterable(array), obj);
    }

    /**
     * Returns the object in array with the minimum value. This form assumes all
     * objects implement {@link Comparable}.
     */
    public static <E extends Object & Comparable<? super E>> E min(E[] array) {
        return Enumerable.min(asIterable(array));
    }

    /**
     * Returns the object in array with the minimum value. This form uses the
     * block to {@link Comparator#compare}.
     */
    public static <E> E min(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.min(asIterable(array), block);
    }

    /**
     * Passes each item in the array to the block. Returns the item
     * corresponding to the smallest value returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E minBy(E[] array, Fn1<E, R> block) {
        return Enumerable.minBy(asIterable(array), block);
    }

    /**
     * Compares the elements of array using {@link Comparable}, returning the
     * minimum and maximum value.
     */
    public static <E extends Object & Comparable<? super E>> E[] minMax(E[] array) {
        return Enumerable.minMax(asIterable(array)).toArray(newEmptyArray(array));
    }

    /**
     * Compares the elements of array using the given block, returning the
     * minimum and maximum value.
     */
    public static <E> E[] minMax(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.minMax(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Passes each item in the array to the block. Returns the items
     * corresponding to the smallest and largest values returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E[] minMaxBy(E[] array, Fn1<E, R> block) {
        return Enumerable.minMaxBy(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block never returns a value other than false or null.
     */
    public static <E> boolean none(E[] array, Fn1<E, ?> block) {
        return Enumerable.none(asIterable(array), block);
    }

    /**
     * Passes each element of the array to the given block. The method returns
     * true if the block returns true exactly one time.
     */
    public static <E> boolean one(E[] array, Fn1<E, ?> block) {
        return Enumerable.one(asIterable(array), block);
    }

    /**
     * Returns two lists, the first containing the elements of array for which
     * the block evaluates to true, the second containing the rest.
     */
    @SuppressWarnings("unchecked")
    public static <E> E[][] partition(E[] array, Fn1<E, Boolean> block) {
        List<EList<E>> partition = Enumerable.partition(asIterable(array), block);

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
        return Enumerable.range(start, end).toList().toArray(new Integer[0]);
    }

    /**
     * Constructs a range using the given start and end. If the third parameter
     * is false, the range will include the end object; otherwise, it will be
     * excluded.
     */
    public static Integer[] range(int start, int end, boolean exclusive) {
        return Enumerable.range(start, end, exclusive).toList().toArray(new Integer[0]);
    }

    /**
     * @see #inject(Object[], Fn2)
     */
    public static <E> E reduce(E[] array, Fn2<E, E, E> block) {
        return Enumerable.inject(asIterable(array), block);
    }

    /**
     * @see #inject(Object[], Object, Fn2)
     */
    public static <E, R> R reduce(E[] array, R initial, Fn2<R, E, R> block) {
        return Enumerable.inject(asIterable(array), initial, block);
    }

    /**
     * Returns an array containing all elements of array for which block is
     * false.
     */
    public static <E> E[] reject(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.reject(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Invokes the block with the elements of array in reverse order.
     */
    public static <E, R> IEnumerable<E> reverseEach(E[] array, Fn1<E, R> block) {
        return Enumerable.each(new ReverseArrayIterable<E>(array), block);
    }

    /**
     * Returns an array containing all elements of array for which block is not
     * false.
     */
    public static <E> E[] select(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.select(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array containing the items in array sorted, according to their
     * own compareTo method.
     */
    public static <E extends Object & Comparable<? super E>> E[] sort(E[] array) {
        return Enumerable.sort(asIterable(array)).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array containing the items in array sorted by using the
     * results of the supplied block.
     */
    public static <E> E[] sort(E[] array, Fn2<E, E, Integer> block) {
        return Enumerable.sort(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Sorts array using a set of keys generated by mapping the values in array
     * through the given block.
     */
    public static <E, R extends Object & Comparable<? super R>> E[] sortBy(E[] array, final Fn1<E, R> block) {
        return Enumerable.sortBy(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns an array containing the first n items from array.
     */
    public static <E> E[] take(E[] array, int n) {
        return Enumerable.take(asIterable(array), n).toArray(newEmptyArray(array));
    }

    /**
     * Passes successive items to the block, adding them to the result array
     * until the block returns false or null.
     */
    public static <E> E[] takeWhile(E[] array, Fn1<E, Boolean> block) {
        return Enumerable.takeWhile(asIterable(array), block).toArray(newEmptyArray(array));
    }

    /**
     * Returns a list containing the items in array.
     */
    public static <E> EList<E> toList(E... array) {
        return Enumerable.toList(asIterable(array));
    }

    /**
     * Creates a new Set containing the elements of the given array.
     */
    public static <E> ESet<E> toSet(E... array) {
        return Enumerable.toSet(asIterable(array));
    }

    /**
     * Creates a new Set containing the elements of the given array, the
     * elements are preprocessed by the given block.
     */
    public static <E, R> ESet<R> toSet(E[] array, Fn1<E, R> block) {
        return Enumerable.toSet(asIterable(array), block);
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
     * Feed the result into {@link #collect(Object[], Fn1)} to achieve the same
     * effect.
     * </p>
     */
    public static <E> Object[][] zip(E[] array, Object[]... args) {
        Iterable<?>[] lists = new Iterable<?>[args.length];
        for (int i = 0; i < args.length; i++)
            lists[i] = asIterable(args[i]);

        List<EList<?>> zip = Enumerable.zip(asIterable(array), lists);

        Object[][] result = new Object[zip.size()][];
        for (int i = 0; i < zip.size(); i++)
            result[i] = zip.get(i).toArray();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] newEmptyArray(T[] array) {
        return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
    }

    private static <T> Iterable<T> asIterable(T[] array) {
        return new ArrayIterable<T>(array);
    }

    static class ArrayIterable<T> implements Iterable<T> {
        T[] array;

        ArrayIterable(T... elements) {
            this.array = elements;
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int i = 0;

                public boolean hasNext() {
                    return i < array.length;
                }

                public T next() {
                    if (i == array.length)
                        throw new NoSuchElementException();
                    return array[i++];
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    static class ReverseArrayIterable<T> implements Iterable<T> {
        T[] array;

        ReverseArrayIterable(T... elements) {
            this.array = elements;
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int i = array.length - 1;

                public boolean hasNext() {
                    return i >= 0;
                }

                public T next() {
                    if (i < 0)
                        throw new NoSuchElementException();
                    return array[i--];
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
