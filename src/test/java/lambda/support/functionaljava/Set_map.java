package lambda.support.functionaljava;

import static fj.Ord.*;
import static fj.Show.*;
import static fj.data.Set.*;
import static lambda.support.functionaljava.Array_bind.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;
import static org.junit.Assert.*;
import lambda.annotation.LambdaParameter;

import org.junit.Test;

import fj.Ord;
import fj.Ordering;
import fj.data.Set;

public final class Set_map {
    @LambdaParameter
    static Integer i;

    @LambdaParameter
    static Integer a1, a2;

    @Test
    public void test() {
        final Set<Integer> a = empty(intOrd()).insert(1).insert(2).insert(3).insert(4).insert(5).insert(6);
        final Set<Integer> b = a.map(intOrd(), λ(i, i / 2));
        assertEquals("<3,2,1,0>", listShow(intShow()).showS(b.toList())); // [3,2,1,0]
    }
    
    public static Ord<Integer> intOrd() {
        return ord(λ(a1, λ(a2, a1.compareTo(a2) < 0 ? Ordering.LT : a1.compareTo(a2) == 0 ? Ordering.EQ : Ordering.GT)));
    }    
}
