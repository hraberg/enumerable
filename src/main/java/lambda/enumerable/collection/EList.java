package lambda.enumerable.collection;

import java.util.*;

public class EList<E> extends ECollection<E> implements List<E> {
    private final List<E> list;

    public EList() {
        this(new ArrayList<E>());
    }

    public EList(List<E> list) {
        super(list);
        this.list = list;
    }

    public List<E> delegate() {
        return list;
    }

    public void add(int index, E element) {
        list.add(index, element);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    public E get(int index) {
        return list.get(index);
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    public E remove(int index) {
        return list.remove(index);
    }

    public E set(int index, E element) {
        return list.set(index, element);
    }

    public EList<E> subList(int fromIndex, int toIndex) {
        return new EList<E>(list.subList(fromIndex, toIndex));
    }
}
