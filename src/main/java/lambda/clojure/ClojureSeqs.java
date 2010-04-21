package lambda.clojure;

import static clojure.lang.RT.*;
import static java.util.Arrays.*;

import java.util.Comparator;
import java.util.List;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Range;
import clojure.lang.Seqable;
import clojure.lang.Var;

/**
 * Facade for Clojure's Sequence library.
 * <p>
 * <i>This file was originally generated, but has been edited by hand.</i>
 */
public class ClojureSeqs {
    static Var second = var("clojure.core", "second");
    static Var every = var("clojure.core", "every?");
    static Var notEvery = var("clojure.core", "not-every?");
    static Var some = var("clojure.core", "some");
    static Var notAny = var("clojure.core", "not-any?");
    static Var concat = var("clojure.core", "concat");
    static Var map = var("clojure.core", "map");
    static Var mapcat = var("clojure.core", "mapcat");
    static Var filter = var("clojure.core", "filter");
    static Var reduce = var("clojure.core", "reduce");
    static Var take = var("clojure.core", "take");
    static Var takeNth = var("clojure.core", "take-nth");
    static Var takeWhile = var("clojure.core", "take-while");
    static Var takeLast = var("clojure.core", "take-last");
    static Var drop = var("clojure.core", "drop");
    static Var dropWhile = var("clojure.core", "drop-while");
    static Var dropLast = var("clojure.core", "drop-last");
    static Var reverse = var("clojure.core", "reverse");
    static Var cycle = var("clojure.core", "cycle");
    static Var interleave = var("clojure.core", "interleave");
    static Var splitAt = var("clojure.core", "split-at");
    static Var splitWith = var("clojure.core", "split-with");
    static Var repeat = var("clojure.core", "repeat");
    static Var replicate = var("clojure.core", "replicate");
    static Var iterate = var("clojure.core", "iterate");
    static Var range = var("clojure.core", "range");
    static Var into = var("clojure.core", "into");
    static Var distinct = var("clojure.core", "distinct");
    static Var set = var("clojure.core", "set");
    static Var dorun = var("clojure.core", "dorun");
    static Var doall = var("clojure.core", "doall");
    static Var sort = var("clojure.core", "sort");
    static Var sortBy = var("clojure.core", "sort-by");
    static Var zipmap = var("clojure.core", "zipmap");
    static Var lazyCat = var("clojure.core", "lazy-cat");

    /**
     * [x]
     */
    public static Object second(Seqable x) throws Exception {
        return second.invoke(x);
    }

    /**
     * [pred coll]
     */
    public static boolean every(IFn pred, Seqable coll) throws Exception {
        return (Boolean) every.invoke(pred, coll);
    }

    /**
     * [pred coll]
     */
    public static boolean notEvery(IFn pred, Seqable coll) throws Exception {
        return (Boolean) notEvery.invoke(pred, coll);
    }

    /**
     * [pred coll]
     */
    public static boolean some(IFn pred, Seqable coll) throws Exception {
        return (Boolean) some.invoke(pred, coll);
    }

    /**
     * [pred coll]
     */
    public static boolean notAny(IFn pred, Seqable coll) throws Exception {
        return (Boolean) notAny.invoke(pred, coll);
    }

    /**
     * []
     */
    public static ISeq concat() throws Exception {
        return (ISeq) concat.invoke();
    }

    /**
     * [x]
     */
    public static ISeq concat(Seqable x) throws Exception {
        return (ISeq) concat.invoke(x);
    }

    /**
     * [x y]
     */
    public static ISeq concat(Seqable x, Seqable y) throws Exception {
        return (ISeq) concat.invoke(x, y);
    }

    /**
     * [x y & zs]
     */
    public static ISeq concat(Seqable x, Seqable y, Seqable... zs) throws Exception {
        List<Seqable> args = new java.util.ArrayList<Seqable>();
        args.addAll(asList(x, y));
        args.addAll(asList(zs));
        return (ISeq) concat.applyTo(seq(args));
    }

    /**
     * [f coll]
     */
    public static ISeq map(IFn f, Seqable coll) throws Exception {
        return (ISeq) map.invoke(f, coll);
    }

    /**
     * [f c1 c2]
     */
    public static ISeq map(IFn f, Seqable c1, Seqable c2) throws Exception {
        return (ISeq) map.invoke(f, c1, c2);
    }

    /**
     * [f c1 c2 c3]
     */
    public static ISeq map(IFn f, Seqable c1, Seqable c2, Seqable c3) throws Exception {
        return (ISeq) map.invoke(f, c1, c2, c3);
    }

    /**
     * [f & colls]
     */
    public static ISeq mapcat(IFn f, Seqable... colls) throws Exception {
        List<Object> args = new java.util.ArrayList<Object>();
        args.add(f);
        args.addAll(asList(colls));
        return (ISeq) mapcat.applyTo(seq(args));
    }

    /**
     * [pred coll]
     */
    public static ISeq filter(IFn pred, Seqable coll) throws Exception {
        return (ISeq) filter.invoke(pred, coll);
    }

    /**
     * [f coll]
     */
    public static Object reduce(IFn f, Seqable coll) throws Exception {
        return reduce.invoke(f, coll);
    }

    /**
     * [f start coll]
     */
    public static Object reduce(IFn f, Object start, Seqable coll) throws Exception {
        return reduce.invoke(f, start, coll);
    }

    /**
     * [n coll]
     */
    public static ISeq take(Number n, Seqable coll) throws Exception {
        return (ISeq) take.invoke(n, coll);
    }

    /**
     * [n coll]
     */
    public static ISeq takeNth(Number n, Seqable coll) throws Exception {
        return (ISeq) takeNth.invoke(n, coll);
    }

