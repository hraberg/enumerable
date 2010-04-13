package lambda.enumerable.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;

public class EIterable<E> implements Iterable<E>, IEnumerable<E> {
    @SuppressWarnings("unchecked")
    public static <T, R extends EIterable<T>> R from(Iterable<T> iterable) {
        if (iterable instanceof EIterable<?>)
            return (R) iterable;
        if (iterable instanceof List<?>)
            return (R) new EList<T>((List<T>) iterable);
        if (iterable instanceof Set<?>)
            return (R) new ESet<T>((Set<T>) iterable);
        if (iterable instanceof Collection<?>)
            return (R) new ECollection<T>((Collection<T>) iterable);
        return (R) new EIterable<T>(iterable);
    }

    protected final Iterable<E> iterable;

    public EIterable(Iterable<E> iterable) {
        this.iterable = iterable;
    }

    public boolean all(Fn1<E, ?> block) {
        return Enumerable.all(iterable, block);
    }

    public boolean any(Fn1<E, ?> block) {
        return Enumerable.any(iterable, block);
    }

    public <R> EList<R> collect(Fn1<E, R> block) {
        return Enumerable.collect(iterable, block);
    }

    public int count() {
        return Enumerable.count(iterable);
    }

    public int count(E obj) {
        return Enumerable.count(iterable, obj);
    }

    public int count(Fn1<E, Boolean> block) {
        return Enumerable.count(iterable, block);
    }

    public <R> Object cycle(int times, Fn1<E, R> block) {
        return Enumerable.cycle(iterable, times, block);
    }

    public Iterable<E> delegate() {
        return iterable;
    }

    public E detect(E ifNone, Fn1<E, Boolean> block) {
        return Enumerable.detect(iterable, ifNone, block);
    }

    public E detect(Fn1<E, Boolean> block) {
        return Enumerable.detect(iterable, block);
    }

    public EList<E> drop(int n) {
        return Enumerable.drop(iterable, n);
    }

    public EList<E> dropWhile(Fn1<E, Boolean> block) {
        return Enumerable.dropWhile(iterable, block);
    }

    public <R> EIterable<E> each(Fn1<E, R> block) {
        return Enumerable.each(iterable, block);
    }

    public <R> Object eachCons(int n, Fn1<List<E>, R> block) {
        return Enumerable.eachCons(iterable, n, block);
    }

    public <R> Object eachSlice(int n, Fn1<List<E>, R> block) {
        return Enumerable.eachSlice(iterable, n, block);
    }

    public <R> EIterable<E> eachWithIndex(Fn2<E, Integer, R> block) {
        return Enumerable.eachWithIndex(iterable, block);
    }

    public <M, R> M eachWithObject(M memo, Fn2<E, M, R> block) {
        return Enumerable.eachWithObject(iterable, memo, block);
    }

    public EList<E> entries() {
        return Enumerable.entries(iterable);
    }

    public E find(E ifNone, Fn1<E, Boolean> block) {
        return Enumerable.find(iterable, ifNone, block);
    }

    public E find(Fn1<E, Boolean> block) {
        return Enumerable.find(iterable, block);
    }

    public EList<E> findAll(Fn1<E, Boolean> block) {
        return Enumerable.findAll(iterable, block);
    }

    public int findIndex(Fn1<E, Boolean> block) {
        return Enumerable.findIndex(iterable, block);
    }

    public E first() {
        return Enumerable.first(iterable);
    }

    public EList<E> first(int n) {
        return Enumerable.first(iterable, n);
    }

    public EList<E> grep(Pattern pattern) {
        return Enumerable.grep(iterable, pattern);
    }

    public <R> EList<R> grep(Pattern pattern, Fn1<E, R> block) {
        return Enumerable.grep(iterable, pattern, block);
    }

    public EList<E> grep(String pattern) {
        return Enumerable.grep(iterable, pattern);
    }

    public <R> EList<R> grep(String pattern, Fn1<E, R> block) {
        return Enumerable.grep(iterable, pattern, block);
    }

    public <R> EMap<R, EList<E>> groupBy(Fn1<E, R> block) {
        return Enumerable.groupBy(iterable, block);
    }

