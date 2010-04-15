/**
 * 
 */
package lambda.enumerable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lambda.enumerable.collection.EnumerableModule;

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