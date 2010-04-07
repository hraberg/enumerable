package lambda.enumerable.collection;

import java.util.List;

import lambda.TestBase;

import org.junit.Test;

import static lambda.Lambda.*;
import static org.junit.Assert.*;

public class EnumerableCollectionsTest extends TestBase {
    @Test
    public void canChainEnumerableCollections() throws Exception {
        List<Integer> actual = oneToTen.select(λ(n, n > 5)).collect(λ(n, n * 2));

        assertEquals(list(12, 14, 16, 18, 20), actual);
    }
}
