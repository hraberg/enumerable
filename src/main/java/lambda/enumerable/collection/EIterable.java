package lambda.enumerable.collection;

import java.util.Iterator;

/**
 * A decorator for Iterable and the actual implementation of the Enumerable
 * module.
 */
public class EIterable<E> extends EnumerableModule<E> {
    protected final Iterable<E> iterable;

    public EIterable(Iterable<E> iterable) {
        this.iterable = iterable;
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

    public Iterator<E> iterator() {
        return iterable.iterator();
    }

    public Iterable<E> delegate() {
        return iterable;
    }

    public String toString() {
        return iterable.toString();
    }
}
