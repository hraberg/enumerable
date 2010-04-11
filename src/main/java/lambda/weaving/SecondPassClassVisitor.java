package lambda.weaving;

import java.util.*;

import lambda.weaving.MethodInfo.LambdaInfo;

import org.objectweb.asm.*;

import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static lambda.weaving.MethodInfo.*;
import static org.objectweb.asm.Type.*;

class SecondPassClassVisitor extends ClassAdapter implements Opcodes {
    String source;
    String className;

    LambdaTransformer transformer;
    Map<String, MethodInfo> methodsByNameAndDesc;

    int currentLambdaId;
    int accessMethods;

    class LambdaMethodVisitor extends MethodAdapter {
        MethodVisitor originalMethodWriter;
        ClassWriter lambdaWriter;

        int currentLine = -1;

        MethodInfo method;
        Iterator<LambdaInfo> lambdas;
        LambdaInfo currentLambda;
        MethodInfo currentLambdaMethod;
        boolean inLambdaDefinition;

        Type lambdaParameterTypeDefinitionToIgnoreValueOfCallOn;
        int lambdaParameterDefinitionPrimitiveConversionToIgnore = -1;

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
                    inLambdaDefinition = true;
                    debugLambdaStart();
                }

                if (transformer.isLambdaParameterField(owner, name)) {
                    if (transformer.isUnusedParameter(desc)) {
                        if (currentLambda.allParametersAreDefined())
                            createLambda();
                        return;
                    }

                    ensureLambdaHasParameter(name);

                    if (currentLambda.isParameterDefined(name)) {
                        ensureAllLambdaParametersAreDefined(name);

                        accessLambdaParameter(name, opcode == PUTSTATIC);

                    } else {
                        currentLambda.defineParameter(name);

                        skipValidImplictCastBetweenTypeOfMethodArgumentAndLambdaParameterAtDefinition(name);

                        if (currentLambda.allParametersAreDefined())
                            createLambda();
                    }
                    
                } else if (isPrivateFieldOnThisWhichNeedsAcccessMethodFromLambda(owner, name)) {
                    createAndCallStaticAccessMethodToReplacePrivateFieldAccess(opcode, owner, name, desc);

                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        void createLambda() {
            inLambdaDefinition = false;

            createLambdaClass();
            createLambdaConstructor();

            createLambdaMethodAndRedirectMethodVisitorToIt();
        }

        void skipValidImplictCastBetweenTypeOfMethodArgumentAndLambdaParameterAtDefinition(String name) {
            Type lambdaParameterType = currentLambda.getParameterType(name);

            if (isPrimitive(lambdaParameterType) && isReference(currentLambda.getNewMethodParameterType(name)))
                lambdaParameterTypeDefinitionToIgnoreValueOfCallOn = getBoxedType(lambdaParameterType);

            Type[] methodParameterTypes = getArgumentTypes(currentLambda.getLambdaMethod().desc);
            Type methodParameterType = methodParameterTypes[currentLambda.getParameterIndex(name)];

            if (!methodParameterType.equals(lambdaParameterType)) {
                if (isPrimitive(lambdaParameterType) && isPrimitive(methodParameterType))
                    skipImplicitWideningPrimitiveConversionAtLambdaParameterDefinition(methodParameterType,
                            lambdaParameterType);

                else if (isPrimitive(lambdaParameterType) && isReference(methodParameterType))
                    lambdaParameterTypeDefinitionToIgnoreValueOfCallOn = getBoxedType(lambdaParameterType);
            }

            debugLambdaParameterDefinitionSkippedConversions(name, methodParameterType, lambdaParameterType);
        }

        void debugLambdaParameterDefinitionSkippedConversions(String name, Type methodParameterType,
                Type lambdaParameterType) {
            if (lambdaParameterTypeDefinitionToIgnoreValueOfCallOn != null)
                debug("parameter " + name + " " + getSimpleClassName(lambdaParameterType) + " is boxed as "
                        + getSimpleClassName(getBoxedType(lambdaParameterType)) + " in lambda method definition");

            if (lambdaParameterDefinitionPrimitiveConversionToIgnore > 0)
                debug("parameter " + name + " " + getSimpleClassName(lambdaParameterType) + " is the wider "
                        + getSimpleClassName(methodParameterType) + " in lambda method definition");
        }

        void skipImplicitWideningPrimitiveConversionAtLambdaParameterDefinition(Type methodParameterType,
                Type lambdaParameterType) {
            if (DOUBLE_TYPE == methodParameterType)
                switch (lambdaParameterType.getSort()) {
                case BYTE:
                case CHAR:
                case SHORT:
                case INT:
                    lambdaParameterDefinitionPrimitiveConversionToIgnore = I2D;
                    break;
                case Type.FLOAT:
                    lambdaParameterDefinitionPrimitiveConversionToIgnore = F2D;
                    break;
                case Type.LONG:
                    lambdaParameterDefinitionPrimitiveConversionToIgnore = L2D;
                    break;
                }
            else if (LONG_TYPE == methodParameterType)
                lambdaParameterDefinitionPrimitiveConversionToIgnore = I2L;

        }

        void debugLambdaStart() {
            String locals = "";
            if (!currentLambda.accessedLocals.isEmpty())
                locals = " closing over "
                        + method.getAccessedParametersAndLocalsString(currentLambda.accessedLocals);

            debug("lambda #" + getSimpleClassName(currentLambda.getExpressionType())
                    + currentLambda.getTypedParametersString() + locals + " as " + currentLambda.getLambdaMethod()
                    + " in " + getSimpleClassName(currentLambda.getType()) + " at " + sourceAndLine());
            debugIndent();
        }

        void ensureAllLambdaParametersAreDefined(String name) {
            if (!currentLambda.allParametersAreDefined())
                throw new IllegalArgumentException("Defining " + name + " more than once for " + currentLambda
                        + " at " + sourceAndLine());
        }

        void ensureLambdaHasParameter(String name) {
            if (!currentLambda.hasParameter(name))
                throw new IllegalArgumentException("Parameter " + name + " is undefined for " + currentLambda
                        + " at " + sourceAndLine());
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            try {
                if (inLambda() && transformer.isNewLambdaMethod(owner, name, desc)) {
                    returnFromLambdaMethod();
                    endLambdaClass();

                    restoreOriginalMethodWriterAndInstantiateTheLambda();

                    debugDedent();

                } else if (isValueOfCallToIgnore(opcode, owner, name)) {
                    lambdaParameterTypeDefinitionToIgnoreValueOfCallOn = null;

                } else if (isInvokeSpecialOnThisWhichNeedsAcccessMethodFromLambda(opcode, owner)) {
                    createAndCallStaticAccessMethodToReplaceInvokeSpecial(opcode, owner, name, desc);

                } else {
                    super.visitMethodInsn(opcode, owner, name, desc);

                }
            } catch (Exception e) {
                throw uncheck(e);
            }
        }

        void createAndCallStaticAccessMethodToReplacePrivateFieldAccess(int opcode, String owner, String name, String desc) {
            List<Type> argumentTypes = new ArrayList<Type>();
            argumentTypes.add(getObjectType(owner));
            Type returnType = getType(desc);

            if (opcode == PUTFIELD)
                argumentTypes.add(getType(desc));

            MethodVisitor mv = createAndCallAccessMethodAndLoadArguments(owner, argumentTypes, returnType);

            mv.visitFieldInsn(opcode, owner, name, desc);
            
            if (opcode == PUTFIELD)
                mv.visitVarInsn(argumentTypes.get(1).getOpcode(ILOAD), 1);

            mv.visitInsn(returnType.getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        void createAndCallStaticAccessMethodToReplaceInvokeSpecial(int opcode, String owner, String name, String desc) {
            List<Type> argumentTypes = new ArrayList<Type>();
            argumentTypes.add(getObjectType(owner));
            for (Type type : getArgumentTypes(desc))
                argumentTypes.add(type);

            MethodVisitor mv = createAndCallAccessMethodAndLoadArguments(owner, argumentTypes, getReturnType(desc));

            mv.visitMethodInsn(opcode, owner, name, desc);

            mv.visitInsn(getReturnType(desc).getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        MethodVisitor createAndCallAccessMethodAndLoadArguments(String owner, List<Type> argumentTypes, Type returnType) {
            String accessMethodDescriptor = getMethodDescriptor(returnType, argumentTypes
                    .toArray(new Type[0]));
            String accessMethodName = "access$lambda$" + String.format("%03d_", accessMethods++);

            mv.visitMethodInsn(INVOKESTATIC, owner, accessMethodName, accessMethodDescriptor);

            MethodVisitor mv = SecondPassClassVisitor.this.cv.visitMethod(ACC_STATIC + ACC_SYNTHETIC,
                    accessMethodName, accessMethodDescriptor, null, null);
            mv.visitCode();

            int i = 0;
            for (Type type : argumentTypes)
                mv.visitVarInsn(type.getOpcode(ILOAD), i++);

            return mv;
        }

        boolean isPrivateFieldOnThisWhichNeedsAcccessMethodFromLambda(String owner, String name) {
            return inLambda() && className.equals(owner) && transformer.isFieldPrivate(owner, name);
        }

        boolean isInvokeSpecialOnThisWhichNeedsAcccessMethodFromLambda(int opcode, String owner) {
            return inLambda() && INVOKESPECIAL == opcode && className.equals(owner);
        }

        boolean isValueOfCallToIgnore(int opcode, String owner, String name) {
            return inLambda() && INVOKESTATIC == opcode && "valueOf".equals(name)
                    && getObjectType(owner).equals(lambdaParameterTypeDefinitionToIgnoreValueOfCallOn);
        }

        public void visitInsn(int opcode) {
            if (opcode == lambdaParameterDefinitionPrimitiveConversionToIgnore)
                lambdaParameterDefinitionPrimitiveConversionToIgnore = -1;
            else
                super.visitInsn(opcode);
        }

        public void visitIincInsn(int var, int increment) {
            if (method.isLocalAccessedFromLambda(var))
                incrementInArray(var, increment);
            else
                super.visitIincInsn(var, increment);
        }

        public void visitVarInsn(int opcode, int operand) {
            if (!method.isLocalAccessedFromLambda(operand)) {
                super.visitVarInsn(opcode, operand);
                return;
            }

            Type type = method.getTypeOfLocal(operand);
            boolean readOnly = method.isLocalReadOnly(operand);
            debugLocalVariableAccess(opcode, operand, type, readOnly);

            if (readOnly) {
                if (inLambda())
                    loadLambdaField(operand, type);
                else
                    super.visitVarInsn(opcode, operand);
            } else {
                if (transformer.isStoreInstruction(opcode))
                    storeTopOfStackInArray(operand, type);
                else
                    loadFirstElementOfArray(operand, type);
            }
        }

        void debugLocalVariableAccess(int opcode, int local, Type type, boolean readOnly) {
            debug("variable "
                    + method.getNameOfLocal(local)
                    + " "
                    + getSimpleClassName(type)
                    + (transformer.isStoreInstruction(opcode) ? (readOnly ? " initalized in" : " stored in")
                            : " read from")
                    + (readOnly ? " final" : " wrapped array in")
                    + (inLambda() ? " lambda field " + currentLambda.getFieldNameForLocal(local) : (method
                            .getAccessedParameters().contains(local) ? " method parameter " : " local ")
                            + local));
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
            mv.visitFieldInsn(GETFIELD, currentLambdaClass(), currentLambda.getFieldNameForLocal(local), type
                    .getDescriptor());
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
            int realLocalIndex = currentLambda.getParameterRealLocalIndex(name);
            debugLambdaParameterAccess(name, store, type, realLocalIndex);

            if (store) {
                mv.visitVarInsn(type.getOpcode(ISTORE), realLocalIndex);
            } else {
                mv.visitVarInsn(type.getOpcode(ILOAD), realLocalIndex);
                if (isReference(type))
                    mv.visitTypeInsn(CHECKCAST, type.getInternalName());
            }
        }

        void debugLambdaParameterAccess(String name, boolean store, Type type, int realLocalIndex) {
            debug("parameter " + name + " " + getSimpleClassName(type) + (store ? " stored in" : " read from")
                    + " lambda local " + realLocalIndex);
        }

        void createLambdaClass() {
            lambdaWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            lambdaWriter.visit(V1_5, ACC_FINAL | ACC_SYNTHETIC, currentLambdaClass(), null, getLambdaSuperType()
                    .getInternalName(), getLambdaInterfaces());
        }

        String[] getLambdaInterfaces() {
            Type type = currentLambda.getType();
            if (transformer.isInterface(type.getInternalName()))
                return new String[] { type.getInternalName() };
            return null;
        }

        void createLambdaMethodAndRedirectMethodVisitorToIt() {
            currentLambdaMethod = currentLambda.getLambdaMethod();

            mv = lambdaWriter.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, currentLambdaMethod.name,
                    currentLambdaMethod.desc, null, null);
            mv.visitCode();

            convertMethodArgumentsToLambdaParameterTypes();
        }

        void convertMethodArgumentsToLambdaParameterTypes() {
            Type[] methodParameterTypes = getArgumentTypes(currentLambdaMethod.desc);
            Type[] lambdaParameterTypes = currentLambda.getParameterTypes();

            for (int i = 0; i < methodParameterTypes.length; i++) {
                Type methodParameterType = methodParameterTypes[i];
                Type lambdaParameterType = lambdaParameterTypes[i];

                if (!methodParameterType.equals(lambdaParameterType)
                        && !(isReference(methodParameterType) && isReference(lambdaParameterType)))
                    convertMethodArgumentIntoLambdaParameterType(i + 1, methodParameterType, lambdaParameterType);
            }
        }

        void convertMethodArgumentIntoLambdaParameterType(int parameterIndex, Type methodParameterType,
                Type lambdaParameterType) {
            int localIndex = currentLambda.getParameterRealLocalIndex(currentLambda
                    .getParameterByIndex(parameterIndex));

            debug("parameter " + currentLambda.getParameterByIndex(parameterIndex) + " converted from "
                    + getSimpleClassName(methodParameterType) + " to " + getSimpleClassName(lambdaParameterType)
                    + " in lambda local " + localIndex);

            if (isReference(methodParameterType) && isPrimitive(lambdaParameterType))
                unboxLocal(localIndex, lambdaParameterType);

            else if (isPrimitive(methodParameterType) && isPrimitive(lambdaParameterType))
                convertNarrowlyBetweenPrimitiveWiderMethodArgumentTypeIntoLambdaParameterType(methodParameterType,
                        lambdaParameterType, localIndex);
        }

        void convertNarrowlyBetweenPrimitiveWiderMethodArgumentTypeIntoLambdaParameterType(
                Type methodParameterType, Type lambdaParameterType, int localIndex) {
            if (DOUBLE_TYPE == methodParameterType)
                switch (lambdaParameterType.getSort()) {
                case BYTE:
                case CHAR:
                case SHORT:
                case INT:
                    convertPrimitive(methodParameterType, lambdaParameterType, localIndex, D2I);
                    break;
                case Type.FLOAT:
                    convertPrimitive(methodParameterType, lambdaParameterType, localIndex, D2F);
                    break;
                case Type.LONG:
                    convertPrimitive(methodParameterType, lambdaParameterType, localIndex, D2L);
                    break;
                }
            else if (LONG_TYPE == methodParameterType)
                convertPrimitive(methodParameterType, lambdaParameterType, localIndex, L2I);
        }

        void convertPrimitive(Type from, Type to, int local, int opcode) {
            mv.visitVarInsn(from.getOpcode(ILOAD), local);
            mv.visitInsn(opcode);
            mv.visitVarInsn(to.getOpcode(ISTORE), local);
        }

        void returnFromLambdaMethod() {
            Type returnType = getReturnType(currentLambdaMethod.desc);
            Type lambdaExpressionType = currentLambda.getExpressionType();

            handleBoxingAndUnboxing(returnType, lambdaExpressionType);

            mv.visitInsn(returnType.getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            currentLambdaMethod = null;
        }

        void handleBoxingAndUnboxing(Type returnType, Type lambdaExpressionType) {
            if (isPrimitive(returnType) && isReference(lambdaExpressionType)) {
                unbox(returnType);
                debug("unboxed return value with type " + getSimpleClassName(lambdaExpressionType) + " as "
                        + getSimpleClassName(returnType));
            }
            if (isReference(returnType) && isPrimitive(lambdaExpressionType)) {
                box(lambdaExpressionType);
                debug("boxed return value with type " + getSimpleClassName(lambdaExpressionType) + " as "
                        + getSimpleClassName(returnType));
            }
        }

        void box(Type type) {
            if (isReference(type))
                return;

            Type boxed = getBoxedType(type);
            String descriptor = getMethodDescriptor(boxed, new Type[] { type });
            String name = "valueOf";
            mv.visitMethodInsn(INVOKESTATIC, boxed.getInternalName(), name, descriptor);
        }

        boolean isReference(Type type) {
            return type.getSort() == OBJECT || type.getSort() == ARRAY;
        }

        boolean isPrimitive(Type type) {
            return !isReference(type);
        }

        void boxLocal(int local, Type type) {
            mv.visitVarInsn(type.getOpcode(ILOAD), local);
            box(type);
            mv.visitVarInsn(ASTORE, local);
        }

        void unboxLocal(int local, Type type) {
            mv.visitVarInsn(ALOAD, local);
            unbox(type);
            mv.visitVarInsn(type.getOpcode(ISTORE), local);
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

        void unbox(Type primitiveType) {
            Type type = getType(Number.class);
            switch (primitiveType.getSort()) {
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
            String descriptor = getMethodDescriptor(primitiveType, new Type[0]);
            String name = primitiveType.getClassName() + "Value";

            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
            mv.visitMethodInsn(INVOKEVIRTUAL, type.getInternalName(), name, descriptor);
        }

        void createLambdaConstructor() {
            Type[] parameters = getLambdaConstructorParameters();
            String descriptor = getMethodDescriptor(VOID_TYPE, parameters);

            MethodVisitor mv = lambdaWriter.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
            mv.visitCode();

            createAndInitializeFieldsWithAccessedLocals(mv, parameters);

            invokeSuperConstructor(mv);

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        void createAndInitializeFieldsWithAccessedLocals(MethodVisitor mv, Type[] parameters) {
            Iterator<Integer> locals = currentLambda.accessedLocals.iterator();
            for (int i = 0; locals.hasNext(); i++) {
                String field = currentLambda.getFieldNameForLocal(locals.next());
                Type type = parameters[i];

                lambdaWriter.visitField(ACC_SYNTHETIC | ACC_PRIVATE | ACC_FINAL, field, type.getDescriptor(), null,
                        null).visitEnd();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(type.getOpcode(ILOAD), i + 1);
                mv.visitFieldInsn(PUTFIELD, currentLambdaClass(), field, type.getDescriptor());
            }
        }

        void invokeSuperConstructor(MethodVisitor mv) {
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
            lambdaWriter.visitOuterClass(className, method.name, method.desc);
            lambdaWriter.visitEnd();

            cv.visitInnerClass(currentLambdaClass(), className, null, 0);

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
            return className + "$" + (currentLine > 0 ? String.format("%04d_", currentLine) : "") + lambdaClass
                    + "_" + currentLambdaId;
        }

        boolean inLambda() {
            return currentLambdaMethod != null || inLambdaDefinition;
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

    public MethodVisitor visitMethod(int access, final String name, String desc, String signature,
            String[] exceptions) {
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