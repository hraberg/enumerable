package lambda.weaving;

import java.util.Arrays;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Type.*;

class MethodFinder extends EmptyVisitor {
    String name;
    String desc;

    String descToMatch;

    MethodFinder(String desc) {
        this.descToMatch = desc;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isAbstract(access)) {
            if (Arrays.equals(getArgumentTypes(this.descToMatch), getArgumentTypes(desc))) {
                this.name = name;
                this.desc = desc;
            }
            if (this.name == null && getArgumentTypes(this.descToMatch).length == getArgumentTypes(desc).length) {
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
        if (name == null)
            return null;
        return new MethodInfo(name, desc);
    }
}