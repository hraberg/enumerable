package org.enumerable.lambda.support.jruby;

import static org.enumerable.lambda.exception.UncheckedException.*;
import static org.jruby.javasupport.JavaEmbedUtils.*;

import java.lang.reflect.Method;

import org.enumerable.lambda.Fn0;
import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Fn2;
import org.enumerable.lambda.Fn3;
import org.enumerable.lambda.annotation.NewLambda;
import org.enumerable.lambda.exception.LambdaWeavingNotEnabledException;
import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;
import org.jruby.runtime.BlockCallback;
import org.jruby.runtime.CallBlock;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * This is class is similar {@link org.enumerable.lambda.Lambda}, but instead of
 * creating lambdas inheriting from {@link org.enumerable.lambda.Fn0} it creates
 * lambdas extending {@link RubyProc} to be used together with JRuby.
 */
@SuppressWarnings("serial")
public class LambdaJRuby {
    public abstract static class RubyProcFnBase extends RubyProc {
        private Method setup;

        class FnBlockCallback implements BlockCallback {
            public IRubyObject call(ThreadContext context, IRubyObject[] args, Block block) {
                args = getNormalArgumentsFromCallBlocksSingleRestArg(context, args);
                getBlock().arity().checkArity(context.getRuntime(), args);

                Object result = null;

                if (args.length == 0)
                    result = RubyProcFnBase.this.call();

                else if (args.length == 1)
                    result = RubyProcFnBase.this.call(rubyToJava(args[0]));

                else if (args.length == 2)
                    result = RubyProcFnBase.this.call(rubyToJava(args[0]), rubyToJava(args[1]));

                else if (args.length == 3)
                    result = RubyProcFnBase.this
                            .call(rubyToJava(args[0]), rubyToJava(args[1]), rubyToJava(args[2]));

                return javaToRuby(getRuntime(), result);
            }

            IRubyObject[] getNormalArgumentsFromCallBlocksSingleRestArg(ThreadContext context, IRubyObject[] args) {
                return getBlock().getBody().prepareArgumentsForCall(context, args, Block.Type.NORMAL);
            }
        }

        public RubyProcFnBase(Ruby runtime) {
            super(runtime, runtime.getProc(), Block.Type.LAMBDA);
            try {
                ThreadContext context = getRuntime().getThreadService().getCurrentContext();
                setup().invoke(this, CallBlock.newCallClosure(this, getType(), getArityFromInstance(),
                        new FnBlockCallback(), context));
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        private Method setup() throws NoSuchMethodException {
            if (setup == null) {
                setup = RubyProc.class.getDeclaredMethod("setup", Block.class);
                setup.setAccessible(true);
            }
            return setup;
        }

        Arity getArityFromInstance() {
            return Arity.createArity(Fn0.getAndCheckArityForMethod(getImplementingClass(), "call"));
        }

        Class<?> getImplementingClass() {
            return getClass();
        }

        protected Object call() {
            throw new UnsupportedOperationException();
        }

        protected Object call(Object a1) {
            throw new UnsupportedOperationException();
        }

        protected Object call(Object a1, Object a2) {
            throw new UnsupportedOperationException();
        }

        protected Object call(Object a1, Object a2, Object a3) {
            throw new UnsupportedOperationException();
        }
    }

    public static abstract class RubyProcFn0 extends RubyProcFnBase {
        public RubyProcFn0() {
            super(Ruby.getGlobalRuntime());
        }

        public abstract Object call();
    }

    public static abstract class RubyProcFn1 extends RubyProcFn0 {
        public Object call() {
            return call(default$1());
        }

        protected Object default$1() {
            return null;
        }

        public abstract Object call(Object a1);
    }

    public static abstract class RubyProcFn2 extends RubyProcFn1 {
        public Object call(Object a1) {
            return call(a1, default$2());
        }

        protected Object default$2() {
            return null;
        }

        public abstract Object call(Object a1, Object a2);
    }

    public static abstract class RubyProcFn3 extends RubyProcFn2 {
        public Object call(Object a1, Object a2) {
            return call(a1, a2, default$3());
        }

        protected Object default$3() {
            return null;
        }

        public abstract Object call(Object a1, Object a2, Object a3);
    }

    /**
     * Creates a new lambda extending {@link RubyProc} taking no arguments.
     */
    @NewLambda
    public static RubyProcFn0 lambda(Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda extending {@link RubyProc} taking one argument.
     */
    @NewLambda
    public static RubyProcFn1 lambda(Object a1, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda extending {@link RubyProc} taking two arguments.
     */
    @NewLambda
    public static RubyProcFn2 lambda(Object a1, Object a2, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda extending {@link RubyProc} taking three arguments.
     */
    @NewLambda
    public static RubyProcFn3 lambda(Object a1, Object a2, Object a3, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn0}.
     */
    public static Fn0<Object> toFn0(final RubyProc proc) {
        return new Fn0<Object>() {
            public Object call() {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]));
            }
        };
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn1}.
     */
    public static Fn1<Object, Object> toFn1(final RubyProc proc) {
        return new Fn1<Object, Object>() {
            public Object call() {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]));
            }

