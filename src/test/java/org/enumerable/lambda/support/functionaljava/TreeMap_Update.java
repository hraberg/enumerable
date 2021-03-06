package org.enumerable.lambda.support.functionaljava;

import static fj.Ord.*;
import static fj.data.TreeMap.*;
import static org.enumerable.lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;

import org.enumerable.lambda.annotation.LambdaParameter;
import org.junit.Test;

import fj.Ord;
import fj.Ordering;
import fj.data.TreeMap;

/**
 * Queries and updates an entry in a TreeMap in one go.
 */
public class TreeMap_Update {
    @LambdaParameter
    static Integer i;

    @LambdaParameter
    static String a1, a2;

    @Test
    public void test() {
        TreeMap<String, Integer> map = empty(stringOrd());
        map = map.set("foo", 2);
        map = map.update("foo", λ(i, i + 3))._2();
        assertEquals(5, (int) (map.get("foo").some())); // 5
    }

    public static Ord<String> stringOrd() {
        return ord(λ(a1, λ(a2, a1.compareTo(a2) < 0 ? Ordering.LT : a1.compareTo(a2) == 0 ? Ordering.EQ : Ordering.GT)));
    }
}
