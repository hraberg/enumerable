package lambda.enumerable.collection;

import java.util.List;

public interface EList<E> extends ECollection<E>, List<E> {
    EList<E> subList(int fromIndex, int toIndex);
}
