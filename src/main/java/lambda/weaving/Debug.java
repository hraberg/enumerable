package lambda.weaving;

import static java.lang.System.*;

class Debug {
    static boolean DEBUG = Boolean.valueOf(getProperty("lambda.weaving.debug"));

    static String debugIndentation = "";

    static void debugIndent() {
        debugIndentation += " ";
    }

    static void debugDedent() {
        debugIndentation = debugIndentation.substring(0, debugIndentation.length() - 1);
    }

    static void debug(String msg) {
        if (DEBUG)
            err.println(debugIndentation + msg);
    }
}
