package lambda.enumerable.arrays;

import static java.util.Arrays.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;

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
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns false or null.
     */
    public static <E> boolean all(E[] col, Fn1<E, ?> block) {
        return lambda.enumerable.Enumerable.all(asList(col), block);
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block ever returns a value other than false or null.
     */
    public static <E> boolean any(E[] col, Fn1<E, ?> block) {
        return Enumerable.any(asList(col), block);
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in collection.
     */
    public static <E, R> Object[] collect(E[] col, Fn1<E, R> block) {
        return Enumerable.collect(asList(col), block).toArray();
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns null.
     */
    public static <E> E detect(E[] col, Fn1<E, Boolean> block) {
        return Enumerable.detect(asList(col), block);
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns ifNone.
     */
    public static <E> E detect(E[] col, Fn1<E, Boolean> block, E ifNone) {
        return Enumerable.detect(asList(col), block);
    }

    /**
     * Calls block for each item in collection.
     */
    public static <E, R> E[] each(E[] col, Fn1<E, R> block) {
        return Enumerable.toList(Enumerable.each(asList(col), block)).toArray(copyOf(col, 0));
    }

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    public static <E, R> Object eachCons(E[] col, int n, Fn1<List<E>, R> block) {
        return Enumerable.eachCons(asList(col), n, block);
    }

    /**
     * Iterates the given block for each slice of n elements.
     */
    public static <E, R> Object eachSlice(E[] col, int n, Fn1<List<E>, R> block) {
        return Enumerable.eachSlice(asList(col), n, block);
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * collection.
     */
    public static <E, R> E[] eachWithIndex(E[] col, Fn2<E, Integer, R> block) {
        return Enumerable.toList(Enumerable.eachWithIndex(asList(col), block)).toArray(copyOf(col, 0));
    }

    /**
     * @see #toList(Iterable)
     */
    public static <E> E[] entries(E[] col) {
        return Enumerable.toList(asList(col)).toArray(copyOf(col, 0));
    }

    /**
     * @see #detect(Iterable, Fn1)
     */
    public static <E> E find(E[] col, Fn1<E, Boolean> block) {
        return Enumerable.detect(asList(col), block);
    }
    /**
     * @see #detect(Iterable, Fn1, Object)
     */
    public static <E> E find(E[] col, Fn1<E, Boolean> block, E ifNone) {
        return Enumerable.detect(asList(col), block);
    }

    /**
     * @see #select(Iterable, Fn1)
     */
    public static <E> E[] findAll(E[] col, Fn1<E, Boolean> block) {
        return Enumerable.findAll(asList(col), block).toArray(copyOf(col, 0));
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     */
    public static <E> E[] grep(E[] col, Pattern pattern) {
        return Enumerable.grep(asList(col), pattern).toArray(copyOf(col, 0));
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    public static <E, R> Object[] grep(E[] col, Pattern pattern, Fn1<E, R> block) {
        return Enumerable.grep(asList(col), pattern, block).toArray();
    }

    /**
     * @see #grep(Iterable, Pattern)
     */
    public static <E> E[] grep(E[] col, String pattern) {
        return Enumerable.grep(asList(col), pattern).toArray(copyOf(col, 0));
    }

    /**
     * @see #grep(Iterable, Pattern, Fn1)
     */
    public static <E, R> Object[] grep(E[] col, String pattern, Fn1<E, R> block) {
        return Enumerable.grep(asList(col), pattern, block).toArray();
    }

    /**
     * @see #member(Iterable, Object)
     */
    public static <E> boolean includes(E[] col, Object obj) {
        return Enumerable.includes(asList(col), obj);
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form uses the first element
     * of the collection as a the initial value (and skips that element while
     * iterating).
     */
    public static <E> E inject(E[] col, Fn2<E, E, E> block) {
        return Enumerable.inject(asList(col), block);
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form lets you supply an
     * initial value for memo.
     */
    public static <E, R> R inject(E[] col, R initial, Fn2<R, E, R> block) {
        return Enumerable.inject(asList(col), initial, block);
    }

    /**
     * @see #collect(Iterable, Fn1)
     */
    public static <E, R> Object[] map(E[] col, Fn1<E, R> block) {
        return Enumerable.map(asList(col), block).toArray();
    }

    /**
     * Returns the object in collection with the maximum value. This form
     * assumes all objects implement {@link Comparable}
     */
    public static <E extends Object & Comparable<? super E>> E max(E[] col) {
        return Enumerable.max(asList(col));
    }

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static <E> E max(E[] col, Fn2<E, E, Integer> block) {
        return Enumerable.max(asList(col), block);
    }

    /**
     * Returns true if any member of collection equals obj. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    public static <E> boolean member(E[] col, Object obj) {
        return Enumerable.member(asList(col), obj);
    }

    /**
     * Returns the object in collection with the minimum value. This form
     * assumes all objects implement {@link Comparable}.
     */
    public static <E extends Object & Comparable<? super E>> E min(E[] col) {
        return Enumerable.min(asList(col));
    }

    /**
     * Returns the object in collection with the minimum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static <E> E min(E[] col, Fn2<E, E, Integer> block) {
        return Enumerable.min(asList(col), block);
    }

    /**
     * Returns two lists, the first containing the elements of collection for
     * which the block evaluates to true, the second containing the rest.
     */
    @SuppressWarnings("unchecked")
    public static <E> E[][] partition(E[] col, Fn1<E, Boolean> block) {
        List<List<E>> partition = Enumerable.partition(asList(col), block);

        E[][] result = (E[][]) java.lang.reflect.Array.newInstance(col.getClass(), 2);

        result[0] = partition.get(0).toArray(copyOf(col, 0));
        result[1] = partition.get(1).toArray(copyOf(col, 0));

        return result;
    }

    /**
     * Returns an list containing all elements of collection for which block is
     * false.
     */
    public static <E> E[] reject(E[] col, Fn1<E, Boolean> block) {
        return Enumerable.reject(asList(col), block).toArray(copyOf(col, 0));
    }

    /**
     * Returns an list containing all elements of collection for which block is
     * not false.
     */
    public static <E> E[] select(E[] col, Fn1<E, Boolean> block) {
        return Enumerable.select(asList(col), block).toArray(copyOf(col, 0));
    }

    /**
     * Returns a list containing the items in collection sorted, according to
     * their own compareTo method.
     */
    public static <E extends Object & Comparable<? super E>> E[] sort(E[] col) {
        return Enumerable.sort(asList(col)).toArray(copyOf(col, 0));
    }

    /**
     * Returns a list containing the items in collection sorted by using the
     * results of the supplied block.
     */
    public static <E> E[] sort(E[] col, Fn2<E, E, Integer> block) {
        return Enumerable.sort(asList(col), block).toArray(copyOf(col, 0));
    }

    /**
     * Sorts collection using a set of keys generated by mapping the values in
     * collection through the given block.
     */
    public static <E, R extends Object & Comparable<? super R>> E[] sortBy(E[] col, final Fn1<E, R> block) {
        return Enumerable.sortBy(asList(col), block).toArray(copyOf(col, 0));
    }

    /**
     * Returns a list containing the items in collection.
     */
    public static <E> E[] toList(E[] col) {
        return Enumerable.toList(asList(col)).toArray(copyOf(col, 0));
    }

    /**
     * Creates a new Set containing the elements of the given collection.
     */
    public static <E> Set<E> toSet(E[] col) {
        return Enumerable.toSet(asList(col));
    }

    /**
     * Creates a new Set containing the elements of the given collection, the
     * elements are preprocessed by the given block.
     */
    public static <E, R> Set<R> toSet(E[] col, Fn1<E, R> block) {
        return Enumerable.toSet(asList(col), block);
    }

    /**
     * Converts any arguments to iterators, then merges elements of collection
     * with corresponding elements from each argument. This generates a sequence
     * of collection#size n-element list, where n is one more that the count of
     * arguments. If the size of any argument is less than collection#size, null
     * values are supplied.
     * 
     * <p>
     * Due to varargs this version doesn't support taking a block like in Ruby.
     * Feed the result into {@link #collect(Iterable, Fn1) to achieve the same
     * effect.
     * </p>
     */
    public static <E> Object[][] zip(E[] col, Object[]... args) {
        Iterable<?>[] lists = new Iterable<?>[args.length];
        for (int i = 0; i < args.length; i++)
            lists[i] = asList(args[i]);

        List<List<?>> zip = Enumerable.zip(asList(col), lists);

        Object[][] result = new Object[zip.size()][];
        for (int i = 0; i < zip.size(); i++)
            result[i] = zip.get(i).toArray();
        return result;
    }
}
