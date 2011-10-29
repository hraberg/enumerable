package org.enumerable.lambda.weaving;

import static java.lang.System.*;

public class Debug {
    public static boolean debug = Boolean.valueOf(getProperty("lambda.weaving.debug"));
    public static boolean devDebug = Boolean.valueOf(getProperty("lambda.weaving.debug.dev"));

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

    public static void devDebug(String msg) {
        if (devDebug)
            out.println(debugIndentation + msg);
    }
}
