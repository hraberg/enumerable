package lambda.enumerable.collection;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;

public interface EIterable<E> extends Iterable<E> {
    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns false or null.
     */
    boolean all(Fn1<E, ?> block);

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block ever returns a value other than false or null.
     */
    boolean any(Fn1<E, ?> block);

    /**
     * Returns a new list with the results of running block once for every
     * element in collection.
     */
    <R> EList<R> collect(Fn1<E, R> block);

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns null.
     */
    E detect(Fn1<E, Boolean> block);

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns ifNone.
     */
    E detect(Fn1<E, Boolean> block, E ifNone);

    /**
     * Calls block for each item in collection.
     */
    <R> EIterable<E> each(Fn1<E, R> block);

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    <R> Object eachCons(int n, Fn1<List<E>, R> block);

    /**
     * Iterates the given block for each slice of n elements.
     */
    <R> Object eachSlice(int n, Fn1<List<E>, R> block);

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * collection.
     */
    <R> EIterable<E> eachWithIndex(Fn2<E, Integer, R> block);

    /**
     * @see #toList(Iterable)
     */
    EList<E> entries();

    /**
     * @see #detect(Iterable, Fn1)
     */
    E find(Fn1<E, Boolean> block);

    /**
     * @see #detect(Iterable, Fn1, Object)
     */
    E find(Fn1<E, Boolean> block, E ifNone);

    /**
     * @see #select(Iterable, Fn1)
     */
    EList<E> findAll(Fn1<E, Boolean> block);

    /**
     * Returns a list of every element in collection for which pattern matches.
     */
    EList<E> grep(Pattern pattern);

    /**
     * Returns a list of every element in collection for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    <R> EList<R> grep(Pattern pattern, Fn1<E, R> block);

    /**
     * @see #grep(Iterable, Pattern)
     */
    EList<E> grep(String pattern);

    /**
     * @see #grep(Iterable, Pattern, Fn1)
     */
    <R> EList<R> grep(String pattern, Fn1<E, R> block);

    /**
     * @see #member(Iterable, Object)
     */
    boolean includes(Object obj);

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form uses the first element
     * of the collection as a the initial value (and skips that element while
     * iterating).
     */
    E inject(Fn2<E, E, E> block);

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form lets you supply an
     * initial value for memo.
     */
    <R> R inject(R initial, Fn2<R, E, R> block);

    /**
     * @see #collect(Iterable, Fn1)
     */
    <R> EList<R> map(Fn1<E, R> block);

    /**
     * Returns the object in collection with the maximum value. This form
     * assumes all objects implement {@link Comparable}
     */
    E max();

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    E max(Fn2<E, E, Integer> block);

    /**
     * Returns true if any member of collection equals obj. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    boolean member(Object obj);

    /**
     * Returns the object in collection with the minimum value. This form
     * assumes all objects implement {@link Comparable}.
     */
    E min();

    /**
     * Returns the object in collection with the minimum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    E min(Fn2<E, E, Integer> block);

    /**
     * Returns two lists, the first containing the elements of collection for
     * which the block evaluates to true, the second containing the rest.
     */
    EList<EList<E>> partition(Fn1<E, Boolean> block);

    /**
     * Returns a list containing all elements of collection for which block is
     * false.
     */
    EList<E> reject(Fn1<E, Boolean> block);

    /**
     * Returns a list containing all elements of collection for which block is
     * not false.
     */
    EList<E> select(Fn1<E, Boolean> block);

    /**
     * Returns a list containing the items in collection sorted, according to
     * their own compareTo method.
     */
    EList<E> sort();

    /**
     * Returns a list containing the items in collection sorted by using the
     * results of the supplied block.
     */
    EList<E> sort(Fn2<E, E, Integer> block);

    /**
     * Sorts collection using a set of keys generated by mapping the values in
     * collection through the given block.
     */
    <R extends Object & Comparable<? super R>> EList<E> sortBy(Fn1<E, R> block);

    /**
     * Returns a list containing the items in collection.
     */
    EList<E> toList();

    /**
     * Creates a new Set containing the elements of the given collection.
     */
    ESet<E> toSet();

    /**
     * Creates a new Set containing the elements of the given collection, the
     * elements are preprocessed by the given block.
     */
    <R> ESet<R> toSet(Fn1<E, R> block);

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
    EList<EList<?>> zip(Iterable<?>... args);
}
