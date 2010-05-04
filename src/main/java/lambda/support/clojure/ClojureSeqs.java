package lambda.support.clojure;

import static clojure.lang.RT.*;
import static lambda.exception.UncheckedException.*;
import static lambda.support.clojure.ClojureSeqs.Vars.*;

import java.util.Comparator;

import clojure.lang.ArraySeq;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Range;
import clojure.lang.Seqable;
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
    /*
     * Methods (or macros) neither here or having a version in RT. Many are in
     * the ISeq subclasses.
     * 
     * Seq-in Seq-out: remove rest fnext nnext butlast replace seque
     * 
     * Using a Seq: ffirst nfirst when-first last into-array into-array2d apply
     * not-empty seq? empty doseq
     * 
     * Creating a Seq: rseq subsec rsubsec lazy-seq line-seq resultset-seq
     * re-seq tree-seq file-seq xml-seq irerator-seq enumeration-seq
     */

    static {
        LambdaClojure.init();
    }

    /**
     * This class refers the main vars of the Clojure Seq library. They can be
     * used directly without the facade in the enclosing class if that's
     * preferred. They can be used on their own, or with
     * {@link LambdaClojure#eval(Object...)}.
     */
    public static class Vars {
        public static Var every = var("clojure.core", "every?");
        public static Var notEvery = var("clojure.core", "not-every?");
        public static Var some = var("clojure.core", "some");
        public static Var notAny = var("clojure.core", "not-any?");
        public static Var concat = var("clojure.core", "concat");
        public static Var map = var("clojure.core", "map");
        public static Var mapcat = var("clojure.core", "mapcat");
        public static Var pmap = var("clojure.core", "pmap");
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
        public static Var interpose = var("clojure.core", "interpose");
        public static Var partition = var("clojure.core", "partition");
        public static Var splitAt = var("clojure.core", "split-at");
        public static Var splitWith = var("clojure.core", "split-with");
        public static Var repeat = var("clojure.core", "repeat");
        public static Var repeatedly = var("clojure.core", "repeatedly");
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
    public static boolean every(IFn pred, Seqable coll) {
        try {
            return (Boolean) every.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static boolean notEvery(IFn pred, Seqable coll) {
        try {
            return (Boolean) notEvery.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static boolean some(IFn pred, Seqable coll) {
        try {
            return (Boolean) some.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static boolean notAny(IFn pred, Seqable coll) {
        try {
            return (Boolean) notAny.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * []
     */
    public static ISeq concat() {
        try {
            return (ISeq) concat.invoke();
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [x]
     */
    public static ISeq concat(Seqable x) {
        try {
            return (ISeq) concat.invoke(x);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [x y]
     */
    public static ISeq concat(Seqable x, Seqable y) {
        try {
            return (ISeq) concat.invoke(x, y);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [x y & zs]
     */
    public static ISeq concat(Seqable x, Seqable y, Seqable... zs) {
        try {
            return (ISeq) concat.applyTo(ArraySeq.create((Object[]) zs).cons(y).cons(x));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f coll]
     */
    public static ISeq map(IFn f, Seqable coll) {
        try {
            return (ISeq) map.invoke(f, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f c1 c2]
     */
    public static ISeq map(IFn f, Seqable c1, Seqable c2) {
        try {
            return (ISeq) map.invoke(f, c1, c2);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f c1 c2 c3]
     */
    public static ISeq map(IFn f, Seqable c1, Seqable c2, Seqable c3) {
        try {
            return (ISeq) map.invoke(f, c1, c2, c3);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f & colls]
     */
    public static ISeq mapcat(IFn f, Seqable... colls) {
        try {
            return (ISeq) mapcat.applyTo(ArraySeq.create((Object[]) colls).cons(f));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f coll]
     */
    public static ISeq pmap(IFn f, Seqable coll) {
        try {
            return (ISeq) pmap.invoke(f, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f coll & colls]
     */
    public static ISeq pmap(IFn f, Seqable coll, Seqable... colls) {
        try {
            return (ISeq) pmap.applyTo(ArraySeq.create((Object[]) colls).cons(coll).cons(f));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static ISeq filter(IFn pred, Seqable coll) {
        try {
            return (ISeq) filter.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f coll]
     */
    @SuppressWarnings("unchecked")
    public static <R> R reduce(IFn f, Seqable coll) {
        try {
            return (R) reduce.invoke(f, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f start coll]
     */
    @SuppressWarnings("unchecked")
    public static <R> R reduce(IFn f, R start, Seqable coll) {
        try {
            return (R) reduce.invoke(f, start, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static ISeq take(Number n, Seqable coll) {
        try {
            return (ISeq) take.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static ISeq takeNth(Number n, Seqable coll) {
        try {
            return (ISeq) takeNth.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static ISeq takeWhile(IFn pred, Seqable coll) {
        try {
            return (ISeq) takeWhile.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static ISeq takeLast(Number n, Seqable coll) {
        try {
            return (ISeq) takeLast.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static ISeq drop(Number n, Seqable coll) {
        try {
            return (ISeq) drop.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [s]
     */
    public static ISeq dropLast(Seqable s) {
        try {
            return (ISeq) dropLast.invoke(s);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n s]
     */
    public static ISeq dropLast(Number n, Seqable s) {
        try {
            return (ISeq) dropLast.invoke(n, s);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static ISeq dropWhile(IFn pred, Seqable coll) {
        try {
            return (ISeq) dropWhile.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static ISeq reverse(Seqable coll) {
        try {
            return (ISeq) reverse.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static ISeq cycle(Seqable coll) {
        try {
            return (ISeq) cycle.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [c1 c2]
     */
    public static ISeq interleave(Seqable c1, Seqable c2) {
        try {
            return (ISeq) interleave.invoke(c1, c2);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [c1 c2 & colls]
     */
    public static ISeq interleave(Seqable c1, Seqable c2, Seqable... colls) {
        try {
            return (ISeq) interleave.applyTo(ArraySeq.create((Object[]) colls).cons(c2).cons(c1));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [sep coll]
     */
    public static ISeq interpose(Object sep, Seqable coll) {
        try {
            return (ISeq) interpose.invoke(sep, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static ISeq partition(Number n, Seqable coll) {
        try {
            return (ISeq) partition.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n step coll]
     */
    public static ISeq partition(Number n, Number step, Seqable coll) {
        try {
            return (ISeq) partition.invoke(n, step, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n step pad coll]
     */
    public static ISeq partition(Number n, Number step, Seqable pad, Seqable coll) {
        try {
            return (ISeq) partition.invoke(n, step, pad, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static IPersistentVector splitAt(Number n, Seqable coll) {
        try {
            return (IPersistentVector) splitAt.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [pred coll]
     */
    public static IPersistentVector splitWith(IFn pred, Seqable coll) {
        try {
            return (IPersistentVector) splitWith.invoke(pred, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [x]
     */
    public static ISeq repeat(Object x) {
        try {
            return (ISeq) repeat.invoke(x);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n x]
     */
    public static ISeq repeat(Number n, Object x) {
        try {
            return (ISeq) repeat.invoke(n, x);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f]
     */
    public static ISeq repeatedly(IFn f) {
        try {
            return (ISeq) repeatedly.invoke(f);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n x]
     */
    public static ISeq replicate(Number n, Object x) {
        try {
            return (ISeq) replicate.invoke(n, x);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [f x]
     */
    public static ISeq iterate(IFn f, Object x) {
        try {
            return (ISeq) iterate.invoke(f, x);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [end]
     */
    public static Range range(Number end) {
        try {
            return (Range) range.invoke(end);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [start end]
     */
    public static Range range(Number start, Number end) {
        try {
            return (Range) range.invoke(start, end);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [start end step]
     */
    public static Range range(Number start, Number end, Number step) {
        try {
            return (Range) range.invoke(start, end, step);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [to from]
     */
    public static ISeq into(Seqable to, Seqable from) {
        try {
            return (ISeq) into.invoke(to, from);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static ISeq distinct(Seqable coll) {
        try {
            return (ISeq) distinct.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static IPersistentSet set(Seqable coll) {
        try {
            return (IPersistentSet) set.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static IPersistentVector vec(Seqable coll) {
        try {
            return (IPersistentVector) vec.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static Object dorun(Seqable coll) {
        try {
            return dorun.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static Object dorun(Number n, Seqable coll) {
        try {
            return dorun.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static ISeq doall(Seqable coll) {
        try {
            return (ISeq) doall.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [n coll]
     */
    public static ISeq doall(Number n, Seqable coll) {
        try {
            return (ISeq) doall.invoke(n, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [coll]
     */
    public static ISeq sort(Object coll) {
        try {
            return (ISeq) sort.invoke(coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [comp coll]
     */
    public static ISeq sort(Comparator<?> comp, Seqable coll) {
        try {
            return (ISeq) sort.invoke(comp, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [keyfn coll]
     */
    public static ISeq sortBy(IFn keyfn, Seqable coll) {
        try {
            return (ISeq) sortBy.invoke(keyfn, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [keyfn comp coll]
     */
    public static ISeq sortBy(IFn keyfn, Comparator<?> comp, Seqable coll) {
        try {
            return (ISeq) sortBy.invoke(keyfn, comp, coll);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    /**
     * [keys vals]
     */
    public static IPersistentMap zipmap(Seqable keys, Seqable vals) {
        try {
            return (IPersistentMap) zipmap.invoke(keys, vals);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }
}
