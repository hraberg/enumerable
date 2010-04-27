package lambda.enumerable.collection;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;

/**
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module in 1.8.6</a>
 * </p>
 * <p>
 * <a href="http://ruby-doc.org/ruby-1.9/classes/Enumerable.html"/>Ruby's
 * Enumerable module in 1.9</a>
 * </p>
 */
public interface IEnumerable<E> extends Iterable<E> {
    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns false or null.
     */
    boolean all(Fn1<? super E, ?> block);

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block ever returns a value other than false or null.
     */
    boolean any(Fn1<? super E, ?> block);

    /**
     * Returns a new list with the results of running block once for every
     * element in collection.
     */
    <R> EList<R> collect(Fn1<? super E, R> block);

    /**
     * Returns the count of all elements in collection.
     */
    int count();

    /**
     * Returns the count of objects in collection that equal obj.
     */
    int count(E obj);

    /**
     * Returns the count of objects in collection for which the block returns a
     * true value.
     */
    int count(Fn1<? super E, Boolean> block);

    /**
     * Returns null if collection has no elements; otherwise, passes the
     * elements, one at a time to the block. When it reaches the end, it
     * repeats. The number of times it repeats is set by the parameter.
     */
    <R> Object cycle(int times, Fn1<? super E, R> block);

    /**
     * Returns a list containing all but the first n elements of collection.
     */
    EList<E> drop(int n);

    /**
     * Passes elements in turn to the block until the block does not return a
     * true value. Starting with that element, copies the remainder to a list
     * and returns it.
     */
    EList<E> dropWhile(Fn1<? super E, Boolean> block);

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns null.
     */
    E detect(Fn1<? super E, Boolean> block);

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns ifNone.
     */
    E detect(Fn0<E> ifNone, Fn1<? super E, Boolean> block);

    /**
     * Calls block for each item in collection.
     */
    <R> IEnumerable<E> each(Fn1<? super E, R> block);

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
    <R> IEnumerable<E> eachWithIndex(Fn2<? super E, Integer, R> block);

    /**
     * Calls block with two arguments, the item and the memo object, for each
     * item in collection.
     */
    <M, R> M eachWithObject(M memo, Fn2<? super E, M, R> block);

    /**
     * @see #toList()
     */
    EList<E> entries();

    /**
     * @see #detect(Fn1)
     */
    E find(Fn1<? super E, Boolean> block);

    /**
     * @see #detect(Fn0, Fn1)
     */
    E find(Fn0<E> ifNone, Fn1<? super E, Boolean> block);

    /**
     * Returns a list containing all elements of collection for which block is
     * not false.
     */
    EList<E> findAll(Fn1<? super E, Boolean> block);

    /**
     * Returns the index of the first item for which the given block returns a
     * true value or returns -1 if the block only ever returns false.
     */
    int findIndex(Fn1<? super E, Boolean> block);

    /**
     * Returns the first item of collection or null.
     */
    E first();

    /**
     * Returns the first n items of collection.
     */
    EList<E> first(int n);

    /**
     * Returns a list of every element in collection for which pattern matches.
     */
    EList<E> grep(Pattern pattern);

    /**
     * Returns a list of every element in collection for which pattern matches.
     * Each matching element is passed to the block, and its result is stored in
     * the output list.
     */
    <R> EList<R> grep(Pattern pattern, Fn1<? super E, R> block);

    /**
     * @see #grep(Pattern)
     */
    EList<E> grep(String pattern);

    /**
     * @see #grep(Pattern, Fn1)
     */
    <R> EList<R> grep(String pattern, Fn1<? super E, R> block);

    /**
     * Partitions collection by calling the block for each item and using the
     * result returned by the block to group the items into buckets. Returns a
     * map where the keys are the objects returned by the block, and the values
     * for a key are those items for which the block returned that object.
     */
    <R> EMap<R, EList<E>> groupBy(Fn1<? super E, R> block);

    /**
     * @see #member(Object)
     */
    boolean include(Object obj);

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
     * @see #collect(Fn1)
     */
    <R> EList<R> map(Fn1<? super E, R> block);

    /**
     * Returns the object in collection with the maximum value. This form
     * assumes all objects implement {@link Comparable}
     */
    E max();

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    E max(Fn2<? super E, ? super E, Integer> block);

