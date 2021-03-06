package org.enumerable.lambda.enumerable.collection;

import java.util.ArrayList;
import java.util.Collection;

import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Fn2;


/**
 * A decorator for {@link Collection} which includes the
 * {@link EnumerableModule} via {@link EIterable}.
 */
public class ECollection<E> extends EIterable<E> implements Collection<E> {
    public ECollection() {
        this(new ArrayList<E>());
    }

    public ECollection(Collection<E> collection) {
        super(collection);
    }

    public <R> ECollection<E> each(Fn1<? super E, R> block) {
        return (ECollection<E>) super.each(block);
    }

    public <R> ECollection<E> eachWithIndex(Fn2<? super E, Integer, R> block) {
        return (ECollection<E>) super.eachWithIndex(block);
    }

    public <R> ECollection<E> reverseEach(Fn1<? super E, R> block) {
        return (ECollection<E>) super.reverseEach(block);
    }

    public Collection<E> delegate() {
        return (Collection<E>) iterable;
    }

    public boolean add(E e) {
        return delegate().add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        return delegate().addAll(c);
    }

    public void clear() {
        delegate().clear();
    }

    public boolean contains(Object o) {
        return delegate().contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return delegate().containsAll(c);
    }

    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    public boolean remove(Object o) {
        return delegate().remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return delegate().removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return delegate().retainAll(c);
    }

    public int size() {
        return delegate().size();
    }

    public Object[] toArray() {
        return delegate().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return delegate().toArray(a);
    }
}