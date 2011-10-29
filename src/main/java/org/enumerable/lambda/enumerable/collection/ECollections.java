package org.enumerable.lambda.enumerable.collection;

import static org.enumerable.lambda.exception.UncheckedException.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Static factory methods for the ECollections classes. These methods offer two
 * benefits over calling the respective constructors. First the type parameters
 * need not to be specified, because the compiler can infer type parameters for
 * static methods. As a second advantage there is a vararg parameter that takes
 * initial elements, thus providing poor man's collection literals.
 * 
 * E.g.: <code>
 * EList<String> list = list("foo", "bar");
 * 
 * EMap<String, String> map = orderedMap(pair("name", "Felix"),
 *                                      pair("title", "Gentleman Developer"));
 * 
 * </code>
 */
public class ECollections {

    /**
     * @param entries
     *            initial elements
     * @return a mutable unordered EMap with the given entries as initial
     *         elements
     */
    public static <A, B> EMap<A, B> map(Map.Entry<A, B>... entries) {
        EMap<A, B> map = new EMap<A, B>(new HashMap<A, B>(entries.length));
        for (Map.Entry<A, B> entry : entries)
            map.put(entry.getKey(), entry.getValue());
        return map;
    }

    /**
     * @param entries
     *            initial elements
     * @return a mutable unordered EMap with the given entries as initial
     *         elements
     */
    public static <A, B> EMap<A, B> map(Collection<Map.Entry<A, B>> entries) {
        EMap<A, B> map = new EMap<A, B>(new HashMap<A, B>(entries.size()));
        for (Map.Entry<A, B> pair : entries)
            map.put(pair.getKey(), pair.getValue());
        return map;
    }

    /**
     * @param entries
     *            initial elements
     * @return a mutable ordered EMap with the given entries as initial elements
     */
    public static <A, B> EMap<A, B> orderedMap(Map.Entry<A, B>... entries) {
        EMap<A, B> map = new EMap<A, B>(new LinkedHashMap<A, B>(entries.length));
        for (Map.Entry<A, B> entry : entries)
            map.put(entry.getKey(), entry.getValue());
        return map;
    }

    /**
     * @param entries
     *            initial elements
     * @return a mutable ordered EMap with the given entries as initial elements
     */
    public static <A, B> EMap<A, B> orderedMap(Collection<Map.Entry<A, B>> entries) {
        EMap<A, B> map = new EMap<A, B>(new LinkedHashMap<A, B>(entries.size()));
        for (Map.Entry<A, B> pair : entries)
            map.put(pair.getKey(), pair.getValue());
        return map;
    }

    /**
     * @param elements
     *            initial elements
     * @return a mutable ESet with the given elements as initial elements
     */
    public static <E> ESet<E> set(E... elements) {
        ESet<E> set = new ESet<E>(new HashSet<E>(elements.length));
        set.addAll(Arrays.asList(elements));
        return set;
    }

    /**
     * @param elements
     *            initial elements
     * @return a mutable sorted ESet with the given elements as initial elements
     */
    public static <E> ESet<E> sortedSet(E... elements) {
        ESet<E> set = new ESet<E>(new TreeSet<E>());
        set.addAll(Arrays.asList(elements));
        return set;
    }

    /**
     * @param elements
     *            initial elements
     * @return a mutable EList with the given elements as initial elements
     */
    public static <E> EList<E> list(E... elements) {
        EList<E> list = new EList<E>(elements.length);
        list.addAll(Arrays.asList(elements));
        return list;
    }

    /**
     * @param first
     *            the first element of the pair
     * @param second
     *            the second element of the pair
     * @return a new Pair containing first and second.
     */
    public static <A, B> Pair<A, B> pair(A first, B second) {
        return new Pair<A, B>(first, second);
    }

    /**
     * @see #pair(Object first, Object second)
     */
    public static <A, B> Pair<A, B> cons(A first, B second) {
        return pair(first, second);
    }

    /**
     * Returns the lines of the String as an {@link EList}.
     */
    public static EList<String> lines(String string) {
        return lines(new StringReader(string));
    }

    /**
     * Returns the lines of the Reader as an {@link EList}.
     */
    public static EList<String> lines(Reader reader) {
        try {
            EList<String> result = new EList<String>();
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
                result.add(line);
            return result;
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Returns the chars of the String as an {@link EList}.
     */
    public static EList<Character> chars(String string) {
        EList<Character> result = new EList<Character>();
        for (char c : string.toCharArray())
            result.add(c);
        return result;
    }
}
