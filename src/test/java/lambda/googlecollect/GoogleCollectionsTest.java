package lambda.googlecollect;

import static com.google.common.collect.Collections2.*;
import static lambda.Parameters.*;
import static lambda.googlecollect.LambdaGoogleCollections.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import lambda.Lambda;
import lambda.TestBase;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class GoogleCollectionsTest extends TestBase {
    @Test
    public void interactingWithGoogleCollections() throws Exception {
        Collection<String> transform = transform(list("hello", "world"), function(s, s.toUpperCase()));
        assertEquals(list("HELLO", "WORLD"), new ArrayList<String>(transform));

        Collection<Integer> filter = filter(list(1, 2, 3, 4, 5), predicate(n, n % 2 == 0));
        assertEquals(list(2, 4), new ArrayList<Integer>(filter));
    }

    @Test
    public void convertFn1ToFunction() {
        Function<String, String> fn = toFunction(Lambda.λ(s, s.toUpperCase()));
        assertEquals("HELLO", fn.apply("hello"));
    }

    @Test
    public void convertFunctionnToFn1() {
        Function<Integer, Integer> square = new Function<Integer, Integer>() {
            public Integer apply(Integer from) {
                return from * from;
            }
        };
        assertEquals(9, (int) toFn1(square).call(3));
    }

    @Test
    public void convertFn1ToPredicate() {
        Predicate<Integer> even = toPredicate(Lambda.λ(n, n % 2 == 0));
        assertTrue(even.apply(2));
        assertFalse(even.apply(1));
    }

    @Test
    public void convertPredicateToFn1() {
        Predicate<Integer> even = new Predicate<Integer>() {
            public boolean apply(Integer from) {
                return from % 2 == 0;
            }
        };
        assertTrue(toFn1(even).call(2));
        assertFalse(toFn1(even).call(1));
    }

    @Test
    public void convertFn0ToSupplier() {
        Supplier<Integer> one = toSupplier(Lambda.λ(1));
        assertEquals(1, (int) one.get());
    }

    @Test
    public void convertSupplierToFn0() {
        Supplier<Integer> one = new Supplier<Integer>() {
            public Integer get() {
                return 1;
            }
        };
        assertEquals(1, (int) toFn0(one).call());
    }
}
