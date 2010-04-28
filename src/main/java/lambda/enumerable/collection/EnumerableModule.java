package lambda.enumerable.collection;

import static java.util.Collections.*;
import static lambda.Fn0.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lambda.Fn0;
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

    public boolean all(Fn1<? super E, ?> block) {
        for (E each : this)
            if (isFalseOrNull(block.call(each)))
                return false;
        return true;
    }

    public boolean all() {
        return all(Fn1.identity());
    }

    public boolean any(Fn1<? super E, ?> block) {
        for (E each : this)
            if (isNotFalseOrNull(block.call(each)))
                return true;
        return false;
    }

    public boolean any() {
        return any(Fn1.identity());
    }

    public <R> EList<R> collect(Fn1<? super E, R> block) {
        EList<R> result = new EList<R>();
        for (E each : this)
            result.add(block.call(each));
        return result;
    }

    @SuppressWarnings({ "unchecked", "unused" })
    public int count() {
        if (this instanceof Collection<?>)
            return ((Collection<E>) this).size();
        int count = 0;
        for (E each : this)
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

    public int count(Fn1<? super E, Boolean> block) {
        return select(block).size();
    }

    public <R> EList<E> cycle(int times, Fn1<? super E, R> block) {
        EList<E> result = new EList<E>();
        while (times-- > 0)
            for (E each : this)
                result.add(each);
        return result.each(block);
    }

    public E detect(Fn1<? super E, Boolean> block) {
        return detect(null, block);
    }

    public E detect(Fn0<E> ifNone, Fn1<? super E, Boolean> block) {
        for (E each : this)
            if (block.call(each))
                return each;
        return ifNone == null ? null : ifNone.call();
    }

    public EList<E> drop(int n) {
        if (n < 0)
            throw new IllegalArgumentException("Attempt to drop negative size");
        EList<E> result = new EList<E>();
        for (E each : this)
            if (n-- <= 0)
                result.add(each);
        return result;
    }

    public EList<E> dropWhile(Fn1<? super E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E next : this)
            if (!result.isEmpty() || !block.call(next))
                result.add(next);
        return result;
    }

    public <R> EnumerableModule<E> each(Fn1<? super E, R> block) {
        for (E each : this)
            block.call(each);
        return this;
    }

    public <R> Object eachCons(int n, Fn1<List<E>, R> block) {
        if (n <= 0)
            throw new IllegalArgumentException("Invalid size");
        List<E> list = asNewList();
        for (int i = 0; i + n <= list.size(); i++)
            if (n + i <= list.size())
                block.call(list.subList(i, i + n));
        return null;
    }

    public <R> Object eachSlice(int n, Fn1<List<E>, R> block) {
        if (n <= 0)
            throw new IllegalArgumentException("Invalid size");
        List<E> list = asNewList();
        for (int i = 0; i <= list.size(); i += n)
            if (i + n > list.size())
                block.call(list.subList(i, list.size()));
            else
                block.call(list.subList(i, i + n));
        return null;
    }

    public <R> EnumerableModule<E> eachWithIndex(Fn2<? super E, Integer, R> block) {
        int i = 0;
        for (E each : this)
            block.call(each, i++);
        return this;
    }

    public <M, R> M eachWithObject(M memo, Fn2<? super E, M, R> block) {
        for (E each : this)
            block.call(each, memo);
        return memo;
    }

    public EList<E> entries() {
        return toList();
    }

    public E find(Fn1<? super E, Boolean> block) {
        return detect(block);
    }

    public E find(Fn0<E> ifNone, Fn1<? super E, Boolean> block) {
        return detect(ifNone, block);
    }

    public EList<E> findAll(Fn1<? super E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (block.call(each))
                result.add(each);
        return result;
    }

    public int findIndex(Fn1<? super E, Boolean> block) {
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
        if (n < 0)
            throw new IllegalArgumentException("Negative array size");
        EList<E> result = new EList<E>();
        for (E each : this)
            if (n-- > 0)
                result.add(each);
            else
                return result;
        return result;
    }

    public EList<E> grep(Pattern pattern) {
        return grep(pattern, Fn1.<E> identity());
    }

    public <R> EList<R> grep(Pattern pattern, Fn1<? super E, R> block) {
        EList<R> result = new EList<R>();
        for (E each : this)
            if (pattern.matcher(each.toString()).matches())
                result.add(block.call(each));
        return result;
    }

    public EList<E> grep(String pattern) {
        return grep(Pattern.compile(pattern));
    }

    public <R> EList<R> grep(String pattern, Fn1<? super E, R> block) {
        return grep(Pattern.compile(pattern), block);
    }

    public <R> EMap<R, EList<E>> groupBy(Fn1<? super E, R> block) {
        EMap<R, EList<E>> result = new EMap<R, EList<E>>(new LinkedHashMap<R, EList<E>>());
        for (E each : this) {
            R key = block.call(each);
            if (!result.containsKey(key))
                result.put(key, new EList<E>());
            result.get(key).add(each);
        }
        return result;
    }

    public boolean include(Object obj) {
        if (obj == null) {
            for (E each : this)
                if (each == null)
                    return true;
        } else {
            for (E each : this)
                if (each != null && each.equals(obj))
                    return true;
        }
        return false;
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

    public <R> EList<R> map(Fn1<? super E, R> block) {
        return collect(block);
    }

    public E max() {
        Comparator<E> reverseOrder = reverseOrder();
        return minInternal(reverseOrder);
    }

    public E max(final Fn2<? super E, ? super E, Integer> block) {
        return minInternal(reverseOrder(new Comparator<E>() {
            public int compare(E o1, E o2) {
                return ((Number) block.call(o1, o2)).intValue();
            }
        }));
    }

    public <R extends Object & Comparable<? super R>> E maxBy(Fn1<? super E, R> block) {
        return minInternal(reverseOrder(new BlockResultComparator<E, R>(block)));
    }

    public boolean member(Object obj) {
        return include(obj);
    }

    public E min() {
        Comparator<E> naturalOrder = naturalOrder();
        return minInternal(naturalOrder);
    }

    public E min(final Fn2<? super E, ? super E, Integer> block) {
        return minInternal(new Comparator<E>() {
            public int compare(E o1, E o2) {
                return ((Number) block.call(o1, o2)).intValue();
            }
        });
    }

    public <R extends Object & Comparable<? super R>> E minBy(Fn1<? super E, R> block) {
        return minInternal(new BlockResultComparator<E, R>(block));
    }

    public EList<E> minMax() {
        EList<E> result = new EList<E>();
        result.add(min());
        result.add(max());
        return result;
    }

    public EList<E> minMax(Fn2<? super E, ? super E, Integer> block) {
        EList<E> result = new EList<E>();
        result.add(min(block));
        result.add(max(block));
        return result;
    }

    public <R extends Object & Comparable<? super R>> EList<E> minMaxBy(Fn1<? super E, R> block) {
        EList<E> result = new EList<E>();
        result.add(minBy(block));
        result.add(maxBy(block));
        return result;
    }

    public boolean none(Fn1<? super E, ?> block) {
        return !any(block);
    }

    public boolean none() {
        return none(Fn1.identity());
    }

    public boolean one(Fn1<? super E, ?> block) {
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

    public boolean one() {
        return one(Fn1.identity());
    }

    public EList<EList<E>> partition(Fn1<? super E, Boolean> block) {
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

    public EList<E> reject(Fn1<? super E, Boolean> block) {
        EList<E> result = new EList<E>();
        for (E each : this)
            if (!block.call(each))
                result.add(each);
        return result;
    }

    public <R> EnumerableModule<E> reverseEach(Fn1<? super E, R> block) {
        List<E> result = asNewList();
        Collections.reverse(result);
        new EList<E>(result).each(block);
        return this;
    }

    public EList<E> select(Fn1<? super E, Boolean> block) {
        return findAll(block);
    }

    public EList<E> sort() {
        return sortInternal((Comparator<E>) null);
    }

    public EList<E> sort(final Fn2<? super E, ? super E, Integer> block) {
        return sortInternal(new Comparator<E>() {
            public int compare(E o1, E o2) {
                return ((Number) block.call(o1, o2)).intValue();
            }
        });
    }

    public <R extends Object & Comparable<? super R>> EList<E> sortBy(final Fn1<? super E, R> block) {
        return sortBySchwartzianTransform(block);
    }

    public EList<E> take(int n) {
        if (n < 0)
            throw new IllegalArgumentException("Attempt to take negative size");
        EList<E> result = new EList<E>();
        for (E each : this)
            if (n-- > 0)
                result.add(each);
            else
                return result;
        return result;
    }

    public EList<E> takeWhile(Fn1<? super E, Boolean> block) {
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

    public <R> ESet<R> toSet(Fn1<? super E, R> block) {
        return new ESet<R>(new HashSet<R>(collect(block)));
    }

    public EList<EList<?>> zip() {
        return zip(new Iterable<?>[0]);
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

    @SuppressWarnings("unchecked")
    List<E> asNewList() {
        if (this instanceof Collection<?>)
            return new ArrayList<E>((Collection<E>) this);

        List<E> result = new ArrayList<E>();
        for (E each : this)
            result.add(each);

        return result;
    }

    E minInternal(Comparator<? super E> comparator) {
        E result = null;
        for (E each : this)
            if (result == null || comparator.compare(each, result) < 0)
                result = each;
        return result;
    }

    EList<E> sortInternal(Comparator<? super E> comparator) {
        List<E> result = asNewList();
        Collections.sort(result, comparator);
        return new EList<E>(result);
    }

    class Pair<R extends Object & Comparable<? super R>> implements Comparable<Pair<R>> {
        R key;
        E value;

        Pair(R key, E value) {
            this.key = key;
            this.value = value;
        }

        public int compareTo(Pair<R> o) {
            return key.compareTo(o.key);
        }
    }

    @SuppressWarnings("unchecked")
    <R extends Object & Comparable<? super R>> EList<E> sortBySchwartzianTransform(Fn1<? super E, R> block) {
        EList<Pair<R>> pairs = new EList<Pair<R>>();
        for (E each : this)
            pairs.add(new Pair<R>(block.call(each), each));
        Collections.sort(pairs);

        EList<E> result = (EList<E>) pairs;
        for (int i = 0; i < pairs.size(); i++)
            result.set(i, pairs.get(i).value);

        return result;
    }

    static class BlockResultComparator<E, R extends Object & Comparable<? super R>> implements Comparator<E> {
        Fn1<? super E, R> block;

        BlockResultComparator(Fn1<? super E, R> block) {
            this.block = block;
        }

        public int compare(E o1, E o2) {
            return block.call(o1).compareTo(block.call(o2));
        }
    }

    static class CachedBlockResultComparator<E, R extends Object & Comparable<? super R>> implements Comparator<E> {
        Map<E, R> cache = new HashMap<E, R>();
        Fn1<? super E, R> block;

        CachedBlockResultComparator(Fn1<? super E, R> block) {
            this.block = block;
        }

        R cached(E e) {
            if (!cache.containsKey(e))
                cache.put(e, block.call(e));
            return cache.get(e);
        }

        public int compare(E o1, E o2) {
            return cached(o1).compareTo(cached(o2));
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
