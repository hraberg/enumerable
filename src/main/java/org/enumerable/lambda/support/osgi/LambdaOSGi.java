package org.enumerable.lambda.support.osgi;

import org.enumerable.lambda.weaving.Version;

import static org.enumerable.lambda.Lambda.λ;
import static org.enumerable.lambda.Parameters.s;

public class LambdaOSGi {
    public void run() {
        System.out.println(λ(s, s + Version.getVersionString()).call("[osgi] "));
    }
}