            public Object call(Object a1) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(),
                        new IRubyObject[] { javaToRuby(ruby, a1) }));
            }
        };
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn2}.
     */
    public static Fn2<Object, Object, Object> toFn2(final RubyProc proc) {
        return new Fn2<Object, Object, Object>() {
            public Object call() {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]));
            }

            public Object call(Object a1) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(),
                        new IRubyObject[] { javaToRuby(ruby, a1) }));
            }

            public Object call(Object a1, Object a2) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                        javaToRuby(ruby, a1), javaToRuby(ruby, a2) }));
            }
        };
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn3}.
     */
    public static Fn3<Object, Object, Object, Object> toFn3(final RubyProc proc) {
        return new Fn3<Object, Object, Object, Object>() {
            public Object call() {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]));
            }

            public Object call(Object a1) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(),
                        new IRubyObject[] { javaToRuby(ruby, a1) }));
            }

            public Object call(Object a1, Object a2) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                        javaToRuby(ruby, a1), javaToRuby(ruby, a2) }));
            }

            public Object call(Object a1, Object a2, Object a3) {
                Ruby ruby = proc.getRuntime();
                return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                        javaToRuby(ruby, a1), javaToRuby(ruby, a2), javaToRuby(ruby, a3) }));
            }
        };
    }

    /**
     * Wraps the {@link Fn0} in a {@link RubyProc}.
     */
    @SuppressWarnings("rawtypes")
    public static RubyProc toProc(final Fn0 fn) {
        return new RubyProcFn0() {
            public Object call() {
                return fn.call();
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link RubyProc}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RubyProc toProc(final Fn1 fn) {
        return new RubyProcFn1() {
            public Object call() {
                return fn.call();
            }

            public Object call(Object a1) {
                return fn.call(a1);
            }

            Class<?> getImplementingClass() {
                return fn.getClass();
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in a {@link RubyProc}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RubyProc toProc(final Fn2 fn) {
        return new RubyProcFn2() {
            public Object call() {
                return fn.call();
            }

            public Object call(Object a1) {
                return fn.call(a1);
            }

            public Object call(Object a1, Object a2) {
                return fn.call(a1, a2);
            }

            Class<?> getImplementingClass() {
                return fn.getClass();
            }
        };
    }

    /**
     * Wraps the {@link Fn3} in a {@link RubyProc}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RubyProc toProc(final Fn3 fn) {
        return new RubyProcFn3() {
            public Object call() {
                return fn.call();
            }

            public Object call(Object a1) {
                return fn.call(a1);
            }

            public Object call(Object a1, Object a2) {
                return fn.call(a1, a2);
            }

            public Object call(Object a1, Object a2, Object a3) {
                return fn.call(a1, a2, a3);
            }

            Class<?> getImplementingClass() {
                return fn.getClass();
            }
        };
    }
}
