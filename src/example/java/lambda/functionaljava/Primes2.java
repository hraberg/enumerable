package lambda.functionaljava;

import static fj.Ord.*;
import static fj.P.*;
import static fj.Show.*;
import static fj.data.Enumerator.*;
import static fj.data.Natural.*;
import static fj.data.Stream.*;
import static lambda.support.functionaljava.LambdaFunctionalJava.*;

import java.math.BigInteger;

import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;
import fj.Show;
import fj.data.Natural;
import fj.data.Stream;

/**
 * Prints all primes less than n
 */
public class Primes2 {
    @LambdaParameter
    static Natural nn;

    @LambdaParameter
    static Stream<?> as;

    // Finds primes in a given stream.
    public static Stream<Natural> sieve(final Stream<Natural> xs) {
        return cons(xs.head(),
                p1(sieve(xs.tail()._1().removeAll(naturalOrd.equal().eq(ZERO).o(mod.f(xs.head()))))));
    }

    // A stream of all primes less than n.
    public static Stream<Natural> primes(final Natural n) {
        return sieve(forever(naturalEnumerator, natural(2).some())).takeWhile(naturalOrd.isLessThan(n));
    }

    public static void main(final String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);

        final Natural n = natural(new BigInteger(args.length == 0 ? "100" : args[0])).some();
        final Show<Stream<Natural>> s = streamShow(naturalShow());

        s.println(primes(n));
    }

    public static Show<Natural> naturalShow() {
        return bigintShow().comap(λ(nn, nn.bigIntegerValue()));
    }

    @SuppressWarnings("unchecked")
    public static <A> Show<Stream<A>> streamShow(final Show<A> sa) {
        return show(λ((Stream<A>) as, join(((Stream<A>) as).map(sa.show_()).intersperse(fromString(",")).cons(fromString("<")).snoc(p(fromString(">"))))));
    }

    public static final Show<BigInteger> bigintShow() {
        return Array_bind.anyShow();
    }
}
