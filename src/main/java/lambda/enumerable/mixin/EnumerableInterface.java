package lambda.enumerable.mixin;

import java.util.List;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;

public interface EnumerableInterface<E> {
	public <R> R each(Fn1<E, R> block);

	public <R> R eachWithIndex(Fn2<E, Integer, R> block);

	public <R> List<R> collect(Fn1<E, R> transformer);

	public List<E> findAll(Fn1<E, Boolean> predicate);

	public List<E> select(Fn1<E, Boolean> predicate);

	public List<E> sort(Fn2<E, E, Integer> comparator);

	public <R extends Object & Comparable<? super R>> List<E> sortBy(Fn1<E, R> transformer);

	public List<E> reject(Fn1<E, Boolean> predicate);

	public List<List<E>> partition(Fn1<E, Boolean> predicate);

	public E find(Fn1<E, Boolean> predicate);

	public E detect(Fn1<E, Boolean> predicate);

	public E detect(Fn1<E, Boolean> predicate, Fn0<E> ifNone);

	public Boolean any(Fn1<E, Boolean> predicate);

	public Boolean all(Fn1<E, Boolean> predicate);

	public Integer count(Fn1<E, Boolean> predicate);

	public <R> R inject(R initial, Fn2<R, E, R> into);

	public E inject(Fn2<E, E, E> into);
}
