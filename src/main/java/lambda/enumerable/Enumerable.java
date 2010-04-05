package lambda.enumerable;

import static java.lang.Boolean.*;
import static java.util.Arrays.*;
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
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;

/**
 * Ruby/Smalltalk style internal iterators for Java 5 using bytecode
 * transformation to capture expressions as closures.
 * 
 * <p>
 * <a href="http://ruby-doc.org/core/classes/Enumerable.html"/>Ruby's Enumerable
 * module</a>
 * </p>
 */
public class Enumerable {
    // Missing: zip
    // Missing tests: eachCons, eachSlice, toList, member, min, max, grep

    /**
     * Calls block for each item in collection.
     */
    public static <E, R> R each(Iterable<E> col, Fn1<E, R> block) {
        R result = null;
        for (E each : col)
            result = block.call(each);
        return result;
    }

    /**
     * Calls block once for each key in map, passing the key and value to the
     * block as parameters.
     */
    public static <K, V, R> R each(Map<K, V> map, Fn2<K, V, R> block) {
        R result = null;
        for (Entry<K, V> each : map.entrySet())
            result = block.call(each.getKey(), each.getValue());
        return result;
    }

    /**
     * Calls block once for each key in map, passing the key as parameter.
     */
    public static <K, V, R> R eachKey(Map<K, V> map, Fn1<K, R> block) {
        return each(map.keySet(), block);
    }

    /**
     * Calls block once for each value in map, passing the key as parameter.
     */
    public static <K, V, R> R eachValue(Map<K, V> map, Fn1<V, R> block) {
        return each(map.values(), block);
    }

    /**
     * Calls block with two arguments, the item and its index, for each item in
     * collection.
     */
    public static <E, R> R eachWithIndex(Iterable<E> col, Fn2<E, Integer, R> block) {
        int i = 0;
        R result = null;
        for (E each : col)
            result = block.call(each, i++);
        return result;
    }

    /**
     * Iterates the given block for each list of consecutive n elements.
     */
    public static <E, R> R eachCons(Iterable<E> col, int n, Fn1<List<E>, R> block) {
        List<E> list = toList(col);
        R result = null;
        for (int i = 0; i < list.size(); i++)
            result = block.call(list.subList(i, i + n - 1));
        return result;
    }

    /**
     * Iterates the given block for each slice of n elements.
     */
    public static <E, R> R eachSlice(Iterable<E> col, int n, Fn1<List<E>, R> block) {
        List<E> list = toList(col);
        R result = null;
        for (int i = 0; i < list.size(); i += n)
            result = block.call(list.subList(i, i + n - 1));
        return result;
    }

    /**
     * @see #toList(Iterable)
     */
    public static <E> List<E> entries(Iterable<E> col) {
        return toList(col);
    }

    /**
     * Returns a list containing the items in collection.
     */
    public static <E> List<E> toList(Iterable<E> col) {
        if (col instanceof Collection<?>)
            return new ArrayList<E>((Collection<E>) col);

        List<E> result = new ArrayList<E>();
        for (E each : col)
            result.add(each);
        return result;
    }

    /**
     * Creates a new Set containing the elements of the given collection.
     */
    public static <E> Set<E> toSet(Iterable<E> col) {
        return new HashSet<E>(toList(col));
    }

    /**
     * Creates a new Set containing the elements of the given collection, the
     * elements are preprocessed by the given block.
     */
    public static <E, R> Set<R> toSet(Iterable<E> col, Fn1<E, R> block) {
        return new HashSet<R>(collect(col, block));
    }

