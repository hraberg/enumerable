package lambda.enumerable.collection;

import static java.lang.System.*;
import static lambda.Lambda.*;
import static lambda.Parameters.*;
import static lambda.enumerable.EnumerableArrays.*;
import static lambda.primitives.LambdaPrimitives.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lambda.Fn1;
import lambda.TestBase;

import org.junit.Test;

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
    public void eachReturnsSameEnumerable() throws Exception {
        EList<Object> list = new EList<Object>();
        assertSame(list, list.each(λ(obj, obj)));
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
    public void mapIsEnumerableAsEntrySet() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("hello", "world");

        EMap<String, String> eMap = new EMap<String, String>(map);
        Entry<String, String> first = eMap.first();
        assertEquals("hello", first.getKey());
        assertEquals("world", first.getValue());

        map.remove("hello");
        assertNull(eMap.first());

        eMap.put("hello", "world");
        assertNotNull(eMap.first());
    }

    @Test
    public void wrappingIterablesUsingEIterableFrom() throws Exception {
        assertEquals(ESet.class, EnumerableModule.extend(new HashSet<Object>()).getClass());
        assertEquals(ECollection.class, EnumerableModule.extend(new HashMap<Object, Object>().values()).getClass());

        EList<Object> list = EnumerableModule.extend(new ArrayList<Object>());
        assertEquals(EList.class, list.getClass());

        AnIterable anIterable = new AnIterable();
        EIterable<Object> eIterable = EnumerableModule.extend(anIterable);
        assertSame(eIterable, EnumerableModule.extend(eIterable));
        assertSame(anIterable, eIterable.delegate());
    }

    @Test
    public void enumerableCollectionForwardsAllCallsToBackingCollection() throws Exception {
        ArrayList<Object> original = new ArrayList<Object>();
        ECollection<Object> collection = EnumerableModule.extend(original);
        assertTrue(original.isEmpty());

        collection.add("hello");
        assertEquals(1, original.size());

        original.add("hello");
        assertEquals(2, collection.size());
    }

    @Test
    public void sortByExpensiveBlock() throws Exception {
        File windows = new File("C:\\Windows");
        if (!windows.isDirectory())
            return;

        EList<String> files = toList(windows.list());
        Fn1<String, Long> lastModifiedBlock = λ(s, new File(s).lastModified());

        List<String> actual = null;
        int times = 5;
        long now;

        long timeSchwartzianTransform = 0;
        long timeWithCache = 0;
        long timeNoCache = 0;
        for (int i = 0; i < times; i++) {
            now = nanoTime();
            actual = files.sortBy(lastModifiedBlock);
            timeSchwartzianTransform += nanoTime() - now;

            now = nanoTime();
            actual = files.sort(new EnumerableModule.CachedBlockResultComparator<String, Long>(lastModifiedBlock));
            timeWithCache += nanoTime() - now;

            now = nanoTime();
            actual = files.sort(new EnumerableModule.BlockResultComparator<String, Long>(lastModifiedBlock));
            timeNoCache += nanoTime() - now;
        }

        // out.println("schwartzian: " + timeSchwartzianTransform);
        // out.println("map: " + timeWithCache);
        // out.println("no cache: " + timeNoCache);

        assertTrue("Schwarzian transform was slower than no cache: " + timeSchwartzianTransform + " > "
                + timeNoCache, timeSchwartzianTransform <= timeNoCache);

        long lastModified = -1;
        for (String f : actual) {
            long fileLastModified = new File(f).lastModified();
            assertTrue(fileLastModified >= lastModified);
            lastModified = fileLastModified;
        }
    }

    class AnIterable implements Iterable<Object> {
        public Iterator<Object> iterator() {
            return null;
        }
    }
}
