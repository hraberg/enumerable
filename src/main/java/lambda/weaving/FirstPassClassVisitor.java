package lambda.weaving;

import static lambda.weaving.LambdaTransformer.*;
import static org.objectweb.asm.Type.*;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class FirstPassClassVisitor extends EmptyVisitor {
    Map<String, MethodInfo> methodsByName = new HashMap<String, MethodInfo>();

    boolean inLambda;
    MethodInfo currentMethod;

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        currentMethod = new MethodInfo(name, desc);
        methodsByName.put(currentMethod.getFullName(), currentMethod);
        return this;
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        try {
            if (!inLambda && opcode == GETSTATIC || opcode == PUTSTATIC) {
                if (isLambdaParameterField(owner, name)) {
                    inLambda = true;
                    currentMethod.newLambda();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void visitIincInsn(int var, int increment) {
        if (inLambda) {
            currentMethod.accessLocalFromLambda(var);
        }
    }

    public void visitVarInsn(int opcode, int operand) {
        if (inLambda) {
            currentMethod.accessLocalFromLambda(operand);
        }
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        currentMethod.setTypeOfLocal(index, getType(desc));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        try {
            if (inLambda && opcode == INVOKESTATIC) {
                if (isNewLambdaMethod(owner, name, desc)) {
                    currentMethod.setLambdaArity(getArgumentTypes(desc).length - 1);
                    inLambda = false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean hasNoLambdas() {
        for (MethodInfo method : methodsByName.values()) {
            if (!method.lambdas.isEmpty())
                return false;
        }
        return true;
    }
}