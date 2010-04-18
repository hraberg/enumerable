package lambda.weaving;

import static java.lang.System.*;

import java.io.InputStream;
import java.util.Map;

import lambda.weaving.tree.LambdaTreeTransformer;

public interface ILambdaTransformer {
    static boolean tree = Boolean.valueOf(getProperty("lambda.weaving.tree")) || true;

    static class Factory {
        public static ILambdaTransformer create() {
            return tree ? new LambdaTreeTransformer() : new LambdaTransformer();
        }
    }

    Map<String, byte[]> getLambdasByClassName();

    byte[] transform(String name, InputStream in) throws Exception;
}