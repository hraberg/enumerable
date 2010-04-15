package lambda.enumerable.collection;

import static java.lang.Boolean.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;
import lambda.enumerable.EnumerableArrays;

/**
 * The Enumerable mixin provides collection classes with several traversal and
 * searching methods, and with the ability to sort. The class must provide a
 * method {@link Iterable#iterator()}, which yields successive members of the
 * collection. If {@link #max}, {@link #min}, or {@link #sort} is used, the
 * objects in the collection must also implement a meaningful
 * {@link Comparable#compareTo(Object)} operator, as these methods rely on an
 * ordering between members of the collection.
 * <p>
 * It can be mixed in either by using the decorators ({@link EIterable},
 * {@link ECollection}, {@link EList}, {@link ESet} and {@link EMap}) or by
 * subclassing and provide {@link Iterable#iterator()}.
 * <p>
 * The static methods in the facades {@link Enumerable} and
 * {@link EnumerableArrays} use this implementation via the decorators.
 */
public abstract class EnumerableModule<E> implements IEnumerable<E> {
    @SuppressWarnings("unchecked")
    public static <T, R extends EnumerableModule<T>> R extend(Iterable<T> iterable) {
        if (iterable instanceof EnumerableModule<?>)
            return (R) iterable;
        if (iterable instanceof List<?>)
            return (R) new EList<T>((List<T>) iterable);
        if (iterable instanceof Set<?>)
            return (R) new ESet<T>((Set<T>) iterable);
        if (iterable instanceof Collection<?>)
            return (R) new ECollection<T>((Collection<T>) iterable);
        return (R) new EIterable<T>(iterable);
    }

    public static <K, V> EMap<K, V> extend(Map<K, V> map) {
        if (map instanceof EMap<?, ?>)
            return (EMap<K, V>) map;
        return new EMap<K, V>(map);
    }

    public boolean all(Fn1<E, ?> block) {
        for (E each : this) {
            Object result = block.call(each);
            if (isFalseOrNull(result))
                return false;
        }
        return true;
    }

    public boolean any(Fn1<E, ?> block) {
        for (E each : this) {
            Object result = block.call(each);
            if (isNotFalseOrNull(result))
                return true;
        }
        return false;
    }

    public <R> EList<R> collect(Fn1<E, R> block) {
        EList<R> result = new EList<R>();
        for (E each : this)
            result.add(block.call(each));
        return result;
    }

    @SuppressWarnings("unchecked")
    public int count() {
        if (this instanceof Collection<?>)
            return ((Collection<E>) this).size();
        int count = 0;
        for (Iterator<E> iterator = this.iterator(); iterator.hasNext(); iterator.next())
            count++;
        return count;
    }

    public int count(E obj) {
        int count = 0;
        for (E each : this)
            if (obj.equals(each))
                count++;
        return count;
    }

    public int count(Fn1<E, Boolean> block) {
        return select(block).size();
    }

    public <R> EList<E> cycle(int times, Fn1<E, R> block) {
        EList<E> result = new EList<E>();
        while (times-- > 0) {
            for (E each : this) {
                block.call(each);
                result.add(each);
            }
        }
        return result;
    }

    public E detect(Fn1<E, Boolean> block) {
        return detect(null, block);
    }

    public E detect(E ifNone, Fn1<E, Boolean> block) {
        for (E each : this)
            if (block.call(each))
                return each;
        return ifNone;
    }

