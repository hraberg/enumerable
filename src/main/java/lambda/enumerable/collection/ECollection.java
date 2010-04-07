package lambda.enumerable.collection;

import java.util.ArrayList;
import java.util.Collection;

public class ECollection<E> extends EIterable<E> implements Collection<E> {
    private final Collection<E> collection; 

    ECollection() {
        this(new ArrayList<E>());
    }

    ECollection(Collection<E> collection) {
        super(collection);
        this.collection = collection;
    }
    
    public Collection<E> delegate() {
        return collection;
    }

    public boolean add(E e) {
        return collection.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        return collection.addAll(c);
    }

    public void clear() {
        collection.clear();
    }

    public boolean contains(Object o) {
        return collection.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public boolean remove(Object o) {
        return collection.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return collection.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return collection.retainAll(c);
    }

    public int size() {
        return collection.size();
    }

    public Object[] toArray() {
        return collection.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }
}