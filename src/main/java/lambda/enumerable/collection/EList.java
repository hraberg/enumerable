package lambda.enumerable.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class EList<E> extends ECollection<E> implements List<E> {
    public EList() {
        this(new ArrayList<E>());
    }

    public EList(List<E> list) {
        super(list);
    }

    public EList(int length) {
        new ArrayList<E>(length);
    }

    public List<E> delegate() {
        return (List<E>) iterable;
    }

    public void add(int index, E element) {
        delegate().add(index, element);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return delegate().addAll(index, c);
    }

    public E get(int index) {
        return delegate().get(index);
    }

    public int indexOf(Object o) {
        return delegate().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return delegate().lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return delegate().listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return delegate().listIterator(index);
    }

    public E remove(int index) {
        return delegate().remove(index);
    }

    public E set(int index, E element) {
        return delegate().set(index, element);
    }

    public EList<E> subList(int fromIndex, int toIndex) {
        return new EList<E>(delegate().subList(fromIndex, toIndex));
    }
}
