package lambda.enumerable;

import static java.lang.Boolean.*;
import static java.util.Collections.*;
import static lambda.exception.UncheckedException.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.collection.EIterable;
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
public class Enumerable {
    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns false or null.
     */
    public static <E> boolean all(Iterable<E> collection, Fn1<E, ?> block) {
        for (E each : collection) {
            Object result = block.call(each);
            if (isFalseOrNull(result))
                return false;
        }
        return true;
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block ever returns a value other than false or null.
     */
    public static <E> boolean any(Iterable<E> collection, Fn1<E, ?> block) {
        for (E each : collection) {
            Object result = block.call(each);
            if (isNotFalseOrNull(result))
                return true;
        }
        return false;
    }

    /**
     * Returns a new list with the results of running block once for every
     * element in collection.
     */
    public static <E, R> EList<R> collect(Iterable<E> collection, Fn1<E, R> block) {
        EList<R> result = new EList<R>();
        for (E each : collection)
            result.add(block.call(each));
        return result;
    }

    /**
     * Returns the count of all elements in collection.
     */
    public static <E> int count(Iterable<E> collection) {
        if (collection instanceof Collection<?>)
            return ((Collection<E>) collection).size();
        int count = 0;
        for (Iterator<E> iterator = collection.iterator(); iterator.hasNext(); iterator.next())
            count++;
        return count;
    }

    /**
     * Returns the count of objects in collection that equal obj.
     */
    public static <E> int count(Iterable<E> collection, E obj) {
        int count = 0;
        for (E each : collection)
            if (obj.equals(each))
                count++;
        return count;
    }

    /**
     * Returns the count of objects in collection for which the block returns a
     * true value.
     */
    public static <E> int count(Iterable<E> collection, Fn1<E, Boolean> block) {
        return select(collection, block).size();
    }

    /**
     * Returns null if collection has no elements; otherwise, passes the
     * elements, one at a time to the block. When it reaches the end, it
     * repeats. The number of times it repeats is set by the parameter.
     */
    public static <E, R> EList<E> cycle(Iterable<E> collection, int times, Fn1<E, R> block) {
        EList<E> result = new EList<E>();
        while (times-- > 0) {
            for (E each : collection) {
                block.call(each);
                result.add(each);
            }
        }
        return result;
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns null.
     */
    public static <E> E detect(Iterable<E> collection, Fn1<E, Boolean> block) {
        return detect(collection, null, block);
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns ifNone.
     */
    public static <E> E detect(Iterable<E> collection, E ifNone, Fn1<E, Boolean> block) {
        for (E each : collection)
            if (block.call(each))
                return each;
        return ifNone;
    }

    /**
     * Returns a list containing all but the first n elements of collection.
     */
    public static <E> EList<E> drop(Iterable<E> collection, int n) {
        EList<E> result = new EList<E>();
        for (E each : collection)
            if (n-- <= 0)
                result.add(each);
        return result;
    }

    /**
     * Passes elements in turn to the block until the block does not return a
     * true value. Starting with that element, copies the remainder to a list
     * and returns it.
     */
    public static <E> EList<E> dropWhile(Iterable<E> collection, Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E next : collection)
            if (!result.isEmpty() || !block.call(next))
                result.add(next);
        return result;
    }

    /**
     * Calls block for each item in collection.
     */
    public static <E, R> EIterable<E> each(Iterable<E> collection, Fn1<E, R> block) {
        for (E each : collection)
            block.call(each);
        return EIterable.from(collection);
    }

    /**
     * Calls block once for each key in map, passing the key and value to the
     * block as parameters.
     */
    public static <K, V, R> EMap<K, V> each(Map<K, V> map, Fn2<K, V, R> block) {
        for (Entry<K, V> each : map.entrySet())
            block.call(each.getKey(), each.getValue());
        return new EMap<K, V>(map);
    }

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    public static <E, R> Object eachCons(Iterable<E> collection, int n, Fn1<List<E>, R> block) {
        List<E> list = asNewList(collection);
        for (int i = 0; i + n <= list.size(); i++)
            if (n + i <= list.size())
                block.call(list.subList(i, i + n));
        return null;
    }

    /**
     * Calls block once for each key in map, passing the key as parameter.
     */
    public static <K, V, R> EMap<K, V> eachKey(Map<K, V> map, Fn1<K, R> block) {
        each(map.keySet(), block);
        return new EMap<K, V>(map);
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
        List<E> list = asNewList(collection);
        for (int i = 0; i <= list.size(); i += n)
            if (i + n > list.size())
                block.call(list.subList(i, list.size()));
            else
                block.call(list.subList(i, i + n));
        return null;
    }

    /**
     * Calls block once for each value in map, passing the key as parameter.
     */
    public static <K, V, R> EMap<K, V> eachValue(Map<K, V> map, Fn1<V, R> block) {
        each(map.values(), block);
        return new EMap<K, V>(map);
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * collection.
     */
    public static <E, R> EIterable<E> eachWithIndex(Iterable<E> collection, Fn2<E, Integer, R> block) {
        int i = 0;
        for (E each : collection)
            block.call(each, i++);
        return EIterable.from(collection);
    }

    /**
     * Calls block with two arguments, the item and the memo object, for each
     * item in collection.
     */
    public static <E, M, R> M eachWithObject(Iterable<E> collection, M memo, Fn2<E, M, R> block) {
        for (E each : collection)
            block.call(each, memo);
        return memo;
    }

    /**
     * @see #toList(Iterable)
     */
    public static <E> EList<E> entries(Iterable<E> collection) {
        return toList(collection);
    }

    /**
     * @see #detect(Iterable, Fn1)
     */
    public static <E> E find(Iterable<E> collection, Fn1<E, Boolean> block) {
        return detect(collection, block);
    }

    /**
     * @see #detect(Iterable, Object, Fn1)
     */
    public static <E> E find(Iterable<E> collection, E ifNone, Fn1<E, Boolean> block) {
        return detect(collection, ifNone, block);
    }

    /**
     * @see #select(Iterable, Fn1)
     */
    public static <E> EList<E> findAll(Iterable<E> collection, Fn1<E, Boolean> block) {
        return select(collection, block);
    }

    /**
     * Returns the index of the first item for which the given block returns a
     * true value or returns -1 if the block only ever returns false.
     */
    public static <E> int findIndex(Iterable<E> collection, Fn1<E, Boolean> block) {
        int index = 0;
        for (E each : collection)
            if (block.call(each))
                return index;
            else
                index++;
        return -1;
    }

    /**
     * Returns the first item of collection or null.
     */
    public static <E> E first(Iterable<E> collection) {
        for (E each : collection)
            return each;
        return null;
    }

    /**
     * Returns the first n items of collection.
     */
    public static <E> EList<E> first(Iterable<E> collection, int n) {
        EList<E> result = new EList<E>();
        for (E each : collection)
            if (n-- > 0)
                result.add(each);
            else
                return result;
        return result;
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     */
    public static <E> EList<E> grep(Iterable<E> collection, Pattern pattern) {
        EList<E> result = new EList<E>();
        for (E each : collection)
            if (pattern.matcher(each.toString()).matches())
                result.add(each);
        return result;
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    public static <E, R> EList<R> grep(Iterable<E> collection, Pattern pattern, Fn1<E, R> block) {
        EList<R> result = new EList<R>();
        for (E each : collection)
            if (pattern.matcher(each.toString()).matches())
                result.add(block.call(each));
        return result;
    }

    /**
     * @see #grep(Iterable, Pattern)
     */
    public static <E> EList<E> grep(Iterable<E> collection, String pattern) {
        return grep(collection, Pattern.compile(pattern));
    }

    /**
     * @see #grep(Iterable, Pattern, Fn1)
     */
    public static <E, R> EList<R> grep(Iterable<E> collection, String pattern, Fn1<E, R> block) {
        return grep(collection, Pattern.compile(pattern), block);
    }

    /**
     * Partitions collection by calling the block for each item and using the
     * result returned by the block to group the items into buckets. Returns a
     * map where the keys are the objects returned by the block, and the values
     * for a key are those items for which the block returned that object.
     */
    public static <E, R> EMap<R, EList<E>> groupBy(Iterable<E> collection, Fn1<E, R> block) {
        EMap<R, EList<E>> result = new EMap<R, EList<E>>();
        for (E each : collection) {
            R key = block.call(each);
            if (!result.containsKey(key))
                result.put(key, new EList<E>());
            result.get(key).add(each);
        }
        return result;
    }

    /**
     * Named parameter for detect.
     * 
     * @see #detect(Iterable, Object, Fn1)
     * @see #find(Iterable, Object, Fn1)
     */
    public static <R> R ifNone(R defaultValue) {
        return defaultValue;
    }

    /**
     * @see #member(Iterable, Object)
     */
    public static <E> boolean includes(Iterable<E> collection, Object obj) {
        return asNewList(collection).contains(obj);
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form uses the first element
     * of the collection as a the initial value (and skips that element while
     * iterating).
     */
    public static <E> E inject(Iterable<E> collection, Fn2<E, E, E> block) {
        Iterator<E> i = collection.iterator();
        if (!i.hasNext())
            return null;
        E initial = i.next();
        while (i.hasNext())
            initial = block.call(initial, i.next());
        return initial;
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form lets you supply an
     * initial value for memo.
     */
    public static <E, R> R inject(Iterable<E> collection, R initial, Fn2<R, E, R> block) {
        for (E each : collection)
            initial = block.call(initial, each);
        return initial;
    }

    /**
     * @see #collect(Iterable, Fn1)
     */
    public static <E, R> EList<R> map(Iterable<E> collection, Fn1<E, R> block) {
        return collect(collection, block);
    }

    /**
     * Returns the object in collection with the maximum value. This form
     * assumes all objects implement {@link Comparable}
     */
    public static <E extends Object & Comparable<? super E>> E max(Iterable<E> collection) {
        Comparator<E> reverseOrder = reverseOrder();
        return min(collection, reverseOrder);
    }

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    @SuppressWarnings("unchecked")
    public static <E> E max(Iterable<E> collection, Fn2<E, E, Integer> block) {
        return min(collection, reverseOrder((Comparator<E>) block.as(Comparator.class)));
    }

    /**
     * Passes each item in the collection to the block. Returns the item
     * corresponding to the largest value returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E maxBy(Iterable<E> collection, Fn1<E, R> block) {
        return min(collection, reverseOrder(new BlockResultComparator<E, R>(block)));
    }

    /**
     * Returns true if any member of collection equals obj. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    public static <E> boolean member(Iterable<E> collection, Object obj) {
        return includes(collection, obj);
    }

    /**
     * Returns the object in collection with the minimum value. This form
     * assumes all objects implement {@link Comparable}.
     */
    public static <E extends Object & Comparable<? super E>> E min(Iterable<E> collection) {
        return min(collection, new NaturalOrderComparator<E>());
    }

    /**
     * Returns the object in collection with the minimum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    @SuppressWarnings("unchecked")
    public static <E> E min(Iterable<E> collection, Fn2<E, E, Integer> block) {
        return min(collection, (Comparator<E>) block.as(Comparator.class));
    }

    /**
     * Passes each item in the collection to the block. Returns the item
     * corresponding to the smallest value returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> E minBy(Iterable<E> collection, Fn1<E, R> block) {
        return min(collection, new BlockResultComparator<E, R>(block));
    }

    /**
     * Compares the elements of collection using {@link Comparable}, returning
     * the minimum and maximum value.
     */
    public static <E extends Object & Comparable<? super E>> EList<E> minMax(Iterable<E> collection) {
        EList<E> result = new EList<E>();
        result.add(min(collection));
        result.add(max(collection));
        return result;
    }

    /**
     * Compares the elements of collection using the given block, returning the
     * minimum and maximum value.
     */
    public static <E> EList<E> minMax(Iterable<E> collection, Fn2<E, E, Integer> block) {
        EList<E> result = new EList<E>();
        result.add(min(collection, block));
        result.add(max(collection, block));
        return result;
    }

    /**
     * Passes each item in the collection to the block. Returns the items
     * corresponding to the smallest and largest values returned by the block.
     */
    public static <E, R extends Object & Comparable<? super R>> EList<E> minMaxBy(Iterable<E> collection,
            Fn1<E, R> block) {
        EList<E> result = new EList<E>();
        result.add(minBy(collection, block));
        result.add(maxBy(collection, block));
        return result;
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns a value other than false or null.
     */
    public static <E> boolean none(Iterable<E> collection, Fn1<E, ?> block) {
        return !any(collection, block);
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block returns true exactly one time.
     */
    public static <E> boolean one(Iterable<E> collection, Fn1<E, ?> block) {
        Object match = null;
        for (E each : collection) {
            Object result = block.call(each);
            if (isNotFalseOrNull(result))
                if (match != null)
                    return false;
                else
                    match = result;
        }
        return match != null;
    }

    /**
     * Returns two lists, the first containing the elements of collection for
     * which the block evaluates to true, the second containing the rest.
     */
    public static <E> EList<EList<E>> partition(Iterable<E> collection, Fn1<E, Boolean> block) {
        EList<E> selected = new EList<E>();
        EList<E> rejected = new EList<E>();
        for (E each : collection)
            if (block.call(each))
                selected.add(each);
            else
                rejected.add(each);
        EList<EList<E>> result = new EList<EList<E>>();
        result.add(selected);
        result.add(rejected);
        return result;
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
    public static <E> E reduce(Iterable<E> collection, Fn2<E, E, E> block) {
        return inject(collection, block);
    }

    /**
     * @see #inject(Iterable, Object, Fn2)
     */
    public static <E, R> R reduce(Iterable<E> collection, R initial, Fn2<R, E, R> block) {
        return inject(collection, initial, block);
    }

    /**
     * Returns a list containing all elements of collection for which block is
     * false.
     */
    public static <E> EList<E> reject(Iterable<E> collection, Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E each : collection)
            if (!block.call(each))
                result.add(each);
        return result;
    }

    /**
     * Invokes the block with the elements of collection in reverse order.
     * Creates an intermediate list internally, so this might be expensive on
     * large collections.
     */
    public static <E, R> IEnumerable<E> reverseEach(Iterable<E> collection, Fn1<E, R> block) {
        List<E> result = asNewList(collection);
        Collections.reverse(result);
        return each(result, block);
    }

    /**
     * Returns a list containing all elements of collection for which block is
     * not false.
     */
    public static <E> EList<E> select(Iterable<E> collection, Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E each : collection)
            if (block.call(each))
                result.add(each);
        return result;
    }

    /**
     * Returns a list containing all Map.Entry pairs for which the block returns
     * true.
     */
    public static <K, V> EList<Map.Entry<K, V>> select(Map<K, V> map, Fn2<K, V, Boolean> block) {
        EList<Map.Entry<K, V>> result = new EList<Map.Entry<K, V>>();
        for (Map.Entry<K, V> each : map.entrySet())
            if (block.call(each.getKey(), each.getValue()))
                result.add(each);
        return result;
    }

    /**
     * Returns a list containing the items in collection sorted, according to
     * their own compareTo method.
     */
    public static <E extends Object & Comparable<? super E>> EList<E> sort(Iterable<E> collection) {
        return sort(collection, (Comparator<E>) null);
    }

    private static <E> EList<E> sort(Iterable<E> collection, Comparator<E> comparator) {
        List<E> result = asNewList(collection);
        Collections.sort(result, comparator);
        return new EList<E>(result);
    }

    /**
     * Returns a list containing the items in collection sorted by using the
     * results of the supplied block.
     */
    @SuppressWarnings("unchecked")
    public static <E> EList<E> sort(Iterable<E> collection, Fn2<E, E, Integer> block) {
        return sort(collection, block.as(Comparator.class));
    }

    /**
     * Sorts collection using a set of keys generated by mapping the values in
     * collection through the given block.
     */
    public static <E, R extends Object & Comparable<? super R>> EList<E> sortBy(Iterable<E> collection,
            final Fn1<E, R> block) {
        return sort(collection, new BlockResultComparator<E, R>(block));
    }

    /**
     * Returns a list containing the first n items from collection.
     */
    public static <E> EList<E> take(Iterable<E> collection, int n) {
        EList<E> result = new EList<E>();
        for (E each : collection)
            if (n-- > 0)
                result.add(each);
            else
                return result;
        return result;
    }

    /**
     * Passes successive items to the block, adding them to the result list
     * until the block returns false or null.
     */
    public static <E> EList<E> takeWhile(Iterable<E> collection, Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E next : collection)
            if (block.call(next))
                result.add(next);
            else
                return result;
        return result;
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
        return new EList<E>(asNewList(collection));
    }

    /**
     * Creates a new Set containing the elements of the given collection.
     */
    public static <E> ESet<E> toSet(Iterable<E> collection) {
        return new ESet<E>(new HashSet<E>(asNewList(collection)));
    }

    /**
     * Creates a new Set containing the elements of the given collection, the
     * elements are preprocessed by the given block.
     */
    public static <E, R> ESet<R> toSet(Iterable<E> collection, Fn1<E, R> block) {
        return toSet(collect(collection, block));
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
     * Feed the result into {@link #collect(Iterable, Fn1)} to achieve the same
     * effect.
     * </p>
     */
    public static <E> EList<EList<?>> zip(Iterable<E> collection, Iterable<?>... args) {
        EList<EList<?>> allResults = new EList<EList<?>>();

        List<Iterator<?>> iterators = new ArrayList<Iterator<?>>();
        iterators.add(collection.iterator());
        for (Iterable<?> iterable : args)
            iterators.add(iterable.iterator());

        while (iterators.get(0).hasNext()) {
            EList<Object> result = new EList<Object>();
            for (Iterator<?> iterator : iterators)
                if (iterator.hasNext())
                    result.add(iterator.next());
                else
                    result.add(null);
            allResults.add(result);
        }

        return allResults;
    }

    static boolean isNotFalseOrNull(Object obj) {
        return obj != FALSE && obj != null;
    }

    static boolean isFalseOrNull(Object result) {
        return !isNotFalseOrNull(result);
    }

    static <E> List<E> asNewList(Iterable<E> collection) {
        if (collection instanceof Collection<?>)
            return new ArrayList<E>((Collection<E>) collection);

        List<E> result = new ArrayList<E>();
        for (E each : collection)
            result.add(each);

        return result;
    }

    static <E> E min(Iterable<E> collection, Comparator<E> comparator) {
        E result = null;
        for (E each : collection)
            if (result == null || comparator.compare(each, result) < 0)
                result = each;
        return result;
    }

    static class BlockResultComparator<E, R extends Object & Comparable<? super R>> implements Comparator<E> {
        Fn1<E, R> block;

        BlockResultComparator(Fn1<E, R> block) {
            this.block = block;
        }

        public int compare(E o1, E o2) {
            return block.call(o1).compareTo(block.call(o2));
        }
    }

    static class NaturalOrderComparator<E extends Object & Comparable<? super E>> implements Comparator<E> {
        public int compare(E o1, E o2) {
            return o1.compareTo(o2);
        }
    }
}
