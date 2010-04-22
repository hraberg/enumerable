package lambda.clojure;

import static clojure.lang.RT.*;
import static lambda.clojure.ClojureSeqs.Vars.*;
import static lambda.exception.UncheckedException.*;

import java.util.Comparator;

import clojure.lang.ArraySeq;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Namespace;
import clojure.lang.Range;
import clojure.lang.Seqable;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 * Facade for the <a href="http://clojure.org/sequences">Clojure Seq
 * library</a>, used together with {@link clojure.lang.RT} and
 * {@link LambdaClojure}, which provides Enumerable.java lambdas implementing
 * {@link IFn}.
 * <p>
 * <i>This file was originally generated, but has been edited by hand.</i>
 */
public class ClojureSeqs {
    static {
        init();
    }

    static void init() {
        try {
            if (CURRENT_NS.deref() == CLOJURE_NS)
                CURRENT_NS.doReset(Namespace.findOrCreate(Symbol.create("user")));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static class Vars {

        public static Var every = var("clojure.core", "every?");
        public static Var notEvery = var("clojure.core", "not-every?");
        public static Var some = var("clojure.core", "some");
        public static Var notAny = var("clojure.core", "not-any?");
        public static Var concat = var("clojure.core", "concat");
        public static Var map = var("clojure.core", "map");
        public static Var mapcat = var("clojure.core", "mapcat");
        public static Var filter = var("clojure.core", "filter");
        public static Var reduce = var("clojure.core", "reduce");
        public static Var take = var("clojure.core", "take");
        public static Var takeNth = var("clojure.core", "take-nth");
        public static Var takeWhile = var("clojure.core", "take-while");
        public static Var takeLast = var("clojure.core", "take-last");
        public static Var drop = var("clojure.core", "drop");
        public static Var dropWhile = var("clojure.core", "drop-while");
        public static Var dropLast = var("clojure.core", "drop-last");
        public static Var reverse = var("clojure.core", "reverse");
        public static Var cycle = var("clojure.core", "cycle");
        public static Var interleave = var("clojure.core", "interleave");
        public static Var splitAt = var("clojure.core", "split-at");
        public static Var splitWith = var("clojure.core", "split-with");
        public static Var repeat = var("clojure.core", "repeat");
        public static Var replicate = var("clojure.core", "replicate");
        public static Var iterate = var("clojure.core", "iterate");
        public static Var range = var("clojure.core", "range");
        public static Var into = var("clojure.core", "into");
        public static Var distinct = var("clojure.core", "distinct");
        public static Var set = var("clojure.core", "set");
        public static Var vec = var("clojure.core", "vec");
        public static Var dorun = var("clojure.core", "dorun");
        public static Var doall = var("clojure.core", "doall");
        public static Var sort = var("clojure.core", "sort");
        public static Var sortBy = var("clojure.core", "sort-by");
        public static Var zipmap = var("clojure.core", "zipmap");
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
        return (ISeq) concat.applyTo(ArraySeq.create((Object[]) zs).cons(y).cons(x));
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
        return (ISeq) mapcat.applyTo(ArraySeq.create((Object[]) colls).cons(f));
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
    @SuppressWarnings("unchecked")
    public static <R> R reduce(IFn f, Seqable coll) throws Exception {
        return (R) reduce.invoke(f, coll);
    }

    /**
     * [f start coll]
     */
    @SuppressWarnings("unchecked")
    public static <R> R reduce(IFn f, Object start, Seqable coll) throws Exception {
        return (R) reduce.invoke(f, start, coll);
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
        return (ISeq) interleave.applyTo(ArraySeq.create((Object[]) colls).cons(c2).cons(c1));
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
    public static IPersistentVector splitWith(IFn pred, Seqable coll) throws Exception {
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
    public static IPersistentVector vec(Seqable coll) throws Exception {
        return (IPersistentVector) vec.invoke(coll);
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
    public static ISeq doall(Seqable coll) throws Exception {
        return (ISeq) doall.invoke(coll);
    }

    /**
     * [n coll]
     */
    public static ISeq doall(Number n, Seqable coll) throws Exception {
        return (ISeq) doall.invoke(n, coll);
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
}
