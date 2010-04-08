package lambda.weaving;

import static lambda.exception.UncheckedException.*;
import static org.objectweb.asm.Type.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lambda.weaving.MethodInfo.LambdaInfo;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

class FirstPassClassVisitor extends EmptyVisitor {
    Map<String, MethodInfo> methodsByNameAndDesc = new HashMap<String, MethodInfo>();

    LambdaTransformer transformer;

    MethodInfo currentMethod;
    LambdaInfo currentLambda;

    Map<Integer, Integer> localsToNumberOfWrites;

    boolean resolvingTypeUsingCheckCast;

    FirstPassClassVisitor(LambdaTransformer transformer) {
        this.transformer = transformer;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        currentMethod = new MethodInfo(name, desc);
        methodsByNameAndDesc.put(currentMethod.getNameAndDesc(), currentMethod);
        localsToNumberOfWrites = new HashMap<Integer, Integer>();
        return this;
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        try {
            if (!inLambda() && transformer.isLambdaParameterField(owner, name))
                if (transformer.isLambdaParameterField(owner, name))
                    currentLambda = currentMethod.newLambda();

            if (transformer.isLambdaParameterField(owner, name) && !transformer.isUnusedParameter(desc))
                currentMethod.lastLambda().setParameterInfo(name, getType(desc));
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        for (Entry<Integer, Integer> entry : localsToNumberOfWrites.entrySet())
            if (entry.getValue() > 1)
                currentMethod.makeLocalMutableFromLambda(entry.getKey());
    }

    public void visitIincInsn(int var, int increment) {
        if (inLambda()) {
            currentLambda.accessLocal(var);
            currentMethod.makeLocalMutableFromLambda(var);
        }
        increseNumberOfWritesFor(var);
    }

    public void visitVarInsn(int opcode, int operand) {
        if (inLambda())
            currentLambda.accessLocal(operand);

        if (inLambda() && transformer.isStoreInstruction(opcode))
            currentMethod.makeLocalMutableFromLambda(operand);
        if (transformer.isStoreInstruction(opcode))
            increseNumberOfWritesFor(operand);
    }

    void increseNumberOfWritesFor(int local) {
        if (!localsToNumberOfWrites.containsKey(local))
            localsToNumberOfWrites.put(local, 0);
        localsToNumberOfWrites.put(local, localsToNumberOfWrites.get(local) + 1);
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        currentMethod.setInfoForLocal(index, name, getType(desc));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        try {
            if (inLambda() && opcode == INVOKESTATIC) {
                if (transformer.isNewLambdaMethod(owner, name, desc)) {
                    currentLambda.setNewLambdaParameterTypes(getArgumentTypes(desc));

                    Type returnType = getReturnType(desc);
                    
                    if (returnType.equals(getType(Object.class))) {
                        resolvingTypeUsingCheckCast = true;

                    } else {
                        currentLambda.setType(returnType);
                        setLambdaMethod();

                        currentLambda = null;
                    }
                }
            }
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    void setLambdaMethod() {
        String descriptor = getMethodDescriptor(getType(Object.class), currentLambda.getParameterTypes());
        MethodInfo currentLambdaMethod = transformer.findMethodByParameterTypes(currentLambda.getType().getInternalName(),
                descriptor);
        currentLambda.setLambdaMethod(currentLambdaMethod);
    }

    public void visitTypeInsn(int opcode, String type) {
        if (CHECKCAST == opcode && resolvingTypeUsingCheckCast) {
            currentLambda.setType(getObjectType(type));
            setLambdaMethod();

            currentLambda = null;
            resolvingTypeUsingCheckCast = false;
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