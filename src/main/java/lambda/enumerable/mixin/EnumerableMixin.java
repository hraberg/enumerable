package lambda.enumerable.mixin;

import java.util.List;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;

public abstract class EnumerableMixin<E> implements Iterable<E>, EnumerableInterface<E> {
	public <R> R each(Fn1<E, R> block) {
		return Enumerable.each(this, block);
	}

	public <R> R eachWithIndex(Fn2<E, Integer, R> block) {
		return Enumerable.eachWithIndex(this, block);
	}

	public <R> List<R> collect(Fn1<E, R> transformer) {
		return Enumerable.collect(this, transformer);
	}

	public List<E> findAll(Fn1<E, Boolean> predicate) {
		return Enumerable.findAll(this, predicate);
	}

	public List<E> select(Fn1<E, Boolean> predicate) {
		return Enumerable.select(this, predicate);
	}

	public List<E> sort(Fn2<E, E, Integer> comparator) {
		return Enumerable.sort(this, comparator);
	}

	public <R extends Object & Comparable<? super R>> List<E> sortBy(Fn1<E, R> transformer) {
		return Enumerable.sortBy(this, transformer);
	}

	public List<E> reject(Fn1<E, Boolean> predicate) {
		return Enumerable.reject(this, predicate);
	}

	public List<List<E>> partition(Fn1<E, Boolean> predicate) {
		return Enumerable.partition(this, predicate);
	}

	public E find(Fn1<E, Boolean> predicate) {
		return Enumerable.find(this, predicate);
	}

	public E detect(Fn1<E, Boolean> predicate) {
		return Enumerable.find(this, predicate);
	}

	public E detect(Fn1<E, Boolean> predicate, Fn0<E> ifNone) {
		return Enumerable.detect(this, predicate, ifNone);
	}

	public Boolean any(Fn1<E, Boolean> predicate) {
		return Enumerable.any(this, predicate);
	}

	public Boolean all(Fn1<E, Boolean> predicate) {
		return Enumerable.all(this, predicate);
	}

	public Integer count(Fn1<E, Boolean> predicate) {
		return Enumerable.count(this, predicate);
	}

	public <R> R inject(R initial, Fn2<R, E, R> into) {
		return Enumerable.inject(this, initial, into);
	}

	public E inject(Fn2<E, E, E> into) {
		return Enumerable.inject(this, into);
	}
}
