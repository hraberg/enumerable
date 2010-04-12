package lambda.extra166y;

import static extra166y.ParallelLongArray.*;
import static lambda.Parameters.*;
import static lambda.extra166y.LambdaOps.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

import extra166y.ParallelLongArray;
import extra166y.Ops.IntToLong;
import extra166y.Ops.LongPredicate;

/*
 * This test was adapted from the extra166y API specs here:
 * http://gee.cs.oswego
 * .edu/dl/jsr166/dist/extra166ydocs/extra166y/ParallelLongArray.html
 */
public class ParallelLongArraySieveTest {
    static final int CERTAINTY = 8;
    static final int N = 20;

    @Test
    public void parallelLongArraySieveUsingInnerClasses() {
        // create array of divisors
        ParallelLongArray a = create(N - 1, defaultExecutor());
        a.replaceWithMappedIndex(add2);

        int i = 0;
        long p = 2;
        while (p * p < N) { // repeatedly filter
            a = a.withFilter(notDivisibleBy(p)).all();
            p = a.get(++i);
        }

        // check result
        assertTrue(a.withFilter(notProbablePrime).isEmpty());
    }

    IntToLong add2 = new IntToLong() {
        public long op(int i) {
            return i + 2;
        }
    };

    LongPredicate notDivisibleBy(final long p) {
        return new LongPredicate() {
            public boolean op(long n) {
                return n <= p || (n % p) != 0;
            }
        };
    }

    LongPredicate notProbablePrime = new LongPredicate() {
        public boolean op(long n) {
            return !BigInteger.valueOf(n).isProbablePrime(CERTAINTY);
        }
    };

    @Test
    public void parallelLongArraySieveUsingLambdaOps() {
        // create array of divisors
        ParallelLongArray a = create(N - 1, defaultExecutor());
        IntToLong add2 = op(idx, (long) idx + 2);
        a.replaceWithMappedIndex(add2);

        int i = 0;
        long p = 2;
        while (p * p < N) { // repeatedly filter
            a = a.withFilter(op(l, l <= p || (l % p) != 0)).all();
            p = a.get(++i);
        }

        // check result
        LongPredicate notProbablePrime = op(l, !BigInteger.valueOf(l).isProbablePrime(CERTAINTY));
        assertTrue(a.withFilter(notProbablePrime).isEmpty());
    }
}
