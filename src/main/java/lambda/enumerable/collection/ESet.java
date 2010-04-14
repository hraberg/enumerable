package lambda.enumerable.collection;

import java.util.HashSet;
import java.util.Set;

/**
 * A decorator for {@link Set}, which includes the Enumerable module via
 * {@link EIterable}.
 */
public class ESet<E> extends ECollection<E> implements Set<E> {
    public ESet() {
        this(new HashSet<E>());
    }

    public ESet(Set<E> set) {
        super(set);
    }

    public Set<E> delegate() {
        return (Set<E>) iterable;
    }
}
