package lambda.weaving;

import static org.objectweb.asm.Type.*;

import java.util.Arrays;

import org.objectweb.asm.MethodVisitor;

class MethodFinder extends EmptyVisitor {
    String name;
    String desc;

    MethodFinder(String desc) {
        this.desc = desc;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isAbstract(access)) {
            if (Arrays.equals(getArgumentTypes(this.desc), getArgumentTypes(desc))) {
                this.name = name;
                this.desc = desc;
            }
            if (this.name == null && getArgumentTypes(this.desc).length == getArgumentTypes(desc).length) {
                this.name = name;
                this.desc = desc;
            }
        }
        return null;
    }

    boolean isAbstract(int access) {
        return (access & ACC_ABSTRACT) != 0;
    }

    MethodInfo getMethod() {
        return new MethodInfo(name, desc);
    }
}