    /**
     * [pred coll]
     */
    public static ISeq takeWhile(IFn pred, Seqable coll) throws Exception {
        return (ISeq) takeWhile.invoke(pred, coll);
    }

    /**
     * [n coll]
     */
    public static ISeq takeLast(Number n, Seqable coll) throws Exception {
        return (ISeq) takeLast.invoke(n, coll);
    }

    /**
     * [n coll]
     */
    public static ISeq drop(Number n, Seqable coll) throws Exception {
        return (ISeq) drop.invoke(n, coll);
    }

    /**
     * [s]
     */
    public static ISeq dropLast(Seqable s) throws Exception {
        return (ISeq) dropLast.invoke(s);
    }

    /**
     * [n s]
     */
    public static ISeq dropLast(Number n, Seqable s) throws Exception {
        return (ISeq) dropLast.invoke(n, s);
    }

    /**
     * [pred coll]
     */
    public static ISeq dropWhile(IFn pred, Seqable coll) throws Exception {
        return (ISeq) dropWhile.invoke(pred, coll);
    }

    /**
     * [coll]
     */
    public static ISeq reverse(Seqable coll) throws Exception {
        return (ISeq) reverse.invoke(coll);
    }

    /**
     * [coll]
     */
    public static ISeq cycle(Seqable coll) throws Exception {
        return (ISeq) cycle.invoke(coll);
    }

    /**
     * [c1 c2]
     */
    public static ISeq interleave(Seqable c1, Seqable c2) throws Exception {
        return (ISeq) interleave.invoke(c1, c2);
    }

    /**
     * [c1 c2 & colls]
     */
    public static ISeq interleave(Seqable c1, Seqable c2, Seqable... colls) throws Exception {
        List<Seqable> args = new java.util.ArrayList<Seqable>();
        args.addAll(asList(c1, c2));
        args.addAll(asList(colls));
        return (ISeq) interleave.applyTo(seq(args));
    }

    /**
     * [n coll]
     */
    public static IPersistentVector splitAt(Number n, Seqable coll) throws Exception {
        return (IPersistentVector) splitAt.invoke(n, coll);
    }

    /**
     * [pred coll]
     */
    public static IPersistentVector split_with(IFn pred, Seqable coll) throws Exception {
        return (IPersistentVector) splitWith.invoke(pred, coll);
    }

    /**
     * [x]
     */
    public static ISeq repeat(Object x) throws Exception {
        return (ISeq) repeat.invoke(x);
    }

    /**
     * [n x]
     */
    public static ISeq repeat(Number n, Object x) throws Exception {
        return (ISeq) repeat.invoke(n, x);
    }

    /**
     * [n x]
     */
    public static ISeq replicate(Number n, Object x) throws Exception {
        return (ISeq) replicate.invoke(n, x);
    }

    /**
     * [f x]
     */
    public static ISeq iterate(IFn f, Object x) throws Exception {
        return (ISeq) iterate.invoke(f, x);
    }

    /**
     * [end]
     */
    public static Range range(Number end) throws Exception {
        return (Range) range.invoke(end);
    }

    /**
     * [start end]
     */
    public static Range range(Number start, Number end) throws Exception {
        return (Range) range.invoke(start, end);
    }

    /**
     * [start end step]
     */
    public static Range range(Number start, Number end, Number step) throws Exception {
        return (Range) range.invoke(start, end, step);
    }

    /**
     * [to from]
     */
    public static ISeq into(Seqable to, Seqable from) throws Exception {
        return (ISeq) into.invoke(to, from);
    }

    /**
     * [coll]
     */
    public static ISeq distinct(Seqable coll) throws Exception {
        return (ISeq) distinct.invoke(coll);
    }

    /**
     * [coll]
     */
    public static IPersistentSet set(Seqable coll) throws Exception {
        return (IPersistentSet) set.invoke(coll);
    }

    /**
     * [coll]
     */
    public static Object dorun(Seqable coll) throws Exception {
        return dorun.invoke(coll);
    }

    /**
     * [n coll]
     */
    public static Object dorun(Number n, Seqable coll) throws Exception {
        return dorun.invoke(n, coll);
    }

    /**
     * [coll]
     */
    public static Object doall(Seqable coll) throws Exception {
        return doall.invoke(coll);
    }

    /**
     * [n coll]
     */
    public static Object doall(Number n, Seqable coll) throws Exception {
        return doall.invoke(n, coll);
    }

    /**
     * [coll]
     */
    public static ISeq sort(Object coll) throws Exception {
        return (ISeq) sort.invoke(coll);
    }

    /**
     * [comp coll]
     */
    public static ISeq sort(Comparator<?> comp, Seqable coll) throws Exception {
        return (ISeq) sort.invoke(comp, coll);
    }

    /**
     * [keyfn coll]
     */
    public static ISeq sortBy(IFn keyfn, Seqable coll) throws Exception {
        return (ISeq) sortBy.invoke(keyfn, coll);
    }

    /**
     * [keyfn comp coll]
     */
    public static ISeq sortBy(IFn keyfn, Comparator<?> comp, Seqable coll) throws Exception {
        return (ISeq) sortBy.invoke(keyfn, comp, coll);
    }

    /**
     * [keys vals]
     */
    public static IPersistentMap zipmap(Seqable keys, Seqable vals) throws Exception {
        return (IPersistentMap) zipmap.invoke(keys, vals);
    }

    /**
     * [& colls]
     */
    public static ISeq lazyCat(Seqable... colls) throws Exception {
        return (ISeq) lazyCat.applyTo(seq(colls));
    }
}
