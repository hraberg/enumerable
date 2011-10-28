package lambda;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static lambda.Fn0.*;
import static lambda.Lambda.*;
import static lambda.exception.UncheckedException.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;

import lambda.annotation.LambdaLocal;
import lambda.annotation.LambdaParameter;
import lambda.weaving.LambdaLoader;

public class MatcherSpike {
    public static int fib(int n) {
        return match(n,
                     0, 0, 
                     1, 1,
                     N, λ(fib(n - 1) + fib(n - 2)));
    }
    
    public static int signum(int n) {
        return match(n,
                     0,          0, 
                     λ(n < 0),  -1,
                     _,          1);
    }

    public static <T> int length(List<T> list) {
        return match(list,
                      _(),      0,
                      _(_, xs), λ(xs, 1 + length(xs)));
    }

    public static <T> int length2(List<T> list) {
        return match(list,
                     _(),        0,
                     List.class, λ(1 + length2(tail(list))));
    }

    public static <T> List<T> take(int n, List<T> list) {
        return match2(n, list,
                      0,  _,       _(),
                      _, _(),      _(),
                      N, _(x, xs),  λ(x, xs, cons(x, take(n - 1, xs))));
    }

    public static <T> List<T> take2(int n, List<T> list) {
        return match2(n, list,
                      0, _,         _(),
                      _, _(),       _(),
                      N, List.class, λ(cons(head(list), take2(n - 1, tail(list)))));
    }

    public static void main(String... args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);
        
        out.println(fib(20));

        out.println(signum(3));
        out.println(signum(0));
        out.println(signum(-2));
        
        out.println(length(asList(1, 2, 3)));
        out.println(length(asList()));

        out.println(length2(asList(1, 2, 3)));
        out.println(length2(asList()));
        
        out.println(take(3, asList(1, 2, 3, 4, 5)));
        out.println(take2(2, asList(1, 2, 3, 4, 5)));
    }

    @LambdaParameter
    final static Object _ = new Object();

    @LambdaParameter
    final static List<?> xs = asList(new Object());
    @LambdaParameter
    final static Object x = new Object();
    final static Class<Integer> N = Integer.class;

    private static List<?> _(Object... list) {
        return asList(list);
    }

    private static List<?> _(Object head, List<?> tail) {
        return cons(head, tail);
    }


    private static List<? extends Object> tail(List<?> list) {
        return list.isEmpty() ? _() : list.subList(1, list.size());
    }

    private static Object head(List<?> list) {
        return list.isEmpty() ? _() : list.get(0);
    }

    private static List<?> cons(Object head, List<?> tail) {
        List<Object> list = new ArrayList<Object>();
        list.add(head);
        list.addAll(tail);
        return unmodifiableList(list);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R match(T value, Object... claueses) {
        if (claueses.length % 2 != 0) throw new IllegalArgumentException("must have even amount of clauses");

        for (int i = 0; i < claueses.length; i += 2) {
            try {
                Object matcher = claueses[i];
                Object clause = claueses[i + 1];

                if (matcher instanceof Fn1)
                    matcher = ((Fn1<T, ?>) matcher).call(value);

                else if (matcher instanceof Fn0)
                    matcher = ((Fn0<?>) matcher).call();

                if (match(value, matcher))
                    return evaluate(clause, value);

                if (matcher instanceof List && value instanceof List && clause instanceof Fn0) {
                    List<?> arguments = deconstructList((List<?>) value, (List<?>) matcher, (Fn0<?>) clause);
                    if (!arguments.isEmpty())
                        return evaluate(clause, arguments.toArray());
                }

            } catch (IllegalArgumentException ok) {
                ok.printStackTrace();
            }
        }
        throw new IllegalArgumentException("no match");
    }

    @SuppressWarnings("unchecked")
    public static <T, V, R> R match2(T value1, V value2, Object... claueses) {
        if (claueses.length % 3 != 0) throw new IllegalArgumentException("must have right amount of clauses");

        for (int i = 0; i < claueses.length; i += 3) {
            try {
                Object matcher1 = claueses[i];
                Object matcher2 = claueses[i + 1];
                Object clause = claueses[i + 2];

                if (matcher1 instanceof Fn1)
                    matcher1 = ((Fn1<T, ?>) matcher1).call(value1);

                else if (matcher1 instanceof Fn0 && ((Fn0<?>) matcher1).arity == 0)
                    matcher1 = ((Fn0<?>) matcher1).call();

                if (matcher2 instanceof Fn1)
                    matcher2 = ((Fn1<V, ?>) matcher2).call(value2);

                else if (matcher2 instanceof Fn0 && ((Fn0<?>) matcher2).arity == 0)
                    matcher2 = ((Fn0<?>) matcher2).call();

                if (match(value1, matcher1) && match(value2, matcher2))
                    return evaluate(clause, value1, value2);
                
                if (matcher1 instanceof List && value1 instanceof List && clause instanceof Fn0) {
                    List<?> arguments = deconstructList((List<?>) value1, (List<?>) matcher1, (Fn0<?>) clause);
                    if (!arguments.isEmpty())
                        return evaluate(clause, arguments.toArray());
                }

                if (matcher2 instanceof List && value2 instanceof List && clause instanceof Fn0) {
                    List<?> arguments = deconstructList((List<?>) value2, (List<?>) matcher2, (Fn0<?>) clause);
                    if (!arguments.isEmpty())
                        return evaluate(clause, arguments.toArray());
                }

            } catch (IllegalArgumentException ok) {
                ok.printStackTrace();
            }
        }
        throw new IllegalArgumentException("no match");
    }

    private static <V> List<Object> deconstructList(List<?> value, List<?> matcher, Fn0<?> clause) {
        List<Object> arguments = new ArrayList<Object>();
        LambdaLocal[] parameters = clause.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            try {
                Class<?> owner = Class.forName(parameters[i].parameterClass());
                Field field = owner.getDeclaredField(parameters[i].name());
                field.setAccessible(true);
                Object matchedField = field.get(null);

                if (matchedField instanceof List) 
                    matchedField = head((List<?>) matchedField);
 
                int indexOf = matcher.indexOf(matchedField);

                if (indexOf >= 0) {
                    value = value.subList(indexOf, value.size());
                    if (List.class.isAssignableFrom(field.getType()) && i == parameters.length - 1)
                        arguments.add(value);
                    else
                        arguments.add(head(value));
                }

            } catch (Exception e) {
                throw uncheck(e);
            }
        }
        return arguments;
    }

    @SuppressWarnings("unchecked")
    private static <T, R> boolean match(T value, Object matcher) {
        if (matcher == _)
            return true;
        
        if (matcher == null && value != null)
            return false;

        if (matcher == null && value == null)
            return true;

        if (matcher.equals(value))
            return true;
        
        if (matcher instanceof org.hamcrest.Matcher)
            return ((org.hamcrest.Matcher<?>) matcher).matches(value);

        if (isInstance(value, matcher))
            return true;
        
        if (matcher == Boolean.TRUE && value != Boolean.FALSE)
            return true;
         
        if (matcher instanceof String && String.valueOf(value).matches((String) matcher))
            return true;

        if (matcher instanceof Fn1 && isNotFalseOrNull(((Fn1<T, ?>) matcher).call(value)))
            return true;

        return false;
    }

    @SuppressWarnings("unchecked")
    private static <R> R evaluate(Object clause, Object... values) {
        if (clause instanceof Fn0)
            return ((Fn0<R>) clause).apply(values);
        return (R) clause;
    }

    private static <T> boolean isInstance(T value, Object constant) {
        return constant instanceof Class && ((Class<?>) constant).isInstance(value);
    }
}
