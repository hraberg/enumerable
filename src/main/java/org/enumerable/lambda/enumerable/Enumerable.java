package org.enumerable.lambda.enumerable;

import static org.enumerable.lambda.enumerable.collection.EnumerableModule.*;
import static org.enumerable.lambda.exception.UncheckedException.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.enumerable.lambda.Fn0;
import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Fn2;
import org.enumerable.lambda.enumerable.collection.EList;
import org.enumerable.lambda.enumerable.collection.EMap;
import org.enumerable.lambda.enumerable.collection.ESet;
import org.enumerable.lambda.enumerable.collection.EnumerableModule;
import org.enumerable.lambda.enumerable.collection.IEnumerable;


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
 * 
 * This class is as a facade for the implementation of the in
 * {@link EnumerableModule} and {@link EMap}.
 */
public class Enumerable {
    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns false or null.
     */
    public static <E> boolean all(Iterable<E> collection, Fn1<? super E, ?> block) {
        return extend(collection).all(block);
    }

    /**
     * Passes each element of the collection to the an implicit block of
     * {@link Fn1#identity()}. The method returns true if the block never
     * returns false or null.
     */
    public static <E> boolean all(Iterable<E> collection) {
        return extend(collection).all();
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block ever returns a value other than false or null.
     */
    public static <E> boolean any(Iterable<E> collection, Fn1<? super E, ?> block) {
        return extend(collection).any(block);
    }

    /**
     * Passes each element of the collection to the an implicit block of
     * {@link Fn1#identity()}. The method returns true if the block ever returns
     * a value other than false or null.
     */
    public static <E> boolean any(Iterable<E> collection) {
        return extend(collection).any();
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in collection.
     */
    public static <E, R> EList<R> collect(Iterable<E> collection, Fn1<? super E, ? extends R> block) {
        return extend(collection).collect(block);
    }

    /**
     * Returns the count of all elements in collection.
     */
    public static <E> int count(Iterable<E> collection) {
        return extend(collection).count();
    }

    /**
     * Returns the count of objects in collection that equal obj.
     */
    public static <E> int count(Iterable<E> collection, E obj) {
        return extend(collection).count(obj);
    }

    /**
     * Returns the count of objects in collection for which the block returns a
     * true value.
     */
    public static <E> int count(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).count(block);
    }

    /**
     * Returns null if collection has no elements; otherwise, passes the
     * elements, one at a time to the block. When it reaches the end, it
     * repeats. The number of times it repeats is set by the parameter.
     */
    public static <E, R> EList<E> cycle(Iterable<E> collection, int times, Fn1<? super E, R> block) {
        return extend(collection).cycle(times, block);
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns null.
     */
    public static <E> E detect(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).detect(block);
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns ifNone.
     */
    public static <E> E detect(Iterable<E> collection, Fn0<E> ifNone, Fn1<? super E, Boolean> block) {
        return extend(collection).detect(ifNone, block);
    }

    /**
     * Returns a list containing all but the first n elements of collection.
     */
    public static <E> EList<E> drop(Iterable<E> collection, int n) {
        return extend(collection).drop(n);
    }

    /**
     * Passes elements in turn to the block until the block does not return a
     * true value. Starting with that element, copies the remainder to a list
     * and returns it.
     */
    public static <E> EList<E> dropWhile(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).dropWhile(block);
    }

    /**
     * Calls block for each item in collection.
     */
    public static <E, R> IEnumerable<E> each(Iterable<E> collection, Fn1<? super E, R> block) {
        return extend(collection).each(block);
    }

    /**
     * Calls block once for each key in map, passing the key and value to the
     * block as parameters.
     */
    public static <K, V, R> EMap<K, V> each(Map<K, V> map, Fn2<? super K, ? super V, R> block) {
        return extend(map).each(block);
    }

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    public static <E, R> Object eachCons(Iterable<E> collection, int n, Fn1<List<E>, R> block) {
        return extend(collection).eachCons(n, block);
    }

    /**
     * Calls block once for each key in map, passing the key as parameter.
     */
    public static <K, V, R> EMap<K, V> eachKey(Map<K, V> map, Fn1<? super K, ? super R> block) {
        return extend(map).eachKey(block);
    }

    /**
     * Executes the block for every line in file.
     */
    public static <R> File eachLine(File file, Fn1<String, R> block) {
        try {
            eachLine(new FileReader(file), block);
            return file;
        } catch (FileNotFoundException e) {
            throw uncheck(e);
        }
    }

    /**
     * Executes the block for every line in reader.
     */
    public static <R> Reader eachLine(Reader reader, Fn1<String, R> block) {
        try {
            BufferedReader in = new BufferedReader(reader);
            String line;
            while ((line = in.readLine()) != null)
                block.call(line);
            return reader;
        } catch (IOException e) {
            throw uncheck(e);
        } finally {
            try {
                reader.close();
            } catch (IOException silent) {
            }
        }
    }

    /**
     * Executes the block for every line in string.
     */
    public static <R> String eachLine(String string, Fn1<String, R> block) {
        eachLine(new StringReader(string), block);
        return string;
    }

    /**
     * Iterates the given block for each slice of n elements.
     */
    public static <E, R> Object eachSlice(Iterable<E> collection, int n, Fn1<List<E>, R> block) {
        return extend(collection).eachSlice(n, block);
    }

    /**
     * Calls block once for each value in map, passing the key as parameter.
     */
    public static <K, V, R> EMap<K, V> eachValue(Map<K, V> map, Fn1<? super V, ? super R> block) {
        return extend(map).eachValue(block);
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * collection.
     */
    public static <E, R> IEnumerable<E> eachWithIndex(Iterable<E> collection, Fn2<? super E, Integer, R> block) {
        return extend(collection).eachWithIndex(block);
    }

    /**
     * Calls block with two arguments, the item and the memo object, for each
     * item in collection.
     */
    public static <E, M, R> M eachWithObject(Iterable<E> collection, M memo, Fn2<? super E, M, R> block) {
        return extend(collection).eachWithObject(memo, block);
    }

    /**
     * @see #toList(Iterable)
     */
    public static <E> EList<E> entries(Iterable<E> collection) {
        return extend(collection).toList();
    }

    /**
     * @see #detect(Iterable, Fn1)
     */
    public static <E> E find(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).find(block);
    }

    /**
     * @see #detect(Iterable, Fn0, Fn1)
     */
    public static <E> E find(Iterable<E> collection, Fn0<E> ifNone, Fn1<? super E, Boolean> block) {
        return extend(collection).find(ifNone, block);
    }

    /**
     * @see #select(Iterable, Fn1)
     */
    public static <E> EList<E> findAll(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).findAll(block);
    }

