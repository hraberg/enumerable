package lambda.weaving.tree;

import static lambda.weaving.Debug.*;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Type.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lambda.weaving.ClassInjector;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer.LambdaAnalyzer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class LambdaTreeTransformer implements Opcodes {
    Map<String, byte[]> lambdasByClassName = new HashMap<String, byte[]>();

    ClassInjector injector = new ClassInjector();

    public LambdaTreeTransformer() {
        debug("current class loader is " + getClass().getClassLoader());
    }

    public Map<String, byte[]> getLambdasByClassName() {
        return lambdasByClassName;
    }

    public byte[] transform(String name, InputStream in) throws Exception {
        if (lambdasByClassName.containsKey(name)) {
            debug("generated lambda requested by the class loader " + name);
            return lambdasByClassName.get(name);
        }

        ClassReader cr = new ClassReader(in);
        name = cr.getClassName();

        LambdaTreeWeaver weaver = new LambdaTreeWeaver(cr);
        ClassNode cn = weaver.analyze().transform();

        if (!weaver.hasLambdas())
            return null;

        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
        cn.accept(cw);

        byte[] bs = cw.toByteArray();

        injector.dump(name, bs);
        injector.verifyIfAsmUtilIsAvailable(bs);

        for (LambdaAnalyzer la : weaver.getLambdas()) {
            cw = new ClassWriter(COMPUTE_FRAMES);
            la.lambda.accept(cw);
            newLambdaClass(getObjectType(la.lambdaClass()).getClassName(), cw.toByteArray());
        }

        return bs;
    }

    void newLambdaClass(String name, byte[] bs) {
        lambdasByClassName.put(name, bs);
        
        InMemoryCompiler.registerLambda(name, bs);

        injector.dump(name, bs);
        injector.verifyIfAsmUtilIsAvailable(bs);
        injector.inject(getClass().getClassLoader(), name, bs);
    }
}
