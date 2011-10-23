package lambda.functionaljava;

import static fj.Ord.*;
import static fj.data.TreeMap.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
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

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        TreeMap<String, Integer> map = empty(stringOrd());
        map = map.set("foo", 2);
        map = map.update("foo", λ(i, i + 3))._2();
        System.out.println(map.get("foo").some()); // 5
    }

    public static Ord<String> stringOrd() {
        return ord(λ(a1, λ(a2, a1.compareTo(a2) < 0 ? Ordering.LT : a1.compareTo(a2) == 0 ? Ordering.EQ : Ordering.GT)));
    }
}
