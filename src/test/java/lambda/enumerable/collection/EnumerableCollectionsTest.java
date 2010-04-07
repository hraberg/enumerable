package lambda.enumerable.collection;

import java.util.List;

import lambda.TestBase;

import org.junit.Test;

import static lambda.Lambda.*;
import static lambda.Lambda.Primitives.*;
import static lambda.enumerable.array.EnumerableArrays.*;
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

    @Test
    public void toListReturnsANewCopy() throws Exception {
        EList<Integer> list = oneToFive.toList();
        assertEquals(oneToFive, list);
        assertNotSame(oneToFive, list);
    }
}