    /**
     * Returns the index of the first item for which the given block returns a
     * true value or returns -1 if the block only ever returns false.
     */
    public static <E> int findIndex(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).findIndex(block);
    }

    /**
     * Returns the first item of collection or null.
     */
    public static <E> E first(Iterable<E> collection) {
        return extend(collection).first();
    }

    /**
     * Returns the first n items of collection.
     */
    public static <E> EList<E> first(Iterable<E> collection, int n) {
        return extend(collection).first(n);
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     */
    public static <E> EList<E> grep(Iterable<E> collection, Pattern pattern) {
        return extend(collection).grep(pattern);
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    public static <E, R> EList<R> grep(Iterable<E> collection, Pattern pattern, Fn1<? super E, R> block) {
        return extend(collection).grep(pattern, block);
    }

    /**
     * @see #grep(Iterable, Pattern)
     */
    public static <E> EList<E> grep(Iterable<E> collection, String pattern) {
        return extend(collection).grep(pattern);
    }

    /**
     * @see #grep(Iterable, Pattern, Fn1)
     */
    public static <E, R> EList<R> grep(Iterable<E> collection, String pattern, Fn1<? super E, R> block) {
        return extend(collection).grep(pattern, block);
    }

    /**
     * Partitions collection by calling the block for each item and using the
     * result returned by the block to group the items into buckets. Returns a
     * map where the keys are the objects returned by the block, and the values
     * for a key are those items for which the block returned that object.
     */
    public static <E, R> EMap<R, EList<E>> groupBy(Iterable<E> collection, Fn1<? super E, R> block) {
        return extend(collection).groupBy(block);
    }

    /**
     * Named parameter for detect.
     * 
     * @see #detect(Iterable, Fn0, Fn1)
     * @see #find(Iterable, Fn0, Fn1)
     */
    public static <R> Fn0<R> ifNone(R defaultValue) {
        return Fn0.constant(defaultValue);
    }

    /**
     * @see #member(Iterable, Object)
     */
    public static <E> boolean include(Iterable<E> collection, Object obj) {
        return extend(collection).include(obj);
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form uses the first element
     * of the collection as a the initial value (and skips that element while
     * iterating).
     */
    public static <E> E inject(Iterable<E> collection, Fn2<? super E, ? super E, ? extends E> block) {
        return extend(collection).inject(block);
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form lets you supply an
     * initial value for memo.
     */
    public static <E, R> R inject(Iterable<E> collection, R initial, Fn2<? super R, ? super E, ? extends R> block) {
        return extend(collection).inject(initial, block);
    }

    /**
     * @see #collect(Iterable, Fn1)
     */
    public static <E, R> EList<R> map(Iterable<E> collection, Fn1<? super E, ? extends R> block) {
        return extend(collection).map(block);
    }

    /**
     * Returns the object in collection with the maximum value. This form
     * assumes all objects implement {@link Comparable}
     */
    public static <E extends Object & Comparable<? super E>> E max(Iterable<E> collection) {
        return extend(collection).max();
    }

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static <E> E max(Iterable<E> collection, Fn2<? super E, ? super E, Integer> block) {
        return extend(collection).max(block);
    }

    /**
     * Passes each item in the collection to the block. Returns the item
     * corresponding to the largest value returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E maxBy(Iterable<E> collection,
            Fn1<? super E, R> block) {
        return extend(collection).maxBy(block);
    }

    /**
     * Returns true if any member of collection equals obj. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    public static <E> boolean member(Iterable<E> collection, Object obj) {
        return extend(collection).include(obj);
    }

    /**
     * Returns the object in collection with the minimum value. This form
     * assumes all objects implement {@link Comparable}.
     */
    public static <E extends Object & Comparable<? super E>> E min(Iterable<E> collection) {
        return extend(collection).min();
    }

    /**
     * Returns the object in collection with the minimum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static <E> E min(Iterable<E> collection, Fn2<? super E, ? super E, Integer> block) {
        return extend(collection).min(block);
    }

    /**
     * Passes each item in the collection to the block. Returns the item
     * corresponding to the smallest value returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E minBy(Iterable<E> collection,
            Fn1<? super E, R> block) {
        return extend(collection).minBy(block);
    }

    /**
     * Compares the elements of collection using {@link Comparable}, returning
     * the minimum and maximum value.
     */
    public static <E extends Object & Comparable<? super E>> EList<E> minMax(Iterable<E> collection) {
        return extend(collection).minMax();
    }

    /**
     * Compares the elements of collection using the given block, returning the
     * minimum and maximum value.
     */
    public static <E> EList<E> minMax(Iterable<E> collection, Fn2<? super E, ? super E, Integer> block) {
        return extend(collection).minMax(block);
    }

    /**
     * Passes each item in the collection to the block. Returns the items
     * corresponding to the smallest and largest values returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> EList<E> minMaxBy(Iterable<E> collection,
            Fn1<? super E, R> block) {
        return extend(collection).minMaxBy(block);
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns a value other than false or null.
     */
    public static <E> boolean none(Iterable<E> collection, Fn1<? super E, ?> block) {
        return extend(collection).none(block);
    }

    /**
     * Passes each element of the collection to the an implicit block of
     * {@link Fn1#identity()}. The method returns true if the block never
     * returns a value other than false or null.
     */
    public static <E> boolean none(Iterable<E> collection) {
        return extend(collection).none();
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block returns true exactly one time.
     */
    public static <E> boolean one(Iterable<E> collection, Fn1<? super E, ?> block) {
        return extend(collection).one(block);
    }

    /**
     * Passes each element of the collection to the an implicit block of
     * {@link Fn1#identity()}. The method returns true if the block returns true
     * exactly one time.
     */
    public static <E> boolean one(Iterable<E> collection) {
        return extend(collection).one();
    }

    /**
     * Returns two lists, the first containing the elements of collection for
     * which the block evaluates to true, the second containing the rest.
     */
    public static <E> EList<EList<E>> partition(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).partition(block);
    }

    /**
     * Constructs a range using the given start and end. The range will include
     * the end object.
     */
    public static Range range(int start, int end) {
        return range(start, end, false);
    }

    /**
     * Constructs a range using the given start and end. If the third parameter
     * is false, the range will include the end object; otherwise, it will be
     * excluded.
     */
    public static Range range(int start, int end, boolean exclusive) {
        return new Range(start, end, exclusive);
    }

    /**
     * @see #inject(Iterable, Fn2)
     */
    public static <E> E reduce(Iterable<E> collection, Fn2<? super E, ? super E, ? extends E> block) {
        return extend(collection).reduce(block);
    }

    /**
     * @see #inject(Iterable, Object, Fn2)
     */
    public static <E, R> R reduce(Iterable<E> collection, R initial, Fn2<? super R, ? super E, ? extends R> block) {
        return extend(collection).reduce(initial, block);
    }

    /**
     * Returns a list containing all elements of collection for which block is
     * false.
     */
    public static <E> EList<E> reject(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).reject(block);
    }

    /**
     * Invokes the block with the elements of collection in reverse order.
     * Creates an intermediate list internally, so this might be expensive on
     * large collections.
     */
    public static <E, R> IEnumerable<E> reverseEach(Iterable<E> collection, Fn1<? super E, R> block) {
        return extend(collection).reverseEach(block);
    }

    /**
     * Returns a list containing all elements of collection for which block is
     * not false.
     */
    public static <E> EList<E> select(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).select(block);
    }

    /**
     * Returns a list containing all Map.Entry pairs for which the block returns
     * true.
     */
    public static <K, V> EList<Map.Entry<K, V>> select(Map<K, V> map, Fn2<? super K, ? super V, Boolean> block) {
        return extend(map).select(block);
    }

    /**
     * Returns a list containing the items in collection sorted, according to
     * their own compareTo method.
     */
    public static <E extends Object & Comparable<? super E>> EList<E> sort(Iterable<E> collection) {
        return extend(collection).sort();
    }

    /**
     * Returns a list containing the items in collection sorted by using the
     * results of the supplied block.
     */
    public static <E> EList<E> sort(Iterable<E> collection, Fn2<? super E, ? super E, Integer> block) {
        return extend(collection).sort(block);
    }

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
    public static <E, R extends Object & Comparable<? super R>> EList<E> sortBy(Iterable<E> collection,
            Fn1<? super E, R> block) {
        return extend(collection).sortBy(block);
    }

    /**
     * Returns a list containing the first n items from collection.
     */
    public static <E> EList<E> take(Iterable<E> collection, int n) {
        return extend(collection).take(n);
    }

    /**
     * Passes successive items to the block, adding them to the result list
     * until the block returns false or null.
     */
    public static <E> EList<E> takeWhile(Iterable<E> collection, Fn1<? super E, Boolean> block) {
        return extend(collection).takeWhile(block);
    }

    /**
     * @see #times(int, Fn1)
     */
    public static int times(int i, Fn0<?> block) {
        Iterator<Integer> range = range(0, i, true).iterator();
        for (; range.hasNext(); range.next())
            block.call();
        return i;
    }

    /**
     * Iterates block i times, passing in values from zero to i - 1.
     */
    public static int times(int i, Fn1<Integer, ?> block) {
        each(range(0, i, true), block);
        return i;
    }

    /**
     * Returns a list containing the items in collection.
     */
    public static <E> EList<E> toList(Iterable<E> collection) {
        return extend(collection).toList();
    }

    /**
     * Creates a new Set containing the elements of the given collection.
     */
    public static <E> ESet<E> toSet(Iterable<E> collection) {
        return extend(collection).toSet();
    }

    /**
     * Creates a new Set containing the elements of the given collection, the
     * elements are preprocessed by the given block.
     */
    public static <E, R> ESet<R> toSet(Iterable<E> collection, Fn1<? super E, R> block) {
        return extend(collection).toSet(block);
    }

    /**
     * Converts any arguments to iterators, then merges elements of collection
     * with corresponding elements from each argument. This generates a sequence
     * of collection#size n-element list, where n is one more that the count of
     * arguments. If the size of any argument is less than collection#size, null
     * values are supplied. The block is invoked for each output array. Returns
     * null.
     */
    public static <E, R> Object zip(Iterable<E> collection, List<Iterable<?>> args, Fn1<? super EList<?>, R> block) {
        return extend(collection).zip(args, block);
    }

    /**
     * Converts any arguments to iterators, then merges elements of collection
     * with corresponding elements from each argument. This generates a sequence
     * of collection#size n-element list, where n is one more that the count of
     * arguments. If the size of any argument is less than collection#size, null
     * values are supplied.
     */
    public static <E> EList<EList<?>> zip(Iterable<E> collection, Iterable<?>... args) {
        return extend(collection).zip(args);
    }
}
