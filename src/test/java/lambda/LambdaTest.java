package lambda;

import static lambda.Lambda.*;
import static lambda.enumerable.Enumerable.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class LambdaTest {
	@Test
	public void partialApplication() throws Exception {
		Fn2<Integer, Integer, Integer> add = λ(n, m, n + m);
		assertEquals(2, (int) add.call(1, 1));

		Fn1<Integer, Integer> add2 = partial(add, 2);
		assertEquals(4, (int) add2.call(2));

		Fn0<Integer> six = partial(add2, 4);
		assertEquals(6, (int) six.call());
	}
	
	@Test
	public void lambdaCanBeUsedInLambda() throws Exception {
		Fn1<Integer, Integer> timesTwo = λ(n, n * 2);
		assertEquals(6, (int) λ(n, m, timesTwo.call(n) + m).call(2, 2));
	}
}
