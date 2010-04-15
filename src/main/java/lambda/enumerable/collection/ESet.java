package lambda.enumerable.collection;

import java.util.HashSet;
import java.util.Set;

import lambda.Fn1;
import lambda.Fn2;

/**
 * A decorator for {@link Set}, which includes the {@link EnumerableModule} via
 * {@link EIterable}.
 */
public class ESet<E> extends ECollection<E> implements Set<E> {
    public ESet() {
        this(new HashSet<E>());
    }

    public ESet(Set<E> set) {
        super(set);
    }

    public <R> ESet<E> each(Fn1<E, R> block) {
        return (ESet<E>) super.each(block);
    }

    public <R> ESet<E> eachWithIndex(Fn2<E, Integer, R> block) {
        return (ESet<E>) super.eachWithIndex(block);
    }

    public <R> ESet<E> reverseEach(Fn1<E, R> block) {
        return (ESet<E>) super.reverseEach(block);
    }

    public Set<E> delegate() {
        return (Set<E>) iterable;
    }
}