    /**
     * @see #grep(Iterable, Pattern)
     */
    public static <E> List<E> grep(Iterable<E> col, String pattern) {
        return grep(col, Pattern.compile(pattern));
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     */
    public static <E> List<E> grep(Iterable<E> col, Pattern pattern) {
        List<E> result = new ArrayList<E>();
        for (E each : col)
            if (pattern.matcher(each.toString()).matches())
                result.add(each);
        return result;
    }

    /**
     * @see #grep(Iterable, Pattern, Fn1)
     */
    public static <E, R> List<R> grep(Iterable<E> col, String pattern, Fn1<E, R> block) {
        return grep(col, Pattern.compile(pattern), block);
    }

    /**
     * Returns a list of every element in collection for which pattern matches.
     * Each matching element is passed to tje block, and its result is stored in
     * the output list.
     */
    public static <E, R> List<R> grep(Iterable<E> col, Pattern pattern, Fn1<E, R> block) {
        List<R> result = new ArrayList<R>();
        for (E each : col)
            if (pattern.matcher(each.toString()).matches())
                result.add(block.call(each));
        return result;
    }

    /**
     * @see #member(Iterable, Object)
     */
    public static <E> boolean includes(Iterable<E> col, Object obj) {
        return toList(col).contains(obj);
    }

    /**
     * Returns true if any member of collection equals obj. Equality is tested
     * using {@link Object#equals(Object)}.
     */
    public static <E> boolean member(Iterable<E> col, Object obj) {
        return includes(col, obj);
    }

    /**
     * Executes the block for every line in string.
     */
    public static <R> String eachLine(String string, Fn1<String, R> block) {
        eachLine(new StringReader(string), block);
        return string;
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
     * Returns a new list with the results of running block once for every
     * element in collection.
     */
    public static <E, R> List<R> collect(Iterable<E> col, Fn1<E, R> block) {
        List<R> result = new ArrayList<R>();
        for (E each : col)
            result.add(block.call(each));
        return result;
    }

    /**
     * @see #select(Iterable, Fn1)
     */
    public static <E> List<E> findAll(Iterable<E> col, Fn1<E, Boolean> block) {
        return select(col, block);
    }

    /**
     * Returns an list containing all Map.Entry pairs for which the block
     * returns true.
     */
    public static <K, V> List<Map.Entry<K, V>> select(Map<K, V> map, Fn2<K, V, Boolean> block) {
        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
        for (Map.Entry<K, V> each : map.entrySet())
            if (block.call(each.getKey(), each.getValue()))
                result.add(each);
        return result;
    }

    /**
     * Returns an list containing all elements of collection for which block is
     * not false.
     */
    public static <E> List<E> select(Iterable<E> col, Fn1<E, Boolean> block) {
        List<E> result = new ArrayList<E>();
        for (E each : col)
            if (block.call(each))
                result.add(each);
        return result;
    }

    /**
     * Returns an list containing all elements of collection for which block is
     * false.
     */
    public static <E> List<E> reject(Iterable<E> col, Fn1<E, Boolean> block) {
        List<E> result = new ArrayList<E>();
        for (E each : col)
            if (!block.call(each))
                result.add(each);
        return result;
    }

    /**
     * Returns the object in collection with the maximum value. This form
     * assumes all objects implement {@link Comparable}
     */
    public static <E extends Object & Comparable<? super E>> E max(Iterable<E> col) {
        List<E> sorted = sort(col);
        if (sorted.isEmpty())
            return null;
        return sorted.get(sorted.size() - 1);
    }

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static <E extends Object & Comparable<? super E>> E min(Iterable<E> col, Fn2<E, E, Integer> block) {
        List<E> sorted = sort(col, block);
        if (sorted.isEmpty())
            return null;
        return sorted.get(sorted.size() - 1);
    }

    /**
     * Returns the object in collection with the minimum value. This form
     * assumes all objects implement {@link Comparable}.
     */
    public static <E extends Object & Comparable<? super E>> E min(Iterable<E> col) {
        List<E> sorted = sort(col);
        if (sorted.isEmpty())
            return null;
        return sorted.get(0);
    }

    /**
     * Returns the object in collection with the maximum value. This form uses
     * the block to {@link Comparator#compare}.
     */
    public static <E extends Object & Comparable<? super E>> E max(Iterable<E> col, Fn2<E, E, Integer> block) {
        List<E> sorted = sort(col, block);
        if (sorted.isEmpty())
            return null;
        return sorted.get(0);
    }

    /**
     * Returns a list containing the items in collection sorted, according to
     * their own compareTo method.
     */
    public static <E extends Object & Comparable<? super E>> List<E> sort(Iterable<E> col) {
        return sort(col, (Comparator<E>) null);
    }

    /**
     * Returns a list containing the items in collection sorted by using the
     * results of the supplied block.
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> sort(Iterable<E> col, Fn2<E, E, Integer> block) {
        return sort(col, block.as(Comparator.class));
    }

    /**
     * Sorts collection using a set of keys generated by mapping the values in
     * collection through the given block.
     */
    public static <E, R extends Object & Comparable<? super R>> List<E> sortBy(Iterable<E> col, final Fn1<E, R> block) {
        return sort(col, new Comparator<E>() {
            public int compare(E o1, E o2) {
                return block.call(o1).compareTo(block.call(o2));
            }
        });
    }

    private static <E> List<E> sort(Iterable<E> col, Comparator<E> comparator) {
        List<E> result = toList(col);
        Collections.sort(result, comparator);
        return result;
    }

    /**
     * Returns two lists, the first containing the elements of collection for
     * which the block evaluates to true, the second containing the rest.
     */
    @SuppressWarnings("unchecked")
    public static <E> List<List<E>> partition(Iterable<E> col, Fn1<E, Boolean> block) {
        List<E> selected = new ArrayList<E>();
        List<E> rejected = new ArrayList<E>();
        for (E each : col)
            if (block.call(each))
                selected.add(each);
            else
                rejected.add(each);
        return new ArrayList<List<E>>(asList(selected, rejected));
    }

    /**
     * @see #detect(Iterable, Fn1)
     */
    public static <E> E find(Iterable<E> col, Fn1<E, Boolean> block) {
        return detect(col, block);
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns null.
     */
    public static <E> E detect(Iterable<E> col, Fn1<E, Boolean> block) {
        return detect(col, block, null);
    }

    /**
     * @see #detect(Iterable, Fn1, Object)
     */
    public static <E> E find(Iterable<E> col, Fn1<E, Boolean> block, E ifNone) {
        return detect(col, block, ifNone);
    }

    /**
     * Passes each entry in collection to block. Returns the first for which
     * block is not false. If no object matches, it returns ifNone.
     */
    public static <E> E detect(Iterable<E> col, Fn1<E, Boolean> block, E ifNone) {
        for (E each : col)
            if (block.call(each))
                return each;
        return ifNone;
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block ever returns a value other than false or null.
     */
    public static <E> boolean any(Iterable<E> col, Fn1<E, ?> block) {
        for (E each : col) {
            Object result = block.call(each);
            if (result != FALSE && result != null)
                return true;
        }
        return false;
    }

    /**
     * Passes each element of the collection to the given block. The method
     * returns true if the block never returns false or null.
     */
    public static <E> boolean all(Iterable<E> col, Fn1<E, ?> block) {
        for (E each : col) {
            Object result = block.call(each);
            if (result == FALSE || result == null)
                return false;
        }
        return true;
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form lets you supply an
     * initial value for memo.
     */
    public static <E, R> R inject(Iterable<E> col, R initial, Fn2<R, E, R> block) {
        for (E each : col)
            initial = block.call(initial, each);
        return initial;
    }

    /**
     * Combines the elements of collection by applying the block to an
     * accumulator value (memo) and each element in turn. At each step, memo is
     * set to the value returned by the block. This form uses the first element
     * of the collection as a the initial value (and skips that element while
     * iterating).
     */
    public static <E> E inject(Iterable<E> col, Fn2<E, E, E> block) {
        Iterator<E> i = col.iterator();
        E initial = i.next();
        while (i.hasNext())
            initial = block.call(initial, i.next());
        return initial;
    }

    /**
     * Named parameter for detect.
     * 
     * @see #detect(Iterable, Fn1, Object)
     * @see #find(Iterable, Fn1, Object)
     */
    public static <R> R ifNone(R defaultValue) {
        return defaultValue;
    }
}
