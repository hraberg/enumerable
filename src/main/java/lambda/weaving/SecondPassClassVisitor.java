package lambda.weaving;

import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static lambda.weaving.MethodInfo.*;
import static org.objectweb.asm.Type.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        MethodInfo currentLambdaMethod;

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

                    debugLambdaStart();

                    createLambdaClass();
                    createLambdaConstructor();

                    createLambdaMethodAndRedirectMethodVisitorToIt();
                }

                if (transformer.isLambdaParameterField(owner, name)) {
                    if (transformer.isUnusedParameter(desc))
                        return;

                    ensureLambdaHasParameter(name);

                    if (currentLambda.isParameterDefined(name)) {
                        ensureAllParametersAreDefined(name);

                        accessLambdaParameter(name, opcode == PUTSTATIC);

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

        void debugLambdaStart() {
            String locals = "";
            if (!currentLambda.accessedLocals.isEmpty())
                locals = " closing over " + method.getAccessedParametersAndLocalsString(currentLambda.accessedLocals);

            debug("starting lambda" + currentLambda.getParametersString() + locals + " at " + sourceAndLine());
            debugIndent();
        }

        void ensureAllParametersAreDefined(String name) {
            if (!currentLambda.allParametersAreDefined())
                throw new IllegalArgumentException("All parameters " + currentLambda.getParameters()
                        + " have to be defined before accessing " + name + " "
                        + sourceAndLine());
        }

        void ensureLambdaHasParameter(String name) {
            if (!currentLambda.hasParameter(name))
                throw new IllegalArgumentException("Tried to access a undefined parameter " + name + " valid ones are "
                        + currentLambda.getParameters() + " " + sourceAndLine());
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            try {
                if (inLambda() && transformer.isNewLambdaMethod(owner, name, desc)) {
                    returnFromLambdaMethod();
                    endLambdaClass();

                    restoreOriginalMethodWriterAndInstantiateTheLambda();

                    debugDedent();
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc);
                }
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
            if (!method.isLocalAccessedFromLambda(operand)) {
                super.visitIntInsn(opcode, operand);
                return;
            }

            Type type = method.getTypeOfLocal(operand);
            boolean readOnly = method.isLocalReadOnly(operand);
            debugLocalVariableAccess(opcode, operand, type, readOnly);

            if (readOnly) {
                if (inLambda())
                    loadLambdaField(operand, type);
                else
                    super.visitIntInsn(opcode, operand);
            } else {
                if (transformer.isStoreInstruction(opcode))
                    storeTopOfStackInArray(operand, type);
                else
                    loadFirstElementOfArray(operand, type);
            }
        }

        void debugLocalVariableAccess(int opcode, int local, Type type, boolean readOnly) {
            debug("variable " + method.getNameOfLocal(local) + " "
                    + getSimpleClassName(type)
                    + (transformer.isStoreInstruction(opcode) ? (readOnly ? " initalized in" : " stored in") : " read from")
                    + (readOnly ? " final" : " wrapped array in")
                    + (inLambda() ? " lambda field " + currentLambda.getFieldNameForLocal(local)
                    : (method.getAccessedParameters().contains(local) ? " method parameter " : " local ") + local));
        }

        public void visitCode() {
            super.visitCode();
            initAccessedLocalsAndMethodParametersAsArrays();
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

        boolean isMethodParameter(int local) {
            return local <= getArgumentTypes(method.desc).length;
        }

        void initAccessedLocalsAndMethodParametersAsArrays() {
            for (int local : method.getAccessedLocals())
                if (!method.isLocalReadOnly(local))
                    initArray(local, method.getTypeOfLocal(local));
        }

        void initArray(int local, Type type) {
            mv.visitInsn(ICONST_1);
            newArray(type);

            if (isMethodParameter(local))
                initializeArrayWithCurrentValueOfLocal(local, type);

            mv.visitVarInsn(ASTORE, local);
        }

        void initializeArrayWithCurrentValueOfLocal(int local, Type type) {
            // a[]
            mv.visitInsn(DUP);
            // a[] a[]
            mv.visitInsn(ICONST_0);
            // a[] a[] 0
            mv.visitVarInsn(type.getOpcode(ILOAD), local);
            // a[] a[] 0 x
            mv.visitInsn(type.getOpcode(IASTORE));
            // a[]
        }

        void newArray(Type type) {
            int arrayType;
            switch (type.getSort()) {
            case Type.BOOLEAN:
                arrayType = Opcodes.T_BOOLEAN;
                break;
            case Type.CHAR:
                arrayType = Opcodes.T_CHAR;
                break;
            case Type.BYTE:
                arrayType = Opcodes.T_BYTE;
                break;
            case Type.SHORT:
                arrayType = Opcodes.T_SHORT;
                break;
            case Type.INT:
                arrayType = Opcodes.T_INT;
                break;
            case Type.FLOAT:
                arrayType = Opcodes.T_FLOAT;
                break;
            case Type.LONG:
                arrayType = Opcodes.T_LONG;
                break;
            case Type.DOUBLE:
                arrayType = Opcodes.T_DOUBLE;
                break;
            default:
                mv.visitTypeInsn(Opcodes.ANEWARRAY, type.getInternalName());
                return;
            }
            mv.visitIntInsn(Opcodes.NEWARRAY, arrayType);
        }

        void loadLambdaField(int local, Type type) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, currentLambdaClass(), currentLambda.getFieldNameForLocal(local),
                    type.getDescriptor());
        }

        void loadArrayFromLocalOrLambdaField(int local, Type type) {
            if (inLambda())
                loadLambdaField(local, toArrayType(type));
            else
                mv.visitVarInsn(ALOAD, local);
        }

        void loadFirstElementOfArray(int local, Type type) {
            loadArrayFromLocalOrLambdaField(local, type);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(type.getOpcode(IALOAD));
        }

        void storeTopOfStackInArray(int local, Type type) {
            // x is at the top of the stack, as it was to be stored
            // directly into a local
            loadArrayFromLocalOrLambdaField(local, type);
            // x a[]
            mv.visitInsn(ICONST_0);
            // x a[] 0
            mv.visitInsn(type.getSize() == 2 ? DUP2_X2 : DUP2_X1);
            // a[] 0 x a[] 0
            mv.visitInsn(POP2);
            // a[] 0 x
            mv.visitInsn(type.getOpcode(IASTORE));
        }

        void incrementInArray(int local, int increment) {
            loadArrayFromLocalOrLambdaField(local, method.getTypeOfLocal(local));
            // a[]
            mv.visitInsn(ICONST_0);
            // a[] 0
            mv.visitInsn(DUP2);
            // a[] 0 a[] 0
            mv.visitInsn(IALOAD);
            // a[] 0 a
            loadInt(increment);
            // a[] 0 a i
            mv.visitInsn(IADD);
            // a[] 0 a+i
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

        void accessLambdaParameter(String name, boolean store) {
            Type type = currentLambda.getParameterType(name);
            int index = currentLambda.getParameterIndex(name);
            debugLambdaParameterAccess(name, store, type);

            if (store) {
                mv.visitVarInsn(ASTORE, index);
            } else {
                mv.visitVarInsn(ALOAD, index);
                mv.visitTypeInsn(CHECKCAST, type.getInternalName());
            }
        }

        void debugLambdaParameterAccess(String name, boolean store, Type type) {
            debug("parameter " + name + " " + getSimpleClassName(type) + (store ? " assigned" : " read"));
        }

        void createLambdaClass() {
            lambdaWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            lambdaWriter.visit(V1_5, ACC_PUBLIC | ACC_FINAL, currentLambdaClass(), null, getLambdaSuperType().getInternalName(),
                    getLambdaInterfaces());
            lambdaWriter.visitOuterClass(className, method.name, method.desc);
            lambdaWriter.visitInnerClass(currentLambdaClass(), null, null, 0);
        }

        String[] getLambdaInterfaces() {
            Type type = currentLambda.getType();
            if (transformer.isInterface(type.getInternalName()))
                return new String[] { type.getInternalName() };
            return null;
        }

        void createLambdaMethodAndRedirectMethodVisitorToIt() {
            String descriptor = getMethodDescriptor(getType(Object.class), currentLambda.getParameterTypes());
            currentLambdaMethod = transformer.findMethodByParameterTypes(currentLambda.getType().getInternalName(), descriptor);

            mv = lambdaWriter.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, currentLambdaMethod.name, currentLambdaMethod.desc, null, null);
            mv.visitCode();

            boxPrimitiveArguments();
        }

        void boxPrimitiveArguments() {
            Type[] argumentTypes = getArgumentTypes(currentLambdaMethod.desc);
            for (int i = 0; i < argumentTypes.length; i++) {
                Type type = argumentTypes[i];
                if (!isReferenceType(type))
                    boxLocal(i + 1, type);
            }
        }

        void returnFromLambdaMethod() {
            Type returnType = getReturnType(currentLambdaMethod.desc);
            unbox(returnType);
            mv.visitInsn(returnType.getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        void valueOf(Type type) {
            if (isReferenceType(type))
                return;
            Type boxed = getBoxedType(type);
            String descriptor = getMethodDescriptor(boxed, new Type[] { type });
            String name = "valueOf";
            mv.visitMethodInsn(INVOKESTATIC, boxed.getInternalName(), name, descriptor);
        }

        boolean isReferenceType(Type type) {
            return type.getSort() == OBJECT || type.getSort() == ARRAY;
        }

        void boxLocal(int local, Type type) {
            mv.visitVarInsn(type.getOpcode(ILOAD), local);
            valueOf(type);
            mv.visitVarInsn(ASTORE, local);
        }

        Type getBoxedType(Type type) {
            switch (type.getSort()) {
            case Type.BOOLEAN:
                return getType(Boolean.class);
            case Type.CHAR:
                return getType(Character.class);
            case Type.BYTE:
                return getType(Byte.class);
            case Type.SHORT:
                return getType(Short.class);
            case Type.INT:
                return getType(Integer.class);
            case Type.FLOAT:
                return getType(Float.class);
            case Type.LONG:
                return getType(Long.class);
            case Type.DOUBLE:
                return getType(Double.class);
            }
            return type;
        }

        void unbox(Type returnType) {
            Type type = getType(Number.class);
            switch (returnType.getSort()) {
            case VOID:
            case ARRAY:
            case OBJECT:
                return;
            case CHAR:
                type = getType(Character.class);
                break;
            case BOOLEAN:
                type = getType(Boolean.class);
                break;
            }
            String descriptor = getMethodDescriptor(returnType, new Type[0]);
            String name = returnType.getClassName() + "Value";
            mv.visitMethodInsn(INVOKEVIRTUAL, type.getInternalName(), name, descriptor);
        }

        void createLambdaConstructor() {
            Type[] parameters = getLambdaConstructorParameters();
            String descriptor = getMethodDescriptor(VOID_TYPE, parameters);

            mv = lambdaWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
            mv.visitCode();

            createAndInitializeFieldsWithAccessedLocals(parameters);

            invokeSuperConstructor();

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        void createAndInitializeFieldsWithAccessedLocals(Type[] parameters) {
            Iterator<Integer> locals = currentLambda.accessedLocals.iterator();
            for (int i = 0; locals.hasNext(); i++) {
                String field = currentLambda.getFieldNameForLocal(locals.next());
                Type type = parameters[i];

                lambdaWriter.visitField(ACC_SYNTHETIC | ACC_PRIVATE | ACC_FINAL, field, type.getDescriptor(), null, null).visitEnd();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(type.getOpcode(ILOAD), i + 1);
                mv.visitFieldInsn(PUTFIELD, currentLambdaClass(), field, type.getDescriptor());
            }
        }

        void invokeSuperConstructor() {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, getLambdaSuperType().getInternalName(), "<init>", "()V");
        }

        Type getLambdaSuperType() {
            Type type = currentLambda.getType();
            if (transformer.isInterface(type.getInternalName()))
                type = getType(Object.class);
            return type;
        }

        void restoreOriginalMethodWriterAndInstantiateTheLambda() {
            mv = originalMethodWriter;
            mv.visitTypeInsn(NEW, currentLambdaClass());
            mv.visitInsn(DUP);

            loadAccessedLocals();
            String descriptor = getMethodDescriptor(VOID_TYPE, getLambdaConstructorParameters());
            mv.visitMethodInsn(INVOKESPECIAL, currentLambdaClass(), "<init>", descriptor);
        }

        void loadAccessedLocals() {
            for (int local : currentLambda.accessedLocals) {
                Type type = method.getTypeOfLocal(local);
                if (!method.isLocalReadOnly(local))
                    type = toArrayType(type);
                mv.visitVarInsn(type.getOpcode(ILOAD), local);
            }
        }

        void endLambdaClass() {
            lambdaWriter.visitEnd();
            cv.visitInnerClass(currentLambdaClass(), null, null, 0);

            byte[] bs = lambdaWriter.toByteArray();
            transformer.newLambdaClass(getObjectType(currentLambdaClass()).getClassName(), bs);

            lambdaWriter = null;
        }

        String sourceAndLine() {
            return source != null ? "(" + source + ":" + currentLine + ")" : "(Unknown Source)";
        }

        void nextLambdaId() {
            currentLambdaId++;
        }

        Type[] getLambdaConstructorParameters() {
            List<Type> result = new ArrayList<Type>();
            for (int local : currentLambda.accessedLocals) {
                Type type = method.getTypeOfLocal(local);
                result.add(method.isLocalReadOnly(local) ? type : toArrayType(type));
            }
            return result.toArray(new Type[0]);
        }

        Type toArrayType(Type type) {
            return getType("[" + type.getDescriptor());
        }

        String currentLambdaClass() {
            String lambdaClass = currentLambda.getType().getInternalName();
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