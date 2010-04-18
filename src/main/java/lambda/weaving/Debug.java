package lambda.weaving;

import static java.lang.System.*;

public class Debug {
    static boolean debug = Boolean.valueOf(getProperty("lambda.weaving.debug"));
    static String debugIndentation = "";

    public static void debugIndent() {
        debugIndentation += " ";
    }

    public static void debugDedent() {
        debugIndentation = debugIndentation.substring(0, debugIndentation.length() - 1);
    }

    public static void debug(String msg) {
        if (debug)
            out.println(debugIndentation + msg);
    }
}
