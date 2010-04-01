package lambda.weaving;

import static lambda.weaving.Debug.*;
import static org.objectweb.asm.Type.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    Map<String, byte[]> lambdasByResourceName = new HashMap<String, byte[]>();

    String source;
    String className;

    Map<String, MethodInfo> methodsByNameAndDesc;

    int currentLambdaId;

    private final LambdaTransformer transformer;

    class LambdaMethodVisitor extends MethodAdapter {
        static final String LAMBDA_CLASS_PREFIX = "Fn";

        MethodVisitor originalMethodWriter;

        ClassWriter lambdaWriter;
        Map<String, Integer> parameterNamesToIndex;

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
                    parameterNamesToIndex = new LinkedHashMap<String, Integer>();
                    nextLambdaId();

                    String locals = "";
                    if (!currentLambda.accessedLocals.isEmpty()) {
                        locals = " closing over [" + method.getAccessedLocalsString(currentLambda.accessedLocals) + "]";
                    }
                    debug("starting new lambda with arity " + currentLambda.arity + locals);

                    debugIndent();

                    createLambdaClass();
                    createLambdaConstructor();

                    createCallMethodAndRedirectMethodVisitorToIt();
                }
                if (transformer.isLambdaParameterField(owner, name)) {
                    if (!parameterNamesToIndex.containsKey(name)) {
                        initLambdaParameter(name);
                    } else {
                        int index = parameterNamesToIndex.get(name);
                        debug("lambda parameter " + getObjectType(owner).getClassName() + "." + name + " "
                                + getType(desc).getClassName() + " accessed as argument "
                                + index);
                        accessLambdaParameter(name, desc, index);
                    }
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            try {
                if (inLambda() && opcode == INVOKESTATIC) {
                    if (transformer.isNewLambdaMethod(owner, name, desc)) {
                        debug("new lambda created at " + sourceAndLine());

                        returnFromCall();
                        endLambdaClass();

                        restoreOriginalMethodWriterAndInstantiateTheLambda();
                        debugDedent();
                        return;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void visitIincInsn(int var, int increment) {
            if (method.isLocalAccessedFromLambda(var)) {
                incrementInArray(var, increment);
            } else {
                super.visitIincInsn(var, increment);
            }
        }

        public void visitVarInsn(int opcode, int operand) {
            if (method.isLocalAccessedFromLambda(operand)) {
                Type type = method.getTypeOfLocal(operand);
                if (isThis(operand)) {
                    if (inLambda()) {
                        mv.visitVarInsn(ALOAD, operand);
                        mv.visitFieldInsn(GETFIELD, currentLambdaClass(), lambdaFieldNameForLocal(operand), getDescriptor(Object.class));
                        mv.visitTypeInsn(CHECKCAST, type.getInternalName());
                    } else {
                        super.visitIntInsn(opcode, operand);
                    }
                } else {
                    loadArrayFromLocalOrLambda(operand, type);
                    accessFirstArrayElement(opcode, type);

                    debug("variable "
                            + method.getNameOfLocal(operand)
                            + " "
                            + type.getClassName()
                            + " accessed using wrapped array"
                            + (inLambda() ? " field " + getObjectType(currentLambdaClass()).getClassName() + "."
                                    + lambdaFieldNameForLocal(operand) : " local "
                            + operand));
                }
            } else {
                super.visitIntInsn(opcode, operand);
            }
        }

        public void visitCode() {
            super.visitCode();
            initAccessedLocalsAndParametersAsArrays();
        }

        public void visitLineNumber(int line, Label start) {
            currentLine = line;
            super.visitLineNumber(line, start);
        }

        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            if (method.isLocalAccessedFromLambda(index)) {
                desc = getDescriptor(Object.class);
            }
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
            if (!accessedLocals.isEmpty()) {
                debug("wrapping locals accessed by lambdas in arrays " + method.getAccessedArgumentsAndLocalsString(accessedLocals));
            } else {
                debug("no locals accessed by lambdas");
            }
            for (int local : method.getAccessedLocals()) {
                if (!isThis(local)) {
                    initArray(local, method.getTypeOfLocal(local));
                }
            }
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

        void loadArrayFromLocalOrLambda(int operand, Type type) {
            if (inLambda()) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, currentLambdaClass(), lambdaFieldNameForLocal(operand), getDescriptor(Object.class));
                mv.visitTypeInsn(CHECKCAST, "[" + type.getDescriptor());
            } else {
                mv.visitVarInsn(ALOAD, operand);
            }
        }

        void accessFirstArrayElement(int opcode, Type type) {
            if (opcode >= ISTORE && opcode <= ASTORE) {
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
            loadArrayFromLocalOrLambda(var, method.getTypeOfLocal(var));
            mv.visitInsn(ICONST_0);
            mv.visitInsn(DUP2);
            mv.visitInsn(IALOAD);
            if (increment >= Byte.MIN_VALUE && increment <= Byte.MAX_VALUE)
                mv.visitIntInsn(Opcodes.BIPUSH, increment);
            else if (increment >= Short.MIN_VALUE && increment <= Short.MAX_VALUE)
                mv.visitIntInsn(Opcodes.SIPUSH, increment);
            else
                mv.visitLdcInsn(increment);
            mv.visitInsn(IADD);
            mv.visitInsn(IASTORE);
        }

        boolean notAConstructor(String name) {
            return !name.startsWith("<");
        }

        void createLambdaClass() {

            lambdaWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            String lambdaInterface = "lambda/" + LAMBDA_CLASS_PREFIX + currentLambda.arity;
            lambdaWriter.visit(V1_5, ACC_PUBLIC, currentLambdaClass(), null, getInternalName(Object.class),
                    new String[] { lambdaInterface });
            lambdaWriter.visitOuterClass(className, method.name, method.desc);
            lambdaWriter.visitInnerClass(currentLambdaClass(), null, null, 0);
        }

        void createCallMethodAndRedirectMethodVisitorToIt() {
            String arguments = "";
            for (int i = 0; i < currentLambda.arity; i++)
                arguments += getDescriptor(Object.class);
            mv = lambdaWriter.visitMethod(ACC_PUBLIC, "call", "(" + arguments + ")" + getDescriptor(Object.class), null, null);
            mv.visitCode();
        }

        void createLambdaConstructor() {
            String parameters = "";
            for (int local : currentLambda.accessedLocals) {
                parameters += getDescriptor(Object.class);
                lambdaWriter.visitField(ACC_FINAL + ACC_SYNTHETIC, lambdaFieldNameForLocal(local), getDescriptor(Object.class), null,
                        null).visitEnd();
            }

            mv = lambdaWriter.visitMethod(ACC_PUBLIC, "<init>", "(" + parameters + ")V", null, null);
            mv.visitCode();
            int i = 1;
            for (int local : currentLambda.accessedLocals) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, i++);
                mv.visitFieldInsn(PUTFIELD, currentLambdaClass(), lambdaFieldNameForLocal(local), getDescriptor(Object.class));
            }

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        String lambdaFieldNameForLocal(int local) {
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

            String parameters = "";
            for (int local : currentLambda.accessedLocals) {
                parameters += getDescriptor(Object.class);
                mv.visitVarInsn(ALOAD, local);
            }
            mv.visitMethodInsn(INVOKESPECIAL, currentLambdaClass(), "<init>", "(" + parameters + ")V");
        }

        void endLambdaClass() {
            lambdaWriter.visitEnd();
            cv.visitInnerClass(currentLambdaClass(), null, null, 0);

            byte[] bs = lambdaWriter.toByteArray();
            transformer.newLambdaClass(currentLambdaClass(), bs);

            lambdaWriter = null;
        }

        void initLambdaParameter(String name) {
            if (parameterNamesToIndex.size() == currentLambda.arity) {
                throw new IllegalArgumentException("Tried to access a unbound parameter [" + name + "] valid ones are "
                        + parameterNamesToIndex.keySet() + " " + sourceAndLine());
            }
            parameterNamesToIndex.put(name, parameterNamesToIndex.size() + 1);
        }

        void accessLambdaParameter(String name, String desc, int parameter) {
            if (parameterNamesToIndex.size() != currentLambda.arity) {
                throw new IllegalArgumentException("Parameter already bound [" + name + "] " + sourceAndLine());
            }
            mv.visitVarInsn(ALOAD, parameter);
            mv.visitTypeInsn(CHECKCAST, getType(desc).getInternalName());
        }

        String sourceAndLine() {
            return source != null ? "(" + source + ":" + currentLine + ")" : "";
        }

        void nextLambdaId() {
            currentLambdaId++;
        }

        String currentLambdaClass() {
            return className + "$" + LAMBDA_CLASS_PREFIX + currentLambda.arity + "_" + currentLambdaId;
        }

        boolean inLambda() {
            return lambdaWriter != null;
        }
    }

    SecondPassClassVisitor(ClassVisitor cv, FirstPassClassVisitor firstPass, LambdaTransformer transformer) {
        super(cv);
        this.transformer = transformer;
        this.methodsByNameAndDesc = firstPass.methodsByName;
    }

    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        this.source = source;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        debug("transforming lambdas and accessed locals in " + getObjectType(name).getClassName());
    }

    public MethodVisitor visitMethod(int access, final String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        MethodInfo method = methodsByNameAndDesc.get(name + desc);
        if (method.lambdas.isEmpty()) {
            debug("skipping method " + name + desc);
            return mv;
        }
        debug("processing method " + method.getNameAndDesc());
        return new LambdaMethodVisitor(mv, method);
    }
}