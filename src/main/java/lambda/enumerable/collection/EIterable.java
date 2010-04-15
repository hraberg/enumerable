package lambda.enumerable.collection;

import java.util.Iterator;

import lambda.Fn1;
import lambda.Fn2;

/**
 * A decorator for {@link Iterable} which includes the {@link EnumerableModule}
 * by extension.
 */
public class EIterable<E> extends EnumerableModule<E> {
    protected final Iterable<E> iterable;

    public EIterable(Iterable<E> iterable) {
        this.iterable = iterable;
    }

    public <R> EIterable<E> each(Fn1<E, R> block) {
        return (EIterable<E>) super.each(block);
    }

    public <R> EIterable<E> eachWithIndex(Fn2<E, Integer, R> block) {
        return (EIterable<E>) super.eachWithIndex(block);
    }

    public <R> EIterable<E> reverseEach(Fn1<E, R> block) {
        return (EIterable<E>) super.reverseEach(block);
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
