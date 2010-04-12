package lambda.enumerable.collection;

import java.util.ArrayList;
import java.util.Collection;

public class ECollection<E> extends EIterable<E> implements Collection<E> {
    public ECollection() {
        this(new ArrayList<E>());
    }

    public ECollection(Collection<E> collection) {
        super(collection);
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