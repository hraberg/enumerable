package lambda.enumerable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.collection.EnumerableModule;

/**
 * An {@link Iterable} that represents a ascending range of integers. Includes
 * the {@link EnumerableModule} by extension.
 */
public class Range extends EnumerableModule<Integer> {
    public final int start, end;
    public final boolean exclusive;

    public Range(int start, int end) {
        this(start, end, false);
    }

    public Range(int start, int end, boolean exclusive) {
        this.start = start;
        this.end = end;
        this.exclusive = exclusive;
    }

    public <R> Range each(Fn1<Integer, R> block) {
        return (Range) super.each(block);
    }

    public <R> Range eachWithIndex(Fn2<Integer, Integer, R> block) {
        return (Range) super.eachWithIndex(block);
    }

    public <R> Range reverseEach(Fn1<Integer, R> block) {
        return (Range) super.reverseEach(block);
    }

    public int[] toArray() {
        int[] array = new int[(exclusive ? end : end + 1) - start];
        int i = 0;
        for (Integer integer : this)
            array[i++] = integer;
        return array;
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int x = start;

            public boolean hasNext() {
                return exclusive ? x < end : x <= end;
            }

            public Integer next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return x++;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}