    /**
     * Passes each item in the collection to the block. Returns the item
     * corresponding to the largest value returned by the block.
     */
    <R extends Object & Comparable<? super R>> E maxBy(Fn1<? super E, R> block);

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
    E min(Fn2<? super E, ? super E, Integer> block);

    /**
     * Passes each item in the collection to the block. Returns the item
     * corresponding to the smallest value returned by the block.
     */
    <R extends Object & Comparable<? super R>> E minBy(Fn1<? super E, R> block);

    /**
     * Compares the elements of self using {@link Comparable}, returning the
     * minimum and maximum value.
     */
    EList<E> minMax();

    /**
     * Compares the elements of self using the given block, returning the
     * minimum and maximum value.
     */
    EList<E> minMax(Fn2<? super E, ? super E, Integer> block);

    /**
     * Passes each item in the collection to the block. Returns the items
     * corresponding to the smallest and largest values returned by the block.
     */
    <R extends Object & Comparable<? super R>> EList<E> minMaxBy(Fn1<? super E, R> block);

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns a value other than false or null.
     */
    boolean none(Fn1<? super E, ?> block);

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block returns true exactly one time.
     */
    boolean one(Fn1<? super E, ?> block);

    /**
     * Returns two lists, the first containing the elements of collection for
     * which the block evaluates to true, the second containing the rest.
     */
    EList<EList<E>> partition(Fn1<? super E, Boolean> block);

    /**
     * @see #inject(Fn2)
     */
    E reduce(Fn2<E, E, E> block);

    /**
     * @see #inject(Object, Fn2)
     */
    <R> R reduce(R initial, Fn2<R, E, R> block);

    /**
     * Returns a list containing all elements of collection for which block is
     * false.
     */
    EList<E> reject(Fn1<? super E, Boolean> block);

    /**
     * Invokes the block with the elements of collection in reverse order.
     * Creates an intermediate list internally, so this might be expensive on
     * large collections.
     */
    <R> IEnumerable<E> reverseEach(Fn1<? super E, R> block);

    /**
     * @see #findAll(Fn1)
     */
    EList<E> select(Fn1<? super E, Boolean> block);

    /**
     * Returns a list containing the items in collection sorted, according to
     * their own compareTo method.
     */
    EList<E> sort();

    /**
     * Returns a list containing the items in collection sorted by using the
     * results of the supplied block.
     */
    EList<E> sort(Fn2<? super E, ? super E, Integer> block);

    /**
     * Sorts collection using a set of keys generated by mapping the values in
     * collection through the given block.
     * <p>
     * The current implementation of sortBy generates an array of tuples
     * containing the original collection element and the mapped value. This
     * makes sortBy fairly expensive when the keysets are simple
     * <p>
     * However, consider the case where comparing the keys is a non-trivial
     * operation. The following code sorts some files on modification time.
     * 
     * <pre>
     * sortBy(files, fn(s, new File(s).lastModified()) // ["mon", "tues", "wed", "thurs"]
     * </pre>
     * <p>
     * Perl users often call this approach a Schwartzian Transform, after Randal
     * Schwartz. We construct a temporary array, where each element is an array
     * containing our sort key along with the filename. We sort this array, and
     * then extract the filename from the result.
     */
    <R extends Object & Comparable<? super R>> EList<E> sortBy(Fn1<? super E, R> block);

    /**
     * Returns a list containing the first n items from collection.
     */
    EList<E> take(int n);

    /**
     * Passes successive items to the block, adding them to the result list
     * until the block returns false or null.
     */
    EList<E> takeWhile(Fn1<? super E, Boolean> block);

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
    <R> ESet<R> toSet(Fn1<? super E, R> block);

    /**
     * Converts any arguments to iterators, then merges elements of collection
     * with corresponding elements from each argument. This generates a sequence
     * of collection#size n-element list, where n is one more that the count of
     * arguments. If the size of any argument is less than collection#size, null
     * values are supplied.
     * 
     * <p>
     * Due to varargs this version doesn't support taking a block like in Ruby.
     * Feed the result into {@link #collect(Fn1)} to achieve the same effect.
     * </p>
     */
    EList<EList<?>> zip(Iterable<?>... args);
}
