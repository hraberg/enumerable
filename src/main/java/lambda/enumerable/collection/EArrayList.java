package lambda.enumerable.collection;

import java.util.*;
import java.util.regex.Pattern;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;

public class EArrayList<E> extends ArrayList<E> implements EList<E> {
    private static final long serialVersionUID = 9062845492685739292L;
    
    public EArrayList() {
    }

    public EArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public EArrayList(Collection<? extends E> c) {
        super(c);
    }
    
    public EList<E> subList(int fromIndex, int toIndex) {
        return new EArrayList<E>(super.subList(fromIndex, toIndex));
    }

    public boolean all(Fn1<E, ?> block) {
        return Enumerable.all(this, block);
    }

    public boolean any(Fn1<E, ?> block) {
        return Enumerable.any(this, block);
    }

    public <R> EList<R> collect(Fn1<E, R> block) {
        return Enumerable.collect(this, block);
    }

    public E detect(Fn1<E, Boolean> block) {
        return Enumerable.detect(this, block);
    }

    public E detect(Fn1<E, Boolean> block, E ifNone) {
        return Enumerable.detect(this, block, ifNone);
    }

    public <R> EIterable<E> each(Fn1<E, R> block) {
        return Enumerable.each(this, block);
    }

    public <R> Object eachCons(int n, Fn1<List<E>, R> block) {
        return Enumerable.eachCons(this, n, block);
    }

    public <R> Object eachSlice(int n, Fn1<List<E>, R> block) {
        return Enumerable.eachSlice(this, n, block);
    }

    public <R> EIterable<E> eachWithIndex(Fn2<E, Integer, R> block) {
        return Enumerable.eachWithIndex(this, block);
    }

    public EList<E> entries() {
        return Enumerable.entries(this);
    }

    public E find(Fn1<E, Boolean> block) {
        return Enumerable.find(this, block);
    }

    public E find(Fn1<E, Boolean> block, E ifNone) {
        return Enumerable.find(this, block,ifNone);
    }

    public EList<E> findAll(Fn1<E, Boolean> block) {
        return Enumerable.findAll(this, block);
    }

    public EList<E> grep(Pattern pattern) {
        return Enumerable.grep(this, pattern);
    }

    public <R> EList<R> grep(Pattern pattern, Fn1<E, R> block) {
        return Enumerable.grep(this, pattern, block);
    }

    public EList<E> grep(String pattern) {
        return Enumerable.grep(this, pattern);
    }

    public <R> EList<R> grep(String pattern, Fn1<E, R> block) {
        return Enumerable.grep(this, pattern, block);
    }

    public boolean includes(Object obj) {
        return Enumerable.includes(this, obj);
    }

    public E inject(Fn2<E, E, E> block) {
        return Enumerable.inject(this, block);
    }

    public <R> R inject(R initial, Fn2<R, E, R> block) {
        return Enumerable.inject(this, initial, block);
    }

    public <R> EList<R> map(Fn1<E, R> block) {
        return Enumerable.map(this, block);
    }

    @SuppressWarnings("unchecked")
    public E max() {
        return (E) Enumerable.max((EIterable<Comparable>) this);
    }

    public E max(Fn2<E, E, Integer> block) {
        return Enumerable.max(this, block);
    }

    public boolean member(Object obj) {
        return Enumerable.member(this, obj);
    }

    @SuppressWarnings("unchecked")
    public E min() {
        return (E) Enumerable.max((EIterable<Comparable>) this);
    }

    public E min(Fn2<E, E, Integer> block) {
        return Enumerable.min(this, block);
    }

    public EList<EList<E>> partition(Fn1<E, Boolean> block) {
        return Enumerable.partition(this, block);
    }

    public EList<E> reject(Fn1<E, Boolean> block) {
        return Enumerable.reject(this, block);
    }

    public EList<E> select(Fn1<E, Boolean> block) {
        return Enumerable.select(this, block);
    }

    @SuppressWarnings("unchecked")
    public EList<E> sort() {
        return Enumerable.sort((EIterable<Comparable>) this);
    }

    public EList<E> sort(Fn2<E, E, Integer> block) {
        return Enumerable.sort(this, block);
    }

    public <R extends Object & Comparable<? super R>> EList<E> sortBy(Fn1<E, R> block) {
        return Enumerable.sortBy(this, block);
    }

    public EList<E> toList() {
        return Enumerable.toList(this);
    }

    public ESet<E> toSet() {
        return Enumerable.toSet(this);
    }

    public <R> ESet<R> toSet(Fn1<E, R> block) {
        return Enumerable.toSet(this, block);
    }

    public EList<EList<?>> zip(Iterable<?>... args) {
        return Enumerable.zip(this, args);
    }
}
