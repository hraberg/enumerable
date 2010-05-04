package lambda.support.googlecollect;

import lambda.Fn0;
import lambda.Fn1;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * This is class is similar {@link lambda.Lambda}, but instead of creating
 * lambdas inheriting from {@link lambda.Fn0} it creates lambdas implementing
 * the {@link Function}, {@link Predicate} or {@link Supplier} interfaces to be
 * used with {@link Collections2}, {@link Iterables}, {@link Iterators},
 * {@link Lists} {@link Maps} or {@link Sets}.
 */
public class LambdaGoogleCollections {
    @NewLambda
    public static <F, T> Function<F, T> function(F from, T to) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <T> Predicate<T> predicate(T input, boolean to) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <T> Supplier<T> supplier(T value) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link Predicate} in a {@link Fn1}.
     */
    @SuppressWarnings("serial")
    public static <T> Fn1<T, Boolean> toFn1(final Predicate<T> predicate) {
        return new Fn1<T, Boolean>() {
            public Boolean call(T input) {
                return predicate.apply(input);
            }
        };
    }

    /**
     * Wraps the {@link Function} in a {@link Fn1}.
     */
    @SuppressWarnings("serial")
    public static <F, T> Fn1<F, T> toFn1(final Function<F, T> function) {
        return new Fn1<F, T>() {
            public T call(F input) {
                return function.apply(input);
            }
        };
    }

    /**
     * Wraps the {@link Supplier} in a {@link Fn0}.
     */
    @SuppressWarnings("serial")
    public static <T> Fn0<T> toFn0(final Supplier<T> supplier) {
        return new Fn0<T>() {
            public T call() {
                return supplier.get();
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link Predicate}.
     */
    public static <T> Predicate<T> toPredicate(final Fn1<T, Boolean> precicate) {
        return new Predicate<T>() {
            public boolean apply(T input) {
                return precicate.call(input);
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link Function}.
     */
    public static <F, T> Function<F, T> toFunction(final Fn1<F, T> function) {
        return new Function<F, T>() {
            public T apply(F input) {
                return function.call(input);
            }
        };
    }

    /**
     * Wraps the {@link Fn0} in a {@link Supplier}.
     */
    public static <T> Supplier<T> toSupplier(final Fn0<T> supplier) {
        return new Supplier<T>() {
            public T get() {
                return supplier.call();
            }
        };
    }
}
