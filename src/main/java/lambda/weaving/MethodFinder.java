package lambda.weaving;

import org.objectweb.asm.MethodVisitor;

class MethodFinder extends EmptyVisitor {
    String name;
    String desc;
    String onlyMethod;
    String onlyDesc;

    MethodFinder(String desc) {
        this.desc = desc;
    }

    public MethodVisitor visitMethod(int access, String name, String
            desc, String signature, String[] exceptions) {
        if (this.desc.equals(desc)) {
            this.name = name;
            this.desc = desc;
        }
        onlyMethod = name;
        onlyDesc = desc;
        return null;
    }

    MethodInfo getMethod() {
        if (name == null)
            return new MethodInfo(onlyMethod, onlyDesc);
        return new MethodInfo(name, desc);
    }
}