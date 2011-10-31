package org.enumerable.lambda.enumerable;

import org.enumerable.lambda.Version;
import org.enumerable.lambda.annotation.LambdaParameter;
import org.enumerable.lambda.enumerable.collection.EList;
import org.enumerable.lambda.weaving.LambdaLoader;

import java.util.Map;

import static org.enumerable.lambda.Lambda.λ;
import static org.enumerable.lambda.Parameters.s;
import static org.enumerable.lambda.Parameters.t;
import static org.enumerable.lambda.enumerable.collection.ECollections.list;
import static org.enumerable.lambda.enumerable.collection.ECollections.pair;

public class AdvancedExample {
    public static void main(String[] args) {
        LambdaLoader.bootstrapMainIfNotEnabledAndExitUponItsReturn(args);
        System.out.println("[example] " + Version.getVersionString());

        new AdvancedExample().run();
    }

    @LambdaParameter
    static Map.Entry<String, EList<String>> e;

    @LambdaParameter
    static Map.Entry<String, Integer> f;

    private void run() {
        String text = "a b c a c d e f a a b b";
        System.out.println(
                list(text.split(" "))
                .groupBy(λ(s, s))      //unfortunately this reads nicer than identity() as the latter one does need explicit type argument
                .collect(λ(e, pair(e.getKey(), e.getValue().size()))) // perhaps i would like to have collectValues() method to make this more readable
                .sortBy(λ(f, f.getKey()))
                .collect(λ(f, f.getKey() + " => " + f.getValue()))
                .inject(λ(s, t, s + "\n" + t))
        ); 

        /*
        Will output something along these lines:
            a => 4
            b => 3
            c => 2
            d => 1
            e => 1
            f => 1
         */
    }
}