    public EList<E> drop(int n) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (n-- <= 0)
                result.add(each);
        return result;
    }

    public EList<E> dropWhile(Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E next : this)
            if (!result.isEmpty() || !block.call(next))
                result.add(next);
        return result;
    }

    public <R> EnumerableModule<E> each(Fn1<E, R> block) {
        for (E each : this)
            block.call(each);
        return this;
    }

    public <R> Object eachCons(int n, Fn1<List<E>, R> block) {
        List<E> list = asNewList();
        for (int i = 0; i + n <= list.size(); i++)
            if (n + i <= list.size())
                block.call(list.subList(i, i + n));
        return null;
    }

    public <R> Object eachSlice(int n, Fn1<List<E>, R> block) {
        List<E> list = asNewList();
        for (int i = 0; i <= list.size(); i += n)
            if (i + n > list.size())
                block.call(list.subList(i, list.size()));
            else
                block.call(list.subList(i, i + n));
        return null;
    }

    public <R> EnumerableModule<E> eachWithIndex(Fn2<E, Integer, R> block) {
        int i = 0;
        for (E each : this)
            block.call(each, i++);
        return this;
    }

    public <M, R> M eachWithObject(M memo, Fn2<E, M, R> block) {
        for (E each : this)
            block.call(each, memo);
        return memo;
    }

    public EList<E> entries() {
        return toList();
    }

    public E find(Fn1<E, Boolean> block) {
        return detect(block);
    }

    public E find(E ifNone, Fn1<E, Boolean> block) {
        return detect(ifNone, block);
    }

    public EList<E> findAll(Fn1<E, Boolean> block) {
        return select(block);
    }

    public int findIndex(Fn1<E, Boolean> block) {
        int index = 0;
        for (E each : this)
            if (block.call(each))
                return index;
            else
                index++;
        return -1;
    }

    public E first() {
        for (E each : this)
            return each;
        return null;
    }

    public EList<E> first(int n) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (n-- > 0)
                result.add(each);
            else
                return result;
        return result;
    }

    public EList<E> grep(Pattern pattern) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (pattern.matcher(each.toString()).matches())
                result.add(each);
        return result;
    }

    public <R> EList<R> grep(Pattern pattern, Fn1<E, R> block) {
        EList<R> result = new EList<R>();
        for (E each : this)
            if (pattern.matcher(each.toString()).matches())
                result.add(block.call(each));
        return result;
    }

    public EList<E> grep(String pattern) {
        return grep(Pattern.compile(pattern));
    }

    public <R> EList<R> grep(String pattern, Fn1<E, R> block) {
        return grep(Pattern.compile(pattern), block);
    }

    public <R> EMap<R, EList<E>> groupBy(Fn1<E, R> block) {
        EMap<R, EList<E>> result = new EMap<R, EList<E>>();
        for (E each : this) {
            R key = block.call(each);
            if (!result.containsKey(key))
                result.put(key, new EList<E>());
            result.get(key).add(each);
        }
        return result;
    }

    public boolean includes(Object obj) {
        return asNewList().contains(obj);
    }

    public E inject(Fn2<E, E, E> block) {
        Iterator<E> i = this.iterator();
        if (!i.hasNext())
            return null;
        E initial = i.next();
        while (i.hasNext())
            initial = block.call(initial, i.next());
        return initial;
    }

    public <R> R inject(R initial, Fn2<R, E, R> block) {
        for (E each : this)
            initial = block.call(initial, each);
        return initial;
    }

    public <R> EList<R> map(Fn1<E, R> block) {
        return collect(block);
    }

    public E max() {
        Comparator<E> reverseOrder = reverseOrder();
        return min(reverseOrder);
    }

    @SuppressWarnings("unchecked")
    public E max(Fn2<E, E, Integer> block) {
        return min(reverseOrder((Comparator<E>) block.as(Comparator.class)));
    }

    public <R extends Object & Comparable<? super R>> E maxBy(Fn1<E, R> block) {
        return min(reverseOrder(new BlockResultComparator<E, R>(block)));
    }

    public boolean member(Object obj) {
        return includes(obj);
    }

    public E min() {
        Comparator<E> naturalOrder = naturalOrder();
        return min(naturalOrder);
    }

    @SuppressWarnings("unchecked")
    public E min(Fn2<E, E, Integer> block) {
        return min((Comparator<E>) block.as(Comparator.class));
    }

    public <R extends Object & Comparable<? super R>> E minBy(Fn1<E, R> block) {
        return min(new BlockResultComparator<E, R>(block));
    }

    public EList<E> minMax() {
        EList<E> result = new EList<E>();
        result.add(min());
        result.add(max());
        return result;
    }

    public EList<E> minMax(Fn2<E, E, Integer> block) {
        EList<E> result = new EList<E>();
        result.add(min(block));
        result.add(max(block));
        return result;
    }

    public <R extends Object & Comparable<? super R>> EList<E> minMaxBy(Fn1<E, R> block) {
        EList<E> result = new EList<E>();
        result.add(minBy(block));
        result.add(maxBy(block));
        return result;
    }

    public boolean none(Fn1<E, ?> block) {
        return !any(block);
    }

    public boolean one(Fn1<E, ?> block) {
        Object match = null;
        for (E each : this) {
            Object result = block.call(each);
            if (isNotFalseOrNull(result))
                if (match != null)
                    return false;
                else
                    match = result;
        }
        return match != null;
    }

    public EList<EList<E>> partition(Fn1<E, Boolean> block) {
        EList<E> selected = new EList<E>();
        EList<E> rejected = new EList<E>();
        for (E each : this)
            if (block.call(each))
                selected.add(each);
            else
                rejected.add(each);
        EList<EList<E>> result = new EList<EList<E>>();
        result.add(selected);
        result.add(rejected);
        return result;
    }

    public E reduce(Fn2<E, E, E> block) {
        return inject(block);
    }

    public <R> R reduce(R initial, Fn2<R, E, R> block) {
        return inject(initial, block);
    }

    public EList<E> reject(Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (!block.call(each))
                result.add(each);
        return result;
    }

    public <R> EnumerableModule<E> reverseEach(Fn1<E, R> block) {
        List<E> result = asNewList();
        Collections.reverse(result);
        new EList<E>(result).each(block);
        return this;
    }

    public EList<E> select(Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (block.call(each))
                result.add(each);
        return result;
    }

    public <K, V> EList<Map.Entry<K, V>> select(Map<K, V> map, Fn2<K, V, Boolean> block) {
        EList<Map.Entry<K, V>> result = new EList<Map.Entry<K, V>>();
        for (Map.Entry<K, V> each : map.entrySet())
            if (block.call(each.getKey(), each.getValue()))
                result.add(each);
        return result;
    }

    public EList<E> sort() {
        return sort((Comparator<E>) null);
    }

    private EList<E> sort(Comparator<E> comparator) {
        List<E> result = asNewList();
        Collections.sort(result, comparator);
        return new EList<E>(result);
    }

    @SuppressWarnings("unchecked")
    public EList<E> sort(Fn2<E, E, Integer> block) {
        return sort(block.as(Comparator.class));
    }

    public <R extends Object & Comparable<? super R>> EList<E> sortBy(final Fn1<E, R> block) {
        return sort(new BlockResultComparator<E, R>(block));
    }

    public EList<E> take(int n) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (n-- > 0)
                result.add(each);
            else
                return result;
        return result;
    }

    public EList<E> takeWhile(Fn1<E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E next : this)
            if (block.call(next))
                result.add(next);
            else
                return result;
        return result;
    }

    public EList<E> toList() {
        return new EList<E>(asNewList());
    }

    public ESet<E> toSet() {
        return new ESet<E>(new HashSet<E>(asNewList()));
    }

    public <R> ESet<R> toSet(Fn1<E, R> block) {
        return new ESet<R>(new HashSet<R>(collect(block)));
    }

    public EList<EList<?>> zip(Iterable<?>... args) {
        EList<EList<?>> allResults = new EList<EList<?>>();

        List<Iterator<?>> iterators = new ArrayList<Iterator<?>>();
        iterators.add(this.iterator());
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

    boolean isNotFalseOrNull(Object obj) {
        return obj != FALSE && obj != null;
    }

    boolean isFalseOrNull(Object result) {
        return !isNotFalseOrNull(result);
    }

    @SuppressWarnings("unchecked")
    List<E> asNewList() {
        if (this instanceof Collection<?>)
            return new ArrayList<E>((Collection<E>) this);

        List<E> result = new ArrayList<E>();
        for (E each : this)
            result.add(each);

        return result;
    }

    E min(Comparator<E> comparator) {
        E result = null;
        for (E each : this)
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

    static final NaturalOrderComparator NATURAL_ORDER = new NaturalOrderComparator();

    static class NaturalOrderComparator implements Comparator<Comparable<Object>> {
        public int compare(Comparable<Object> o1, Comparable<Object> o2) {
            return o1.compareTo(o2);
        }
    }

    @SuppressWarnings("unchecked")
    Comparator<E> naturalOrder() {
        return (Comparator<E>) NATURAL_ORDER;
    }
}