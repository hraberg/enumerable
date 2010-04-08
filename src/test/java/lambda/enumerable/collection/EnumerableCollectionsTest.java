package lambda.enumerable.collection;

import java.util.*;

import lambda.TestBase;

import org.junit.Test;

import static lambda.enumerable.EnumerableArrays.*;

import static lambda.Lambda.*;
import static lambda.primitives.LambdaPrimitives.*;
import static org.junit.Assert.*;

public class EnumerableCollectionsTest extends TestBase {
    @Test
    public void canChainEnumerableCollections() throws Exception {
        List<Integer> actual = oneToTen.select(λ(n, n > 5)).collect(λ(n, n * 2));

        assertEquals(list(12, 14, 16, 18, 20), actual);
    }

    @Test
    public void canChainEnumerableCollectionsThatSwitchType() throws Exception {
        List<String> actual = oneToTen.select(λ(n, n > 5)).collect(λ(n, String.format("%03d", n)));

        assertEquals(list("006", "007", "008", "009", "010"), actual);
    }

    @Test
    public void sortWorksIfContainingElementsAreComparable() throws Exception {
        assertEquals(oneToFive, toList(4, 5, 3, 1, 2).sort());
    }

    @Test(expected = ClassCastException.class)
    public void sortThrowsClassCastExceptionIfContainingElementsAreNotComparable() throws Exception {
        toList(new Object(), new Object()).sort();
    }

    @Test
    public void toListReturnsANewCopy() throws Exception {
        EList<Integer> list = oneToFive.toList();
        assertEquals(oneToFive, list);
        assertNotSame(oneToFive, list);
    }

    @Test
    public void wrappingIterablesUsingEIterableFrom() throws Exception {
        assertEquals(ESet.class, EIterable.from(new HashSet<Object>()).getClass());
        assertEquals(ECollection.class, EIterable.from(new HashMap<Object, Object>().values()).getClass());

        EList<Object> list = EIterable.from(new ArrayList<Object>());
        assertEquals(EList.class, list.getClass());

        AnIterable anIterable = new AnIterable();
        EIterable<Object> eIterable = EIterable.from(anIterable);
        assertSame(eIterable, EIterable.from(eIterable));
        assertSame(anIterable, eIterable.delegate());
    }

    @Test
    public void enumerableCollectionForwardsAllCallsToBackingCollection() throws Exception {
        ArrayList<Object> original = new ArrayList<Object>();
        ECollection<Object> collection = EIterable.from(original);
        assertTrue(original.isEmpty());
        
        collection.add("hello");
        assertEquals(1, original.size());

        original.add("hello");
        assertEquals(2, collection.size());
    }

    class AnIterable implements Iterable<Object> {
        public Iterator<Object> iterator() {
            return null;
        }
    }
}
