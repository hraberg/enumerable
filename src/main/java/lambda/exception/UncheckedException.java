package lambda.exception;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SuppressWarnings("serial")
public class UncheckedException extends RuntimeException {
    public static Set<String> filteredPackages = new HashSet<String>();

    static {
        filteredPackages.add("sun.reflect");
        filteredPackages.add("org.junit");
        filteredPackages.add("org.eclipse.jdt.internal");
        filteredPackages.add("java.lang.reflect");
    }

    Throwable wrapped;

    public static RuntimeException uncheck(Throwable t) {
        if (t.getCause() != null)
            return uncheck(t.getCause());
        if (t instanceof RuntimeException) {
            t.setStackTrace(filterStackTrace(t.getStackTrace()));
            return (RuntimeException) t;
        }
        return new UncheckedException(t);
    }

    UncheckedException(Throwable t) {
        super(t.getMessage(), t.getCause());
        this.wrapped = t;
        setStackTrace(filterStackTrace(t.getStackTrace()));
    }

    static StackTraceElement[] filterStackTrace(StackTraceElement[] stackTrace) {
        List<StackTraceElement> trace = new ArrayList<StackTraceElement>();
        for (StackTraceElement element : stackTrace)
            if (!isFilteredPackage(element))
                    trace.add(element);
        return trace.toArray(new StackTraceElement[0]);
    }

    static boolean isFilteredPackage(StackTraceElement element) {
        for (String prefix : filteredPackages)
            if (element.getClassName().startsWith(prefix))
                return true;
        return false;
    }

    public String toString() {
        String s = wrapped.getClass().getName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}