package lambda.enumerable;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.LambdaParameter;
import lambda.NewLambda;


public class Enumerable {
	@LambdaParameter
	public static Integer n;
	@LambdaParameter
	public static Integer m;
	@LambdaParameter
	public static Integer i;
	@LambdaParameter
	public static Integer idx;
	@LambdaParameter
	public static String s;
	@LambdaParameter
	public static CharSequence cs;
	@LambdaParameter
	public static String t;
	@LambdaParameter
	public static Double d;
	@LambdaParameter
	public static Character c;
	@LambdaParameter
	public static Collection<?> col;
	@LambdaParameter
	public static Object o;

	@NewLambda
	public static <E, R> Fn1<E, R> λ(E n, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E, R> Fn1<E, R> lambda(E n, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E, R> Fn1<E, R> fn(E n, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E1, E2, R> Fn2<E1, E2, R> λ(E1 n, E2 m, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E1, E2, R> Fn2<E1, E2, R> lambda(E1 n, E2 m, R block) {
		throw new UnsupportedOperationException();
	}

	@NewLambda
	public static <E1, E2, R> Fn2<E1, E2, R> fn(E1 n, E2 m, R block) {
		throw new UnsupportedOperationException();
	}

	public static <E, R> R each(Iterable<E> col, Fn1<E, R> block) {
		R result = null;
		for (E each : col)
			result = block.call(each);
		return result;
	}

	public static <E, R> R eachWithIndex(Iterable<E> col, Fn2<E, Integer, R> block) {
		int i = 0;
		R result = null;
		for (E each : col)
			result = block.call(each, i++);
		return result;
	}

	public static <E, R> List<R> collect(Iterable<E> col, Fn1<E, R> transformer) {
		List<R> result = new ArrayList<R>();
		for (E each : col)
			result.add(transformer.call(each));
		return result;
	}

	public static <E> List<E> findAll(Iterable<E> col, Fn1<E, Boolean> predicate) {
		return select(col, predicate);
	}

	public static <E> List<E> select(Iterable<E> col, Fn1<E, Boolean> predicate) {
		List<E> result = new ArrayList<E>();
		for (E each : col)
			if (predicate.call(each))
				result.add(each);
		return result;
	}

	public static <E> List<E> reject(Iterable<E> col, Fn1<E, Boolean> predicate) {
		List<E> result = new ArrayList<E>();
		for (E each : col)
			if (!predicate.call(each))
				result.add(each);
		return result;
	}

	public static <E> List<E> sort(Iterable<E> col, final Fn2<E, E, Integer> comparator) {
		List<E> result = new ArrayList<E>();
		for (E each : col)
			result.add(each);
		Collections.sort(result, new Comparator<E>() {
			public int compare(E o1, E o2) {
				return comparator.call(o1, o2);
			}
		});
		return result;
	}

	public static <E, R extends Object & Comparable<? super R>> List<E> sortBy(Iterable<E> col, final Fn1<E, R> transformer) {
		List<E> result = new ArrayList<E>();
		for (E each : col)
			result.add(each);
		Collections.sort(result, new Comparator<E>() {
			public int compare(E o1, E o2) {
				return transformer.call(o1).compareTo(transformer.call(o2));
			}
		});
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <E> List<List<E>> partition(Iterable<E> col, Fn1<E, Boolean> predicate) {
		List<E> result1 = new ArrayList<E>();
		List<E> result2 = new ArrayList<E>();
		for (E each : col)
			if (predicate.call(each))
				result1.add(each);
			else
				result2.add(each);
		return new ArrayList<List<E>>(asList(result1, result2));
	}

	public static <E> E find(Iterable<E> col, Fn1<E, Boolean> predicate) {
		return detect(col, predicate);
	}

	public static <E> E detect(Iterable<E> col, Fn1<E, Boolean> predicate) {
		return detect(col, predicate, ifNone((E) null));
	}

	public static <E> E detect(Iterable<E> col, Fn1<E, Boolean> predicate, Fn0<E> ifNone) {
		for (E each : col)
			if (predicate.call(each))
				return each;
		return ifNone.call();
	}

	public static <E> Boolean any(Iterable<E> col, Fn1<E, Boolean> predicate) {
		for (E each : col)
			if (predicate.call(each))
				return true;
		return false;
	}

	public static <E> Boolean all(Iterable<E> col, Fn1<E, Boolean> predicate) {
		for (E each : col)
			if (!predicate.call(each))
				return false;
		return true;
	}

	public static <E> Integer count(Iterable<E> col, Fn1<E, Boolean> predicate) {
		int result = 0;
		for (E each : col)
			if (predicate.call(each))
				result++;
		return result;
	}

	public static <E, R> R inject(Iterable<E> col, R initial, Fn2<R, E, R> into) {
		for (E each : col)
			initial = into.call(initial, each);
		return initial;
	}

	public static <E> E inject(Iterable<E> col, Fn2<E, E, E> into) {
		Iterator<E> i = col.iterator();
		E initial = i.next();
		while (i.hasNext())
			initial = into.call(initial, i.next());
		return initial;
	}

	public static <R> IfNone<R> ifNone(R defaultValue) {
		return new IfNone<R>(defaultValue);
	}

	public static <E, R> Into<R, E> into(Fn2<R, E, R> block) {
		return new Into<R, E>(block);
	}

	static class IfNone<R> implements Fn0<R> {
		R defaultValue;

		IfNone(R defaultValue) {
			this.defaultValue = defaultValue;
		}

		public R call() {
			return defaultValue;
		}
	}

	static class Into<R, E> implements Fn2<R, E, R> {
		Fn2<R, E, R> block;

		Into(Fn2<R, E, R> block) {
			this.block = block;
		}

		public R call(R arg1, E arg2) {
			return block.call(arg1, arg2);
		}
	}

	public static <E, R> List<R> map(Iterable<E> col, Fn1<E, R> transformer) {
		return collect(col, transformer);
	}

	public static <E> List<E> filter(Iterable<E> col, Fn1<E, Boolean> predicate) {
		return select(col, predicate);
	}

	public static <E, R> R reduce(Iterable<E> col, R initial, Fn2<R, E, R> block) {
		return inject(col, initial, into(block));
	}

	public static <E> E reduce(Iterable<E> col, Fn2<E, E, E> block) {
		return inject(col, into(block));
	}
}
