package lambda.support.totallylazy;

import static lambda.exception.UncheckedException.*;

import java.util.concurrent.Callable;

import lambda.Fn1;
import lambda.Fn2;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Runnable1;

public class LambdaTotallyLazy {
    @NewLambda
    public static Runnable runnable(){
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <Input> Runnable1<Input> runnable(Input input){
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <Input> Callable<Input> callable(Input input){
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <Input, Output> Callable1<Input,Output> callable(Input input, Output output){
        throw new LambdaWeavingNotEnabledException();
    }

    @NewLambda
    public static <Input1, Input2, Output> Callable2<Input1, Input2, Output> callable(Input1 input1, Input2 input2, Output output){
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link Callable1} in a {@link Fn1}.
     */
    @SuppressWarnings("serial")
    public static <Input,Output> Fn1<Input, Output> toFn(final Callable1<Input,Output> callable) {
        return new Fn1<Input, Output>() {
            public Output call(Input input) {
                try {
                    return callable.call(input);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link Callable2} in a {@link Fn2}.
     */
    @SuppressWarnings("serial")
    public static <Input1, Input2, Output> Fn2<Input1, Input2, Output> toFn(final Callable2<Input1, Input2, Output> callable) {
        return new Fn2<Input1, Input2, Output>() {
            public Output call(Input1 input1, Input2 input2) {
                try {
                    return callable.call(input1, input2);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link Callable1}.
     */
    public static <Input, Output> Callable1<Input, Output> toCallable(final Fn1<Input, Output> fn) {
        return new Callable1<Input, Output>() {
            public Output call(Input input) {
                return fn.call(input);
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in a {@link Callable2}.
     */
    public static <Input1, Input2, Output> Callable2<Input1, Input2, Output> toCallable(final Fn2<Input1, Input2, Output> fn) {
        return new Callable2<Input1, Input2, Output>() {
            public Output call(Input1 input1, Input2 input2) {
                return fn.call(input1, input2);
            }
        };
    }
}
