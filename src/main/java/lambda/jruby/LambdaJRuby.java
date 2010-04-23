package lambda.jruby;

import static lambda.exception.UncheckedException.*;
import static org.jruby.javasupport.JavaEmbedUtils.*;
import lambda.Fn0;
import lambda.Fn1;
import lambda.Fn2;
import lambda.Fn3;
import lambda.annotation.NewLambda;
import lambda.exception.LambdaWeavingNotEnabledException;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;
import org.jruby.runtime.BlockCallback;
import org.jruby.runtime.CallBlock;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

@SuppressWarnings("serial")
public class LambdaJRuby {
    public static Ruby ruby = Ruby.getGlobalRuntime();

    public abstract static class RubyProcFnBase extends RubyProc {
        class FnBlockCallback implements BlockCallback {
            public IRubyObject call(ThreadContext context, IRubyObject[] args, Block block) {
                Object result = null;
                args = getBlock().getBody().prepareArgumentsForCall(context, args, Block.Type.NORMAL);

                if (args.length == 0)
                    result = RubyProcFnBase.this.call();
                if (args.length == 1)
                    result = RubyProcFnBase.this.call(rubyToJava(args[0]));
                if (args.length == 2)
                    result = RubyProcFnBase.this.call(rubyToJava(args[0]), rubyToJava(args[1]));
                if (args.length == 3)
                    result = RubyProcFnBase.this
                            .call(rubyToJava(args[0]), rubyToJava(args[1]), rubyToJava(args[2]));

                return javaToRuby(ruby, result);
            }
        }

        public RubyProcFnBase(Arity arity) {
            super(ruby, ruby.getProc(), Block.Type.LAMBDA);
            ThreadContext context = ruby.getThreadService().getCurrentContext();
            initialize(context, CallBlock.newCallClosure(this, getType(), arity, new FnBlockCallback(), context));
        }

        public Object call() {
            return null;
        }

        public Object call(Object arg0) {
            return null;
        }

        public Object call(Object arg0, Object arg2) {
            return null;
        }

        public Object call(Object arg0, Object arg2, Object arg3) {
            return null;
        }
    }

    public static abstract class RubyProcFn0 extends RubyProcFnBase {
        public RubyProcFn0() {
            super(Arity.NO_ARGUMENTS);
        }

        public abstract Object call();
    }

    public static abstract class RubyProcFn1 extends RubyProcFnBase {
        public RubyProcFn1() {
            super(Arity.ONE_REQUIRED);
        }

        public abstract Object call(Object arg0);
    }

    public static abstract class RubyProcFn2 extends RubyProcFnBase {
        public RubyProcFn2() {
            super(Arity.TWO_REQUIRED);
        }

        public abstract Object call(Object arg0, Object arg1);
    }

    public static abstract class RubyProcFn3 extends RubyProcFnBase {
        public RubyProcFn3() {
            super(Arity.THREE_REQUIRED);
        }

        public abstract Object call(Object arg0, Object arg1, Object arg2);
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
    public static RubyProcFn1 lambda(Object arg0, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda extending {@link RubyProc} taking two arguments.
     */
    @NewLambda
    public static RubyProcFn2 lambda(Object arg0, Object arg1, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Creates a new lambda extending {@link RubyProc} taking three arguments.
     */
    @NewLambda
    public static RubyProcFn3 lambda(Object arg0, Object arg1, Object arg2, Object block) {
        throw new LambdaWeavingNotEnabledException();
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn0}.
     */
    public static Fn0<Object> toFn0(final RubyProc proc) {
        return new Fn0<Object>() {
            public Object call() {
                try {
                    return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[0]));
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn1}.
     */
    public static Fn1<Object, Object> toFn1(final RubyProc proc) {
        return new Fn1<Object, Object>() {
            public Object call(Object a1) {
                try {
                    return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(),
                            new IRubyObject[] { javaToRuby(ruby, a1) }));
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn2}.
     */
    public static Fn2<Object, Object, Object> toFn2(final RubyProc proc) {
        return new Fn2<Object, Object, Object>() {
            public Object call(Object a1, Object a2) {
                try {
                    return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                            javaToRuby(ruby, a1), javaToRuby(ruby, a2) }));
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link RubyProc} in a {@link Fn3}.
     */
    public static Fn3<Object, Object, Object, Object> toFn3(final RubyProc proc) {
        return new Fn3<Object, Object, Object, Object>() {
            public Object call(Object a1, Object a2, Object a3) {
                try {
                    return rubyToJava(proc.call(ruby.getThreadService().getCurrentContext(), new IRubyObject[] {
                            javaToRuby(ruby, a1), javaToRuby(ruby, a2), javaToRuby(ruby, a3) }));
                } catch (Exception e) {
                    throw uncheck(e);
                }
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
                try {
                    return fn.call();
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link Fn1} in a {@link RubyProc}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RubyProc toProc(final Fn1 fn) {
        return new RubyProcFn1() {
            public Object call(Object arg0) {
                try {
                    return fn.call(arg0);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link Fn2} in a {@link RubyProc}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RubyProc toProc(final Fn2 fn) {
        return new RubyProcFn2() {
            public Object call(Object arg0, Object arg1) {
                try {
                    return fn.call(arg0, arg1);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }

    /**
     * Wraps the {@link Fn3} in a {@link RubyProc}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static RubyProc toProc(final Fn3 fn) {
        return new RubyProcFn3() {
            public Object call(Object arg0, Object arg1, Object arg2) {
                try {
                    return fn.call(arg0, arg1, arg2);
                } catch (Exception e) {
                    throw uncheck(e);
                }
            }
        };
    }
}
