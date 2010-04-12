package lambda.weaving;

import static org.objectweb.asm.Type.*;

import java.util.Arrays;

import org.objectweb.asm.MethodVisitor;

class MethodFinder extends EmptyVisitor {
    String descToMatch;
    MethodInfo method;

    MethodFinder(String desc) {
        this.descToMatch = desc;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isAbstract(access)) {
            if (Arrays.equals(getArgumentTypes(this.descToMatch), getArgumentTypes(desc))) {
                method = new MethodInfo(name, desc);
            }
            if (this.method == null && getArgumentTypes(this.descToMatch).length == getArgumentTypes(desc).length) {
                method = new MethodInfo(name, desc);
            }
        }
        return null;
    }

    boolean isAbstract(int access) {
        return (access & ACC_ABSTRACT) != 0;
    }

    MethodInfo getMethod() {
        return method;
    }
}