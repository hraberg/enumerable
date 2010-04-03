package lambda.weaving;

import static lambda.exception.UncheckedException.*;
import static org.objectweb.asm.Type.*;

import java.util.HashMap;
import java.util.Map;

import lambda.weaving.MethodInfo.LambdaInfo;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class FirstPassClassVisitor extends EmptyVisitor {
    Map<String, MethodInfo> methodsByNameAndDesc = new HashMap<String, MethodInfo>();

    LambdaTransformer transformer;

    MethodInfo currentMethod;
    private LambdaInfo currentLambda;

    FirstPassClassVisitor(LambdaTransformer transformer) {
        this.transformer = transformer;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        currentMethod = new MethodInfo(name, desc);
        methodsByNameAndDesc.put(currentMethod.getNameAndDesc(), currentMethod);
        return this;
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        try {
            if (!inLambda() && transformer.isLambdaParameterField(owner, name))
                if (transformer.isLambdaParameterField(owner, name))
                    currentLambda = currentMethod.newLambda();

            if (transformer.isLambdaParameterField(owner, name))
                currentMethod.lastLambda().setParameterInfo(name, getType(desc));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public void visitIincInsn(int var, int increment) {
        if (inLambda())
            currentLambda.accessLocal(var);
    }

    public void visitVarInsn(int opcode, int operand) {
        if (inLambda())
            currentLambda.accessLocal(operand);
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        currentMethod.setInfoForLocal(index, name, getType(desc));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        try {
            if (inLambda() && opcode == INVOKESTATIC) {
                if (transformer.isNewLambdaMethod(owner, name, desc)) {
                    currentLambda.setType(getReturnType(desc));
                    currentLambda = null;
                }
            }
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    boolean inLambda() {
        return currentLambda != null;
    }

    boolean hasNoLambdas() {
        for (MethodInfo method : methodsByNameAndDesc.values())
            if (!method.lambdas.isEmpty())
                return false;
        return true;
    }
}