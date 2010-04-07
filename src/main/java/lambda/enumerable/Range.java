/**
 * 
 */
package lambda.enumerable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Range implements Iterable<Integer> {
    public final int start, end;
    public final boolean exclusive;

    private final List<Integer> range = new ArrayList<Integer>();
    
    public Range(int start, int end) {
        this(start, end, false);
    }

    public Range(int start, int end, boolean exclusive) {
        this.start = start;
        this.end = end;
        this.exclusive = exclusive;

        for (int x = start; exclusive ? x < end : x <= end; x++)
            range.add(x);
    }
    
    public Integer[] toArray() {
        return range.toArray(new Integer[0]);
    }

    public Iterator<Integer> iterator() {
        return range.iterator();
    }
}