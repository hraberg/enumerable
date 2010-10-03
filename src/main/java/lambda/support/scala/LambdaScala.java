package lambda.support.scala;

import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;
import scala.Function0;
import scala.Function1;
import scala.Function2;
import scala.Function3;

@SuppressWarnings("serial")
public class LambdaScala {
    @NewLambda
    public static <R> FunctionFn0<R> function(R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A1, R> FunctionFn1<A1, R> function(A1 a1, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A1, A2, R> FunctionFn2<A1, A2, R> function(A1 a1, A2 a2, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <A1, A2, A3, R> FunctionFn3<A1, A2, A3, R> function(A1 a1, A2 a2, A3 a3, R block) {
        throw new LambdaWeavingNotEnabledException();
    }

    public static <R> Function0<R> toFunction(final Fn0<R> block) {
        return new FunctionFn0<R>() {
            public R apply() {
                return block.call();
            }
        };
    }

    public static <A1, R> Function1<A1, R> toFunction(final Fn1<A1, R> block) {
        return LambdaScalaFactory.toFunction(block);
    }

    public static <A1, A2, R> Function2<A1, A2, R> toFunction(final Fn2<A1, A2, R> block) {
        return LambdaScalaFactory.toFunction(block);
    }

    public static <A1, A2, A3, R> Function3<A1, A2, A3, R> toFunction(final Fn3<A1, A2, A3, R> block) {
        return LambdaScalaFactory.toFunction(block);
    }

    public static <R> Fn0<R> toFn0(final Function0<R> f) {
        return new Fn0<R>() {
            public R call() {
                return f.apply();
            }
        };
    }

    public static <A1, R> Fn1<A1, R> toFn1(final Function1<A1, R> f) {
        return new Fn1<A1, R>() {
            public R call(A1 a1) {
                return f.apply(a1);
            }
        };
    }

    public static <A1, A2, R> Fn2<A1, A2, R> toFn2(final Function2<A1, A2, R> f) {
        return new Fn2<A1, A2, R>() {
            public R call(A1 a1, A2 a2) {
                return f.apply(a1, a2);
            }
        };
    }

    public static <A1, A2, A3, R> Fn3<A1, A2, A3, R> toFn3(final Function3<A1, A2, A3, R> f) {
        return new Fn3<A1, A2, A3, R>() {
            public R call(A1 a1, A2 a2, A3 a3) {
                return f.apply(a1, a2, a3);
            }
        };
    }

//    static Object eval(String expression) {
//        PrintStream realOut = System.out;
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        try {
//            System.setOut(new PrintStream(out));
//            Object[] _result = new Object[1];
//            interpreter.bind("_result", "Array[Any]", _result);
//            interpreter.interpret("_result(0) = " + expression);
//            return _result[0];
//        } catch (RuntimeException e) {
//            realOut.println(out);
//            throw e;
//        } finally {
//            System.setOut(realOut);
//        }
//    }
}
