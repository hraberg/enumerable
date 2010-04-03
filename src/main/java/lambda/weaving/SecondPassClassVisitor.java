package lambda.weaving;

import static java.util.Arrays.*;
import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static org.objectweb.asm.Type.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lambda.weaving.MethodInfo.LambdaInfo;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class SecondPassClassVisitor extends ClassAdapter implements Opcodes {
    String source;
    String className;

    LambdaTransformer transformer;
    Map<String, MethodInfo> methodsByNameAndDesc;

    int currentLambdaId;

    class LambdaMethodVisitor extends MethodAdapter {
        MethodVisitor originalMethodWriter;
        ClassWriter lambdaWriter;

        int currentLine;

        MethodInfo method;
        Iterator<LambdaInfo> lambdas;
        LambdaInfo currentLambda;

        LambdaMethodVisitor(MethodVisitor mv, MethodInfo method) {
            super(mv);
            this.method = method;
            this.lambdas = method.lambdas();
            this.originalMethodWriter = mv;
            debugIndent();
        }

        public void visitEnd() {
            debugDedent();
            super.visitEnd();
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            try {
                if (!inLambda() && transformer.isLambdaParameterField(owner, name)) {
                    currentLambda = lambdas.next();
                    nextLambdaId();

                    String locals = "";
                    if (!currentLambda.accessedLocals.isEmpty())
                        locals = " closing over " + method.getLocalNames(currentLambda.accessedLocals);

                    debug("starting lambda" + currentLambda.getParametersString() + locals + " at " + sourceAndLine());
                    debugIndent();

                    createLambdaClass();
                    createLambdaConstructor();

                    createCallMethodAndRedirectMethodVisitorToIt();
                }
                if (transformer.isLambdaParameterField(owner, name)) {
                    if (!currentLambda.hasParameter(name))
                        throw new IllegalArgumentException("Tried to access a undefined parameter " + name + " valid ones are "
                                + currentLambda.parameters + " " + sourceAndLine());

                    if (currentLambda.isParameterDefined(name)) {
                        if (currentLambda.allParametersAreDefined())
                            accessLambdaParameter(opcode == PUTSTATIC, name, desc);
                        else
                            throw new IllegalArgumentException("All parameters " + currentLambda.parameters
                                    + " have to be defined before accessing " + name + " "
                                    + sourceAndLine());
                    } else {
                        currentLambda.defineParameter(name);
                    }
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            try {
                if (inLambda() && opcode == INVOKESTATIC) {
                    if (transformer.isNewLambdaMethod(owner, name, desc)) {
                        returnFromCall();
                        endLambdaClass();

                        restoreOriginalMethodWriterAndInstantiateTheLambda();

                        debugDedent();
                        return;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        public void visitIincInsn(int var, int increment) {
            if (method.isLocalAccessedFromLambda(var))
                incrementInArray(var, increment);
            else
                super.visitIincInsn(var, increment);
        }

        public void visitVarInsn(int opcode, int operand) {
            if (method.isLocalAccessedFromLambda(operand)) {
                Type type = method.getTypeOfLocal(operand);
                if (isThis(operand)) {
                    if (inLambda())
                        loadLambdaField(operand, type);
                    else
                        super.visitIntInsn(opcode, operand);
                } else {
                    debug("variable " + method.getNameOfLocal(operand) + " "
                            + MethodInfo.getSimpleClassName(type)
                            + (isStoreInstruction(opcode) ? " stored in" : " read from")
                            + (inLambda() ? " lambda field " + getLambdaFieldNameForLocal(operand)
                            : " local " + operand));

                    loadArrayFromLocalOrLambdaField(operand, type);
                    accessFirstArrayElement(opcode, type, isStoreInstruction(opcode));
                }
            } else {
                super.visitIntInsn(opcode, operand);
            }
        }

        boolean isStoreInstruction(int opcode) {
            return opcode >= ISTORE && opcode <= ASTORE;
        }

        public void visitCode() {
            super.visitCode();
            initAccessedLocalsAndParametersAsArrays();
        }

        public void visitLineNumber(int line, Label start) {
            super.visitLineNumber(line, start);
            currentLine = line;
        }

        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            if (method.isLocalAccessedFromLambda(index))
                desc = getDescriptor(Object.class);
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }

        boolean isMethodParameter(int operand) {
            return operand <= getArgumentTypes(method.desc).length;
        }

        boolean isThis(int operand) {
            return operand == 0;
        }

        void initAccessedLocalsAndParametersAsArrays() {
            Set<Integer> accessedLocals = method.getAccessedLocals();
            if (accessedLocals.isEmpty())
                debug("no locals accessed by lambdas");
            else
                debug("wrapping locals accessed by lambdas in arrays " + method.getAccessedParametersAndLocalsString(accessedLocals));
            for (int local : accessedLocals)
                if (!isThis(local))
                    initArray(local, method.getTypeOfLocal(local));
        }

        void initArray(int operand, Type type) {
            mv.visitInsn(ICONST_1);
            newArray(type);

            if (isMethodParameter(operand)) {
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(type.getOpcode(ILOAD), operand);
                mv.visitInsn(type.getOpcode(IASTORE));
            }

            mv.visitVarInsn(ASTORE, operand);
        }

        void newArray(Type type) {
            int typ;
            switch (type.getSort()) {
            case Type.BOOLEAN:
                typ = Opcodes.T_BOOLEAN;
                break;
            case Type.CHAR:
                typ = Opcodes.T_CHAR;
                break;
            case Type.BYTE:
                typ = Opcodes.T_BYTE;
                break;
            case Type.SHORT:
                typ = Opcodes.T_SHORT;
                break;
            case Type.INT:
                typ = Opcodes.T_INT;
                break;
            case Type.FLOAT:
                typ = Opcodes.T_FLOAT;
                break;
            case Type.LONG:
                typ = Opcodes.T_LONG;
                break;
            case Type.DOUBLE:
                typ = Opcodes.T_DOUBLE;
                break;
            default:
                mv.visitTypeInsn(Opcodes.ANEWARRAY, type.getInternalName());
                return;
            }
            mv.visitIntInsn(Opcodes.NEWARRAY, typ);
        }

        void loadLambdaField(int operand, Type type) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, currentLambdaClass(), getLambdaFieldNameForLocal(operand),
                    getDescriptor(Object.class));
            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
        }

        void loadArrayFromLocalOrLambdaField(int operand, Type type) {
            if (inLambda())
                loadLambdaField(operand, toArrayType(type));
            else
                mv.visitVarInsn(ALOAD, operand);
        }

        void accessFirstArrayElement(int opcode, Type type, boolean store) {
            if (store) {
                mv.visitInsn(SWAP);
                mv.visitInsn(ICONST_0);
                mv.visitInsn(SWAP);
                mv.visitInsn(type.getOpcode(IASTORE));
            } else {
                mv.visitInsn(ICONST_0);
                mv.visitInsn(type.getOpcode(IALOAD));
            }
        }

        void incrementInArray(int var, int increment) {
            loadArrayFromLocalOrLambdaField(var, method.getTypeOfLocal(var));
            mv.visitInsn(ICONST_0);
            mv.visitInsn(DUP2);
            mv.visitInsn(IALOAD);
            loadInt(increment);
            mv.visitInsn(IADD);
            mv.visitInsn(IASTORE);
        }

        void loadInt(int value) {
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
                mv.visitIntInsn(Opcodes.BIPUSH, value);
            else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
                mv.visitIntInsn(Opcodes.SIPUSH, value);
            else
                mv.visitLdcInsn(value);
        }

        void accessLambdaParameter(boolean write, String name, String desc) {
            debug("parameter " + name + " " + MethodInfo.getSimpleClassName(getType(desc)) + (write ? " assigned" : " read"));
            if (write) {
                mv.visitTypeInsn(CHECKCAST, getType(desc).getInternalName());
                mv.visitVarInsn(ASTORE, currentLambda.getParameterIndex(name));
            } else {
                mv.visitVarInsn(ALOAD, currentLambda.getParameterIndex(name));
                mv.visitTypeInsn(CHECKCAST, getType(desc).getInternalName());
            }
        }

        void createLambdaClass() {
            lambdaWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            lambdaWriter.visit(V1_5, ACC_PUBLIC, currentLambdaClass(), null, currentLambda.getInternalName(),
                    null);
            lambdaWriter.visitOuterClass(className, method.name, method.desc);
            lambdaWriter.visitInnerClass(currentLambdaClass(), null, null, 0);
        }

        void createCallMethodAndRedirectMethodVisitorToIt() {
            String descriptor = getMethodDescriptor(getType(Object.class), getTypeArrayOfObjects(currentLambda.getArity()));
            mv = lambdaWriter.visitMethod(ACC_PUBLIC, "call", descriptor, null, null);
            mv.visitCode();
        }

        void createLambdaConstructor() {
            String descriptor = getMethodDescriptor(Type.VOID_TYPE, getTypeArrayOfObjects(currentLambda.accessedLocals.size()));
            mv = lambdaWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
            mv.visitCode();

            int i = 1;
            for (int local : currentLambda.accessedLocals) {
                String field = getLambdaFieldNameForLocal(local);
                lambdaWriter.visitField(ACC_FINAL + ACC_SYNTHETIC, field, getDescriptor(Object.class), null,
                        null).visitEnd();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, i++);
                mv.visitFieldInsn(PUTFIELD, currentLambdaClass(), field, getDescriptor(Object.class));
            }

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, currentLambda.getInternalName(), "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        String getLambdaFieldNameForLocal(int local) {
            return isThis(local) ? "this$0" : "val$" + local;
        }

        void returnFromCall() {
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        void restoreOriginalMethodWriterAndInstantiateTheLambda() {
            mv = originalMethodWriter;
            mv.visitTypeInsn(NEW, currentLambdaClass());
            mv.visitInsn(DUP);

            for (int local : currentLambda.accessedLocals)
                mv.visitVarInsn(ALOAD, local);

            String descriptor = getMethodDescriptor(Type.VOID_TYPE, getTypeArrayOfObjects(currentLambda.accessedLocals.size()));
            mv.visitMethodInsn(INVOKESPECIAL, currentLambdaClass(), "<init>", descriptor);
        }

        void endLambdaClass() {
            lambdaWriter.visitEnd();
            cv.visitInnerClass(currentLambdaClass(), null, null, 0);

            byte[] bs = lambdaWriter.toByteArray();
            transformer.newLambdaClass(getObjectType(currentLambdaClass()).getClassName(), bs);

            lambdaWriter = null;
        }

        String sourceAndLine() {
            return source != null ? "(" + source + ":" + currentLine + ")" : "";
        }

        void nextLambdaId() {
            currentLambdaId++;
        }

        Type toArrayType(Type type) {
            return getType("[" + type.getDescriptor());
        }

        Type[] getTypeArrayOfObjects(int length) {
            Type[] arguments = new Type[length];
            fill(arguments, getType(Object.class));
            return arguments;
        }

        String currentLambdaClass() {
            String lambdaClass = currentLambda.getInternalName();
            lambdaClass = lambdaClass.substring(lambdaClass.lastIndexOf("/") + 1, lambdaClass.length());
            return className + "$" + lambdaClass + "_" + currentLambdaId;
        }

        boolean inLambda() {
            return lambdaWriter != null;
        }
    }

    SecondPassClassVisitor(ClassVisitor cv, FirstPassClassVisitor firstPass, LambdaTransformer transformer) {
        super(cv);
        this.transformer = transformer;
        this.methodsByNameAndDesc = firstPass.methodsByNameAndDesc;
    }

    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        this.source = source;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        debug("transforming " + getObjectType(name).getClassName());
    }

    public MethodVisitor visitMethod(int access, final String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        MethodInfo method = methodsByNameAndDesc.get(name + desc);

        if (method.lambdas.isEmpty()) {
            debug("skipping " + method);
            return mv;
        }
        debug("processing " + method);
        return new LambdaMethodVisitor(mv, method);
    }
}