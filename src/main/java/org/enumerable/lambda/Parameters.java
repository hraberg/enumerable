package org.enumerable.lambda;

import java.util.Collection;

import org.enumerable.lambda.annotation.LambdaParameter;


/**
 * A set of default parameters to use in Lambda definitions.
 * 
 * @see LambdaParameter
 */
public class Parameters {
    @LambdaParameter
    public static int n;
    @LambdaParameter
    public static int m;
    @LambdaParameter
    public static int i;
    @LambdaParameter
    public static int idx;
    @LambdaParameter
    public static long k;
    @LambdaParameter
    public static long l;
    @LambdaParameter
    public static double d;
    @LambdaParameter
    public static double x;
    @LambdaParameter
    public static double y;
    @LambdaParameter
    public static char c;
    @LambdaParameter
    public static boolean b;
    @LambdaParameter
    public static String s;
    @LambdaParameter
    public static String t;
    @LambdaParameter
    public static Collection<?> col;
    @LambdaParameter
    public static Object obj;
}