    public boolean includes(Object obj) {
        return Enumerable.includes(iterable, obj);
    }

    public E inject(Fn2<E, E, E> block) {
        return Enumerable.inject(iterable, block);
    }

    public <R> R inject(R initial, Fn2<R, E, R> block) {
        return Enumerable.inject(iterable, initial, block);
    }

    public Iterator<E> iterator() {
        return iterable.iterator();
    }

    public <R> EList<R> map(Fn1<E, R> block) {
        return Enumerable.map(iterable, block);
    }

    @SuppressWarnings("unchecked")
    public E max() {
        return (E) Enumerable.max((EIterable<? extends Comparable>) iterable);
    }

    public E max(Fn2<E, E, Integer> block) {
        return Enumerable.max(iterable, block);
    }

    public <R extends Object & Comparable<? super R>> E maxBy(Fn1<E, R> block) {
        return Enumerable.maxBy(iterable, block);
    }

    public boolean member(Object obj) {
        return Enumerable.member(iterable, obj);
    }

    @SuppressWarnings("unchecked")
    public E min() {
        return (E) Enumerable.max((EIterable<? extends Comparable>) iterable);
    }

    public E min(Fn2<E, E, Integer> block) {
        return Enumerable.min(iterable, block);
    }

    public <R extends Object & Comparable<? super R>> E minBy(Fn1<E, R> block) {
        return Enumerable.minBy(iterable, block);
    }

    public EList<E> minMax() {
        return Enumerable.minMax(iterable);
    }

    public EList<E> minMax(Fn2<E, E, Integer> block) {
        return Enumerable.minMax(iterable, block);
    }

    public <R extends Object & Comparable<? super R>> EList<E> minMaxBy(Fn1<E, R> block) {
        return Enumerable.minMaxBy(iterable, block);
    }

    public boolean none(Fn1<E, ?> block) {
        return Enumerable.none(iterable, block);
    }

    public boolean one(Fn1<E, ?> block) {
        return Enumerable.one(iterable, block);
    }

    public EList<EList<E>> partition(Fn1<E, Boolean> block) {
        return Enumerable.partition(iterable, block);
    }

    public E reduce(Fn2<E, E, E> block) {
        return Enumerable.reduce(iterable, block);
    }

    public <R> R reduce(R initial, Fn2<R, E, R> block) {
        return Enumerable.reduce(iterable, initial, block);
    }

    public EList<E> reject(Fn1<E, Boolean> block) {
        return Enumerable.reject(iterable, block);
    }

    public <R> IEnumerable<E> reverseEach(Fn1<E, R> block) {
        return Enumerable.reverseEach(iterable, block);
    }

    public EList<E> select(Fn1<E, Boolean> block) {
        return Enumerable.select(iterable, block);
    }

    @SuppressWarnings("unchecked")
    public EList<E> sort() {
        return Enumerable.sort((Iterable<? extends Comparable>) iterable);
    }

    public EList<E> sort(Fn2<E, E, Integer> block) {
        return Enumerable.sort(iterable, block);
    }

    public <R extends Object & Comparable<? super R>> EList<E> sortBy(Fn1<E, R> block) {
        return Enumerable.sortBy(iterable, block);
    }

    public EList<E> take(int n) {
        return Enumerable.take(iterable, n);
    }

    public EList<E> takeWhile(Fn1<E, Boolean> block) {
        return Enumerable.takeWhile(iterable, block);
    }

    public EList<E> toList() {
        return Enumerable.toList(iterable);
    }

    public ESet<E> toSet() {
        return Enumerable.toSet(iterable);
    }

    public <R> ESet<R> toSet(Fn1<E, R> block) {
        return Enumerable.toSet(iterable, block);
    }

    public String toString() {
        return iterable.toString();
    }

    public EList<EList<?>> zip(Iterable<?>... args) {
        return Enumerable.zip(iterable, args);
    }

    public boolean equals(Object obj) {
        if (obj instanceof EIterable<?>)
            return this.iterable.equals(((EIterable<?>) obj).iterable);
        if (obj instanceof Iterable<?>)
            return this.iterable.equals(obj);
        return false;
    }

    public int hashCode() {
        return iterable.hashCode();
    }
}
