package org.enumerable.lambda.weaving.tree;

import org.enumerable.lambda.annotation.LambdaLocal;
import org.enumerable.lambda.annotation.LambdaParameter;
import org.enumerable.lambda.annotation.NewLambda;
import org.enumerable.lambda.weaving.ClassFilter;
import org.enumerable.lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer.LambdaAnalyzer;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.ASMifierMethodVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.System.getProperty;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static org.enumerable.lambda.exception.UncheckedException.uncheck;
import static org.enumerable.lambda.weaving.Debug.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;

class LambdaTreeWeaver implements Opcodes {
    static Type newLambdaAnnotation = getConfigurableAnnotationType("lambda.weaving.annotation.newlambda", NewLambda.class.getName());
    static Type lambdaParameterAnnotation = getConfigurableAnnotationType("lambda.weaving.annotation.lambdaparameter", LambdaParameter.class.getName());

    ClassNode c;
    int currentLambdaId = 1;
    List<MethodAnalyzer> methods = new ArrayList<MethodAnalyzer>();

    Map<String, FieldNode> fieldsThatNeedStaticAccessMethod = new HashMap<String, FieldNode>();
    Map<String, MethodNode> methodsThatNeedStaticAccessMethod = new HashMap<String, MethodNode>();

    Map<String, MethodNode> staticAccessMethodsByFieldName = new HashMap<String, MethodNode>();
    Map<String, MethodNode> staticAccessMethodsByMethodNameAndDesc = new HashMap<String, MethodNode>();

    ClassLoader loader;
    ClassFilter filter;
    ClassReader cr;

    LambdaTreeWeaver(ClassLoader loader, ClassFilter filter, ClassReader cr) {
        this.loader = loader;
        this.filter = filter;
        this.cr = cr;
    }

    @SuppressWarnings("unchecked")
    LambdaTreeWeaver analyze() {
        try {
            if (c != null)
                return this;

            c = new ClassNode();
            cr.accept(c, 0);

            devDebug(c.name);
            devDebug("");

            for (MethodNode m : (List<MethodNode>) c.methods) {
                MethodAnalyzer ma = new MethodAnalyzer(m);
                ma.analyze();
                methods.add(ma);
            }
            return this;
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    ClassNode transform() {
        try {
            if (c == null)
                analyze();

            debug("transforming " + getObjectType(c.name).getClassName());

            for (MethodAnalyzer ma : (methods)) {
                try {
                    debugIndent();
                    ma.transform();
                } finally {
                    debugDedent();
                }
            }

            return c;
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    boolean hasLambdas() {
        for (MethodAnalyzer ma : (methods))
            if (!ma.lambdas.isEmpty())
                return true;
        return false;
    }

    List<LambdaAnalyzer> getLambdas() {
        List<LambdaAnalyzer> result = new ArrayList<LambdaAnalyzer>();
        for (MethodAnalyzer ma : (methods))
            result.addAll(ma.lambdas);
        return result;
    }

    class MethodAnalyzer {
        MethodNode m;
        Method method;
        Frame[] frames;
        int currentLambda = 0;

        List<LambdaAnalyzer> lambdas = new ArrayList<LambdaAnalyzer>();
        Map<String, LocalVariableNode> methodLocals = new LinkedHashMap<String, LocalVariableNode>();
        Map<String, LocalVariableNode> methodMutableLocals = new LinkedHashMap<String, LocalVariableNode>();
        int line;

        MethodAnalyzer(MethodNode m) {
            this.m = m;
            method = new Method(m.name, m.desc);
        }

        void transform() throws IOException {
            if (lambdas.isEmpty())
                return;

            debug("processing " + method);

            InsnList instructions = m.instructions;
            m.instructions = new InsnList();

            for (LocalVariableNode local : getLocalsMutableFromLambdas().values())
                initArray(m, local);

            for (int i = 0; i < instructions.size(); i++) {
                AbstractInsnNode n = instructions.get(i);
                if (currentLambda < lambdas.size() && i == lambdas.get(currentLambda).getStart()) {
                    LambdaAnalyzer lambda = lambdas.get(currentLambda);

                    i = lambdas.get(currentLambda).getEnd();
                    currentLambda++;

                    try {
                        debugIndent();
                        lambda.transform(instructions);
                    } finally {
                        debugDedent();
                    }

                    lambda.instantiate(m, null);

                } else if (n.getType() == VAR_INSN) {
                    VarInsnNode vin = (VarInsnNode) n;
                    LocalVariableNode local = getLocalVariable(vin.var);

                    if (local != null && getLocalsMutableFromLambdas().containsKey(local.name)) {
                        loadArrayFromLocal(m, local);

                        if (isStoreInstruction(vin))
                            storeTopOfStackInArray(m, getType(local.desc));

                        else
                            loadFirstElementOfArray(m, getType(local.desc));
                    } else {

                        n.accept(m);
                    }
                } else if (n.getType() == IINC_INSN) {
                    IincInsnNode iinc = (IincInsnNode) n;
                    LocalVariableNode local = getLocalVariable(iinc.var);

                    if (local != null && getLocalsMutableFromLambdas().containsKey(local.name)) {
                        loadArrayFromLocal(m, local);
                        incrementInArray(m, iinc.incr);

                    } else {
                        n.accept(m);
                    }
                } else {
                    n.accept(m);
                }
            }
            endMethod(m);

            devDebug("after ================= ");
            devDebugAsm(m);
        }

        void endMethod(MethodVisitor mv) {
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        @SuppressWarnings("unchecked")
        LocalVariableNode getLocalVariable(int var) {
            for (LocalVariableNode local : (List<LocalVariableNode>) m.localVariables)
                if (var == local.index)
                    return local;
            return null;
        }

        void analyze() throws Exception {
            frames = new Analyzer(new BasicInterpreter()).analyze(c.name, m);

            line = 0;
            for (int i = 0; i < m.instructions.size(); i++) {
                AbstractInsnNode n = m.instructions.get(i);

                int type = n.getType();
                if (type == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode mi = (MethodInsnNode) n;

                    if (isNewLambdaMethod(mi)) {
                        List<int[]> argumentRanges = findArgumentInstructionRangesOfLambda(i,
                                getArgumentTypes(mi.desc).length);

                        lambdas.add(0, new LambdaAnalyzer(currentLambdaId++, line, mi, argumentRanges));
                    }
                }
                if (type == VAR_INSN)
                    localVariable((VarInsnNode) n);

                if (type == IINC_INSN)
                    localVariable((IincInsnNode) n);

                if (type == LINE)
                    line = ((LineNumberNode) n).line;
            }

            Collections.sort(lambdas, new Comparator<LambdaAnalyzer>() {
                public int compare(LambdaAnalyzer o1, LambdaAnalyzer o2) {
                    return o1.getStart() - o2.getStart();
                }
            });

            for (LambdaAnalyzer lambda : lambdas)
                lambda.analyze();

            if (!lambdas.isEmpty()) {
                devDebug("");
                devDebug("before ================ ");
                devDebugAsm(m);
            }
        }

        List<int[]> findArgumentInstructionRangesOfLambda(int end, int arguments) {
            List<int[]> argumentRanges = new ArrayList<int[]>();

            if (arguments == 0)
                argumentRanges.add(new int[] { end, end - 1 });

            while (arguments-- > 0) {
                int start = findInstructionWithRelativeStackDepthOf(end, -1);
                argumentRanges.add(0, new int[] { start, end - 1 });
                end = start;
            }

            return argumentRanges;
        }

        int findInstructionWithRelativeStackDepthOf(int index, int relativeDepth) {
            if (relativeDepth == 0)
                return index;
            int depth = getStackSize(index) + relativeDepth;
            for (int i = index - 1; i >= 0; i--) {
                if (getStackSize(i) == depth) {
                    if (i > 0) {
                        AbstractInsnNode n = m.instructions.get(i - 1);
                        if (n.getType() == LABEL)
                            return resolveBranches(i, (LabelNode) n);

                        else if (n.getType() == FRAME || n.getType() == JUMP_INSN)
                            continue;

                    }
                    return i;
                }
            }
            throw new IllegalStateException("Could not find previous stack depth of " + depth + " at " + sourceAndLine());
        }

        int resolveBranches(int end, LabelNode label) {
            int start = lambdas.isEmpty() ? 0 : lambdas.get(lambdas.size() - 1).getEnd();
            for (int j = start; j < end; j++) {
                AbstractInsnNode n = m.instructions.get(j);
                if (n.getType() == JUMP_INSN) {
                    if (((JumpInsnNode) n).label == label) {
                        switch (n.getOpcode()) {
                        case IFEQ:
                        case IFNE:
                        case IFLT:
                        case IFGE:
                        case IFGT:
                        case IFLE:
                        case IFNULL:
                        case IFNONNULL:
                            return findInstructionWithRelativeStackDepthOf(j, -1);
                        case IF_ICMPEQ:
                        case IF_ICMPNE:
                        case IF_ICMPLT:
                        case IF_ICMPGE:
                        case IF_ICMPGT:
                        case IF_ICMPLE:
                        case IF_ACMPEQ:
                        case IF_ACMPNE:
                            return findInstructionWithRelativeStackDepthOf(j, -2);
                        }
                    }
                }
            }
            return end;
        }

        int getStackSize(int i) {
            return frames[i] == null ? 0 : frames[i].getStackSize();
        }

        LocalVariableNode localVariable(VarInsnNode vin) {
            LocalVariableNode local = getLocalVariable(vin.var);
            if (local == null)
                return null;
           

            if (isStoreInstruction(vin) && methodLocals.containsKey(local.name))
                methodMutableLocals.put(local.name, local);

            methodLocals.put(local.name, local);
            return local;
        }

        LocalVariableNode localVariable(IincInsnNode iin) {
            LocalVariableNode local = getLocalVariable(iin.var);
            if (local == null)
                return local;
            methodMutableLocals.put(local.name, local);
            methodLocals.put(local.name, local);
            return local;
        }

        boolean isStoreInstruction(VarInsnNode vin) {
            return vin.getOpcode() >= ISTORE && vin.getOpcode() <= ASTORE;
        }

        void loadArrayFromLocal(MethodVisitor mv, LocalVariableNode local) {
            mv.visitVarInsn(ALOAD, local.index);
        }

        void loadFirstElementOfArray(MethodVisitor mv, Type type) {
            mv.visitInsn(ICONST_0);
            mv.visitInsn(type.getOpcode(IALOAD));
        }

        void storeTopOfStackInArray(MethodVisitor mv, Type type) {
            // x is at the top of the stack, as it was to be stored
            // directly into a local
            // x a[]
            mv.visitInsn(ICONST_0);
            // x a[] 0
            mv.visitInsn(type.getSize() == 2 ? DUP2_X2 : DUP2_X1);
            // a[] 0 x a[] 0
            mv.visitInsn(POP2);
            // a[] 0 x
            mv.visitInsn(type.getOpcode(IASTORE));
        }

        Map<String, LocalVariableNode> getLocalsMutableFromLambdas() {
            Map<String, LocalVariableNode> localsMutableFromLambdas = new HashMap<String, LocalVariableNode>();

            for (LambdaAnalyzer lambda : lambdas)
                localsMutableFromLambdas.putAll(lambda.getMutableLocals());
            return localsMutableFromLambdas;
        }

        void initArray(MethodVisitor mv, LocalVariableNode local) {
            mv.visitInsn(ICONST_1);
            newArray(mv, getType(local.desc));

            if (isMethodArgument(local))
                initializeArrayOnTopOfStackWithCurrentValueOfLocal(mv, local);

            mv.visitVarInsn(ASTORE, local.index);
        }

        boolean isMethodArgument(LocalVariableNode local) {
            return local.index <= getArgumentTypes(m.desc).length;
        }

        void initializeArrayOnTopOfStackWithCurrentValueOfLocal(MethodVisitor mv, LocalVariableNode local) {
            initializeArrayOnTopOfStackWithCurrentValueOfLocal(mv, getType(local.desc), local.index);
        }

        void initializeArrayOnTopOfStackWithCurrentValueOfLocal(MethodVisitor mv, Type type, int index) {
            // a[]
            mv.visitInsn(DUP);
            // a[] a[]
            mv.visitInsn(ICONST_0);
            // a[] a[] 0
            mv.visitVarInsn(type.getOpcode(ILOAD), index);
            // a[] a[] 0 x
            mv.visitInsn(type.getOpcode(IASTORE));
            // a[]
        }

        void newArray(MethodVisitor mv, Type type) {
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

        void incrementInArray(MethodVisitor mv, int increment) {
            // a[]
            mv.visitInsn(ICONST_0);
            // a[] 0
            mv.visitInsn(DUP2);
            // a[] 0 a[] 0
            mv.visitInsn(IALOAD);
            // a[] 0 a
            loadInt(mv, increment);
            // a[] 0 a i
            mv.visitInsn(IADD);
            // a[] 0 a+i
            mv.visitInsn(IASTORE);
        }

        void loadInt(MethodVisitor mv, int value) {
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
                mv.visitIntInsn(Opcodes.BIPUSH, value);
            else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
                mv.visitIntInsn(Opcodes.SIPUSH, value);
            else
                mv.visitLdcInsn(value);
        }

        class LambdaAnalyzer {
            LambdaAnalyzer parent;

            int id;
            int line;

            Method newLambdaMethod;
            List<Type> newLambdaParameterTypes;

            Type lambdaType;
            Type expressionType;

            Set<String> parametersWithDefaultValue = new HashSet<String>();
            Set<String> parametersMutableFromChildLambdas = new HashSet<String>();
            Map<String, FieldNode> parameters = new LinkedHashMap<String, FieldNode>();
            Map<String, String> parameterOwners = new LinkedHashMap<String, String>();
            Map<String, FieldNode> parentParameters = new LinkedHashMap<String, FieldNode>();

            Map<String, LocalVariableNode> locals = new LinkedHashMap<String, LocalVariableNode>();

            List<int[]> argumentRanges;

            Method sam;
            ClassNode lambda;
            MethodNode saMn;

            ASMifierMethodVisitor devDebugAsm = new ASMifierMethodVisitor();

            LambdaAnalyzer(int id, int line, MethodInsnNode mi, List<int[]> argumentRanges) {
                this.id = id;
                this.line = line;
                this.argumentRanges = argumentRanges;

                newLambdaMethod = new Method(mi.name, mi.desc);

                newLambdaParameterTypes = asList(newLambdaMethod.getArgumentTypes());
                expressionType = newLambdaParameterTypes.isEmpty() ? getType(Object.class)
                        : newLambdaParameterTypes.get(newLambdaParameterTypes.size() - 1);

                if (!newLambdaParameterTypes.isEmpty())
                    newLambdaParameterTypes = newLambdaParameterTypes
                            .subList(0, newLambdaParameterTypes.size() - 1);

                lambdaType = resolveLambdaType();
            }

            void transform(InsnList instructions) throws IOException {
                debugLambdaStart();

                lambda = new ClassNode();
                lambda.visit(V1_5, ACC_FINAL | ACC_SYNTHETIC | ACC_PUBLIC, lambdaClass(), null, getSuperType()
                        .getInternalName(), getLambdaInterfaces());

                createLambdaConstructor();
                createSAMethod();

                for (String parameter : parametersWithDefaultValue) {
                    int[] range = argumentRanges.get(getParameterIndex(parameter));
                    captureDefaultParameterValue(parameter, range[0], range[1], instructions);
                }

                for (String parameter : parametersMutableFromChildLambdas)
                    if (parameters.containsKey(parameter)) {
                        int index = getParameterRealLocalIndex(parameter);
                        initArray(saMn, getParameterTypes().get(getParameterIndex(parameter)), index);
                    }

                for (int i = getBodyStart(); i < getEnd(); i++) {
                    AbstractInsnNode n = instructions.get(i);

                    if (currentLambda < lambdas.size() && i == lambdas.get(currentLambda).getStart()) {
                        LambdaAnalyzer la = lambdas.get(currentLambda);

                        i = lambdas.get(currentLambda).getEnd();
                        currentLambda++;

                        la.transform(instructions);
                        la.instantiate(saMn, this);
                    } else if (isInSAMBody(i))
                        handleInsnNodeInSAM(saMn, n);
                }

                returnFromSAMethod();

                lambda.visitOuterClass(c.name, m.name, m.desc);

                c.visitInnerClass(lambdaClass(), c.name, null, 0);

                devDebug("new lambda ================= ");
                if (devDebug)
                    lambda.accept(new ASMifierClassVisitor(new PrintWriter(out)));
            }

            void debugLambdaStart() {
                String localsString = "";
                if (!locals.isEmpty())
                    localsString = " closing over " + locals.keySet();

                debug("lambda #" + getSimpleClassName(expressionType) + getTypedParametersString() + localsString
                        + " as " + sam + " in " + getSimpleClassName(lambdaType) + " at " + sourceAndLine());
            }

            void handleInsnNodeInSAM(MethodVisitor mv, AbstractInsnNode n) throws IOException {
                int type = n.getType();
                if (type == VAR_INSN) {
                    VarInsnNode vin = (VarInsnNode) n;
                    LocalVariableNode local = getLocalVariable(vin.var);

                    debugLocalVariableAccess(local, isStoreInstruction(vin));

                    if (local == null) {
                        n.accept(mv);

                    } else if (getLocalsMutableFromLambdas().containsKey(local.name)) {
                        loadLambdaField(mv, local, toArrayType(getType(local.desc)));
                        if (isStoreInstruction(vin))
                            storeTopOfStackInArray(mv, getType(local.desc));

                        else
                            loadFirstElementOfArray(mv, getType(local.desc));

                    } else {
                        loadLambdaField(mv, local, getType(local.desc));

                    }

                } else if (type == FIELD_INSN) {
                    FieldInsnNode fin = (FieldInsnNode) n;
                    if (isLambdaParameterField(fin)) {
                        accessParameter(mv, fin);

                    } else if (isPrivateFieldOnOwnerWhichNeedsAcccessMethodFromLambda(fin)) {
                        MethodNode am = getAccessMethodReplacingPrivateFieldAccess(fin);
                        mv.visitMethodInsn(INVOKESTATIC, c.name, am.name, am.desc);

                    } else {
                        n.accept(mv);
                    }

                } else if (type == IINC_INSN) {
                    IincInsnNode iinc = (IincInsnNode) n;
                    LocalVariableNode local = getLocalVariable(iinc.var);
                    if (local != null) {
                        loadLambdaField(mv, local, toArrayType(getType(local.desc)));
                        incrementInArray(mv, iinc.incr);
                    }

                } else if (type == METHOD_INSN) {
                    MethodInsnNode mn = (MethodInsnNode) n;

                    if (isPrivateMethodOnOwnerWhichNeedsAcccessMethodFromLambda(mn)) {
                        MethodNode am = getAccessMethodReplacingPrivateMethodInvocation(mn);
                        mv.visitMethodInsn(INVOKESTATIC, c.name, am.name, am.desc);

                    } else {
                        n.accept(mv);
                    }
                } else {
                    n.accept(mv);
                }
            }

            void initArray(MethodVisitor mv, Type type, int index) {
                mv.visitInsn(ICONST_1);
                newArray(mv, type);

                initializeArrayOnTopOfStackWithCurrentValueOfLocal(mv, type, index);

                mv.visitVarInsn(ASTORE, index);
            }

            MethodNode getAccessMethodReplacingPrivateFieldAccess(FieldInsnNode fn) {
                int opcode = fn.getOpcode();
                boolean write = opcode == PUTFIELD || opcode == PUTSTATIC;

                String key = fn.name.substring(0, 1).toUpperCase() + fn.name.substring(1);
                key = (write ? "set" : "get") + key;
                key = key + (opcode == GETSTATIC || opcode == PUTSTATIC ? "Static" : "");

                if (staticAccessMethodsByFieldName.containsKey(key))
                    return staticAccessMethodsByFieldName.get(key);

                List<Type> argumentTypes = new ArrayList<Type>();

                if (opcode == GETFIELD || opcode == PUTFIELD)
                    argumentTypes.add(getObjectType(fn.owner));

                if (write)
                    argumentTypes.add(getType(fn.desc));

                Type returnType = getType(fn.desc);
                MethodNode am = createAccessMethodAndLoadArguments("field$" + fn.name, argumentTypes,
                        returnType);

                fn.accept(am);

                if (write) {
                    int indexOfNewFieldValue = argumentTypes.size() - 1;
                    am.visitVarInsn(argumentTypes.get(indexOfNewFieldValue).getOpcode(ILOAD), indexOfNewFieldValue);
                }

                am.visitInsn(returnType.getOpcode(IRETURN));
                am.visitMaxs(0, 0);
                am.visitEnd();

                staticAccessMethodsByFieldName.put(key, am);

                return am;
            }

            void debugLocalVariableAccess(LocalVariableNode local, boolean store) {
                boolean readOnly = methodMutableLocals.containsKey(local.name);
                debug("variable " + local.name + " " + getSimpleClassName(getType(local.desc))
                        + (store ? (readOnly ? " initalized in" : " stored in") : " read from")
                        + (readOnly ? " final" : " wrapped array in") + " lambda field "
                        + getFieldNameForLocal(local)
                        + (isMethodParameter(local) ? " method parameter " : " local ") + local.name);
            }

            void debugLambdaParameterAccess(FieldInsnNode fin, int realLocalIndex) {
                debug("parameter " + fin.name + " " + getSimpleClassName(getType(fin.desc))
                        + (fin.getOpcode() == PUTSTATIC ? " stored in" : " read from") + " lambda local "
                        + realLocalIndex);
            }

            MethodNode getAccessMethodReplacingPrivateMethodInvocation(MethodInsnNode mn) {
                String key = mn.name + mn.desc;
                if (staticAccessMethodsByMethodNameAndDesc.containsKey(key))
                    return staticAccessMethodsByMethodNameAndDesc.get(mn.name + mn.desc);

                List<Type> argumentTypes = new ArrayList<Type>();

                int opcode = mn.getOpcode();
                if (opcode != INVOKESTATIC)
                    argumentTypes.add(getObjectType(mn.owner));

                Collections.addAll(argumentTypes, getArgumentTypes(mn.desc));

                MethodNode am = createAccessMethodAndLoadArguments("method$" + mn.name, argumentTypes,
                        getReturnType(mn.desc));

                mn.accept(am);

                am.visitInsn(getReturnType(mn.desc).getOpcode(IRETURN));
                am.visitMaxs(0, 0);
                am.visitEnd();

                staticAccessMethodsByMethodNameAndDesc.put(key, am);

                return am;
            }

            MethodNode createAccessMethodAndLoadArguments(String name, List<Type> argumentTypes,
                                                          Type returnType) {
                String accessMethodDescriptor = getMethodDescriptor(returnType, argumentTypes.toArray(new Type[0]));
                String accessMethodName = "access$lambda$" + name;

                MethodNode am = (MethodNode) c.visitMethod(ACC_STATIC + ACC_SYNTHETIC, accessMethodName,
                        accessMethodDescriptor, null, null);
                am.visitCode();

                int i = 0;
                for (Type type : argumentTypes)
                    am.visitVarInsn(type.getOpcode(ILOAD), i++);

                return am;
            }

            boolean isPrivateFieldOnOwnerWhichNeedsAcccessMethodFromLambda(FieldInsnNode fn) throws IOException {
                return c.name.equals(fn.owner) && hasAccess(findField(fn), ACC_PRIVATE);
            }

            boolean isPrivateMethodOnOwnerWhichNeedsAcccessMethodFromLambda(MethodInsnNode mn) throws IOException {
                return c.name.equals(mn.owner)
                        && (INVOKESPECIAL == mn.getOpcode() || INVOKESTATIC == mn.getOpcode())
                        && hasAccess(findMethod(mn), ACC_PRIVATE);
            }

            void captureDefaultParameterValue(String name, int start, int end, InsnList insns) throws IOException {
                int parameterIndex = getParameterIndex(name);
                Type defaultParameterValueType = getParameterTypes().get(parameterIndex);

                debug("parameter " + name + " " + getSimpleClassName(defaultParameterValueType)
                        + " has default value");

                MethodNode mv = (MethodNode) lambda
                        .visitMethod(ACC_PROTECTED | ACC_SYNTHETIC, "default$" + (parameterIndex + 1),
                                getMethodDescriptor(getType(Object.class), new Type[0]), null, null);
                mv.visitCode();

                for (int i = start; i < end; i++)
                    handleInsnNodeInSAM(mv, insns.get(i));

                Type returnType = isReference(defaultParameterValueType) ? defaultParameterValueType
                        : getBoxedType(defaultParameterValueType);

                if (isPrimitive(defaultParameterValueType))
                    box(mv, defaultParameterValueType);

                mv.visitInsn(returnType.getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            void accessParameter(MethodVisitor mv, FieldInsnNode fin) throws IOException {
                Type type = getType(fin.desc);
                if (parentParameters.containsKey(fin.name)) {
                    accessParentParameter(mv, fin, type);
                    return;
                }

                int realLocalIndex = getParameterRealLocalIndex(fin.name);

                debugLambdaParameterAccess(fin, realLocalIndex);

                if (parametersMutableFromChildLambdas.contains(fin.name)) {
                    mv.visitVarInsn(ALOAD, realLocalIndex);

                    if (fin.getOpcode() == PUTSTATIC)
                        storeTopOfStackInArray(mv, type);

                    else
                        loadFirstElementOfArray(mv, type);

                } else {

                    if (fin.getOpcode() == PUTSTATIC) {
                        mv.visitVarInsn(type.getOpcode(ISTORE), realLocalIndex);

                    } else {

                        mv.visitVarInsn(type.getOpcode(ILOAD), realLocalIndex);
                        if (isReference(type))
                            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
                    }
                }
            }

            void accessParentParameter(MethodVisitor mv, FieldInsnNode fin, Type type) {
                mv.visitVarInsn(ALOAD, 0);
                if (parametersMutableFromChildLambdas.contains(fin.name)) {
                    mv.visitFieldInsn(GETFIELD, lambdaClass(), getParentParameterName(fin.name), toArrayType(type)
                            .getDescriptor());
                    if (fin.getOpcode() == PUTSTATIC)
                        storeTopOfStackInArray(mv, type);
                    else
                        loadFirstElementOfArray(mv, type);
                } else
                    mv.visitFieldInsn(GETFIELD, lambdaClass(), getParentParameterName(fin.name), type
                            .getDescriptor());

                debug("parameter " + fin.name + " " + getSimpleClassName(getType(fin.desc))
                        + (fin.getOpcode() == PUTSTATIC ? " stored in" : " read from") + " parent lambda parameter");
            }

            String getParentParameterName(String name) {
                return name + "$parentParameter";
            }

            int getParameterRealLocalIndex(String name) {
                List<Type> parameterTypes = getParameterTypes();
                int index = 1;
                for (int i = 0; i < parameterTypes.size(); i++)
                    if (getParameterIndex(name) == i)
                        break;
                    else
                        index += parameterTypes.get(i).getSize();
                return index;
            }

            void loadLambdaField(MethodVisitor mv, LocalVariableNode local, Type type) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, lambdaClass(), getFieldNameForLocal(local), type.getDescriptor());
            }

            void createSAMethod() {
                saMn = (MethodNode) lambda.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, sam.getName(), sam
                        .getDescriptor(), null, null);
                addLambdaParameterAnnotations();
                saMn.visitCode();

                convertMethodArgumentsToLambdaParameterTypes();
            }

            void addLambdaParameterAnnotations() {
                int i = 0;
                for (String parameter : parameters.keySet()) {
                    AnnotationVisitor annotation = saMn.visitParameterAnnotation(i++, getType(LambdaLocal.class)
                            .getDescriptor(), true);
                    annotation.visit("name", parameter);
                    annotation.visit("parameterClass", getObjectType(parameterOwners.get(parameter)).getClassName());
                    annotation.visitEnd();
                }
            }

            void convertMethodArgumentsToLambdaParameterTypes() {
                Type[] methodParameterTypes = sam.getArgumentTypes();
                Type[] lambdaParameterTypes = getParameterTypes().toArray(new Type[0]);

                for (int i = 0; i < methodParameterTypes.length; i++) {
                    Type methodParameterType = methodParameterTypes[i];
                    Type lambdaParameterType = lambdaParameterTypes[i];

                    if (!methodParameterType.equals(lambdaParameterType)
                            && !(isReference(methodParameterType) && isReference(lambdaParameterType)))
                        convertMethodArgumentIntoLambdaParameterType(i, methodParameterType, lambdaParameterType);
                }
            }

            void convertMethodArgumentIntoLambdaParameterType(int parameterIndex, Type methodParameterType,
                    Type lambdaParameterType) {
                int localIndex = getParameterRealLocalIndex(getParameter(parameterIndex));
                debug("parameter " + getParameter(parameterIndex) + " converted from "
                        + getSimpleClassName(methodParameterType) + " to "
                        + getSimpleClassName(lambdaParameterType) + " in lambda local " + localIndex);

                if (isReference(methodParameterType) && isPrimitive(lambdaParameterType))
                    unboxLocal(saMn, localIndex, lambdaParameterType);

                else if (isPrimitive(methodParameterType) && isPrimitive(lambdaParameterType))
                    convertNarrowlyBetweenPrimitiveWiderMethodArgumentTypeIntoLambdaParameterType(
                            methodParameterType, lambdaParameterType, localIndex);
            }

            void convertNarrowlyBetweenPrimitiveWiderMethodArgumentTypeIntoLambdaParameterType(
                    Type methodParameterType, Type lambdaParameterType, int localIndex) {
                if (DOUBLE_TYPE == methodParameterType)
                    switch (lambdaParameterType.getSort()) {
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        convertPrimitive(saMn, methodParameterType, lambdaParameterType, localIndex, D2I);
                        break;
                    case Type.FLOAT:
                        convertPrimitive(saMn, methodParameterType, lambdaParameterType, localIndex, D2F);
                        break;
                    case Type.LONG:
                        convertPrimitive(saMn, methodParameterType, lambdaParameterType, localIndex, D2L);
                        break;
                    }
                else if (LONG_TYPE == methodParameterType)
                    convertPrimitive(saMn, methodParameterType, lambdaParameterType, localIndex, L2I);
            }

            void convertPrimitive(MethodNode mv, Type from, Type to, int local, int opcode) {
                mv.visitVarInsn(from.getOpcode(ILOAD), local);
                mv.visitInsn(opcode);
                mv.visitVarInsn(to.getOpcode(ISTORE), local);
            }

            void unboxLocal(MethodNode mv, int local, Type type) {
                mv.visitVarInsn(ALOAD, local);
                unbox(mv, type);
                mv.visitVarInsn(type.getOpcode(ISTORE), local);
            }

            void unbox(MethodNode mv, Type primitiveType) {
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

            void returnFromSAMethod() {
                if (getStart() == getEnd())
                    saMn.visitInsn(Opcodes.ACONST_NULL);

                handleBoxingAndUnboxingOfReturnFromLambda(sam.getReturnType(), expressionType);
                saMn.visitInsn(sam.getReturnType().getOpcode(IRETURN));
                endMethod(saMn);
            }

            void handleBoxingAndUnboxingOfReturnFromLambda(Type returnType, Type lambdaExpressionType) {
                if (isPrimitive(returnType) && isReference(lambdaExpressionType)) {
                    unbox(saMn, returnType);
                    debug("unboxed return value with type " + getSimpleClassName(lambdaExpressionType) + " as "
                            + getSimpleClassName(returnType));
                }
                if (isReference(returnType) && isPrimitive(lambdaExpressionType)) {
                    box(saMn, lambdaExpressionType);
                    debug("boxed return value with type " + getSimpleClassName(lambdaExpressionType) + " as "
                            + getSimpleClassName(returnType));
                }
            }

            void box(MethodVisitor mv, Type type) {
                if (isReference(type))
                    return;

                Type boxed = getBoxedType(type);
                String descriptor = getMethodDescriptor(boxed, new Type[] { type });
                String name = "valueOf";
                mv.visitMethodInsn(INVOKESTATIC, boxed.getInternalName(), name, descriptor);
            }

            boolean isInSAMBody(int index) {
                return index >= getBodyStart() && index != getEnd();
            }

            void createLambdaConstructor() throws IOException {
                Type[] parameters = getConstructorParameters();
                String descriptor = getMethodDescriptor(VOID_TYPE, parameters);

                MethodVisitor mv = lambda.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
                mv.visitCode();

                createAndInitializeFieldsWithAccessedLocals(mv, parameters);

                invokeSuperConstructor(mv);

                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            void instantiate(MethodVisitor mv, LambdaAnalyzer parentLambda) {
                mv.visitTypeInsn(NEW, lambdaClass());
                mv.visitInsn(DUP);

                loadAccessedLocals(mv, parentLambda);
                String descriptor = getMethodDescriptor(VOID_TYPE, getConstructorParameters());
                mv.visitMethodInsn(INVOKESPECIAL, lambdaClass(), "<init>", descriptor);
            }

            void loadAccessedLocals(MethodVisitor mv, LambdaAnalyzer parentLambda) {
                for (FieldNode f : parentParameters.values()) {
                    Type type = getType(f.desc);
                    if (parametersMutableFromChildLambdas.contains(f.name))
                        type = toArrayType(type);

                    if (parentLambda.parameters.containsKey(f.name)) {
                        mv.visitVarInsn(type.getOpcode(ILOAD), parentLambda.getParameterRealLocalIndex(f.name));
                        if (isReference(type))
                            mv.visitTypeInsn(CHECKCAST, type.getInternalName());

                    } else {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, parentLambda.lambda.name, getParentParameterName(f.name), type
                                .getDescriptor());
                    }

                }
                for (LocalVariableNode local : locals.values()) {
                    Type type = getType(local.desc);
                    if (methodMutableLocals.containsKey(local.name))
                        type = toArrayType(type);

                    if (parentLambda != null) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, parentLambda.lambda.name, getFieldNameForLocal(local), type
                                .getDescriptor());

                    } else
                        mv.visitVarInsn(type.getOpcode(ILOAD), local.index);
                }
            }

            void invokeSuperConstructor(MethodVisitor mv) throws IOException {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, getSuperType().getInternalName(), "<init>", "()V");
            }

            void createAndInitializeFieldsWithAccessedLocals(MethodVisitor mv, Type[] parameters) {
                int i = 1;
                for (FieldNode f : parentParameters.values()) {
                    Type type = parameters[i - 1];
                    loadFieldFromArgument(mv, i++, f.name, getParentParameterName(f.name), type);
                }
                for (LocalVariableNode local : locals.values()) {
                    Type type = parameters[i - 1];
                    loadFieldFromArgument(mv, i++, local.name, getFieldNameForLocal(local), type);
                }
            }

            void loadFieldFromArgument(MethodVisitor mv, int i, String name, String fieldName, Type type) {
                FieldVisitor fieldVisitor = lambda.visitField(ACC_SYNTHETIC | ACC_PRIVATE | ACC_FINAL, fieldName,
                        type.getDescriptor(), null, null);
                addLambdaLocalAnnotation(name, fieldVisitor);
                fieldVisitor.visitEnd();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(type.getOpcode(ILOAD), i);
                mv.visitFieldInsn(PUTFIELD, lambdaClass(), fieldName, type.getDescriptor());
            }

            String getFieldNameForLocal(LocalVariableNode local) {
                return local.name + "$" + local.index;
            }

            void addLambdaLocalAnnotation(String local, FieldVisitor fieldVisitor) {
                AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation(
                        getDescriptor(LambdaLocal.class), true);
                annotationVisitor.visit("isReadOnly", !methodMutableLocals.containsKey(local));
                annotationVisitor.visit("name", local);
                annotationVisitor.visitEnd();
            }

            Type getSuperType() throws IOException {
                Type type = lambdaType;
                if (hasAccess(readClassNoCode(type.getInternalName()), ACC_INTERFACE))
                    type = getType(Object.class);
                return type;
            }

            String[] getLambdaInterfaces() throws IOException {
                Type type = lambdaType;
                if (hasAccess(readClassNoCode(type.getInternalName()), ACC_INTERFACE))
                    return new String[] { type.getInternalName() };
                return null;
            }

            Type[] getConstructorParameters() {
                List<Type> result = new ArrayList<Type>();
                for (FieldNode parentParameter : parentParameters.values()) {
                    Type type = getType(parentParameter.desc);
                    result.add(parametersMutableFromChildLambdas.contains(parentParameter.name) ? toArrayType(type)
                            : type);
                }

                for (String local : locals.keySet()) {
                    Type type = getLocalVariableType(local);
                    result.add(methodMutableLocals.containsKey(local) ? toArrayType(type) : type);
                }
                return result.toArray(new Type[0]);
            }

            String getTypedParametersString() {
                List<String> result = new ArrayList<String>();
                for (FieldNode parameter : parameters.values())
                    result.add(getSimpleClassName(getType(parameter.desc)) + " " + parameter.name);
                return toParameterString(result);
            }

            String toParameterString(Collection<?> parameters) {
                StringBuilder sb = new StringBuilder(parameters.toString());
                sb.setCharAt(0, '(');
                sb.setCharAt(sb.length() - 1, ')');
                return sb.toString();
            }

            String sourceAndLine() {
                return c.sourceFile != null ? "(" + c.sourceFile + ":" + line + ")" : "(Unknown Source)";
            }

            Type resolveLambdaType() {
                Type lambdaType = newLambdaMethod.getReturnType();
                if (getType(Object.class).equals(lambdaType) && m.instructions.size() > getEnd()) {
                    AbstractInsnNode n = m.instructions.get(getEnd() + 1);
                    if (n.getOpcode() == CHECKCAST)
                        lambdaType = getObjectType(((TypeInsnNode) n).desc);
                }
                return lambdaType;
            }

            void analyze() throws IOException {
                devDebug("lambda ================ " + getStart() + " -> " + getEnd());
                devDebugPrintInstructionHeader();
                resolveParentLambda();

                int childLambda = lambdas.indexOf(this) + 1;
                boolean inChildLambda = false;
                for (int i = getStart(); i < getEnd(); i++) {
                    AbstractInsnNode n = m.instructions.get(i);

                    devDebugPrintInstruction(i, i - getStart(), frames[i], n);
                    if (childLambda < lambdas.size() && i >= lambdas.get(childLambda).getStart()) {
                        inChildLambda = true;
                    }

                    if (childLambda < lambdas.size() && i > lambdas.get(childLambda).getEnd()) {
                        inChildLambda = false;
                        childLambda++;
                    }

                    int type = n.getType();
                    if (type == VAR_INSN)
                        methodLocalVariable((VarInsnNode) n);

                    if (type == METHOD_INSN) {
                        MethodInsnNode min = (MethodInsnNode) n;

                        if (min.owner.equals(c.name)) {
                            MethodNode m = findMethod(min);
                            if (hasAccess(m, ACC_PRIVATE))
                                methodsThatNeedStaticAccessMethod.put(m.name, m);
                        }

                    }
                    if (type == FIELD_INSN) {
                        FieldInsnNode fin = (FieldInsnNode) n;

                        if (isLambdaParameterField(fin) && !inChildLambda) {
                            lambdaParameter(fin);

                        } else if (fin.owner.equals(c.name)) {
                            FieldNode f = findField(fin);
                            if (hasAccess(f, ACC_PRIVATE))
                                fieldsThatNeedStaticAccessMethod.put(f.name, f);
                        }
                    }
                }

                devDebug("end =================== " + getStart() + " -> " + getEnd());
                int i = 0;
                for (int[] range : argumentRanges) {
                    i++;
                    String parameter;
                    if (i >= parameters.keySet().size())
                        parameter = "<missing>";
                    else
                        parameter = getParameter(i);
                    devDebug("    "
                            + (i == argumentRanges.size() - 1 ? "body: " : "argument " + parameter + ": ")
                            + range[0] + " -> " + range[1]);                    
                }
                devDebug("    type: " + lambdaType);
                devDebug("    class: " + lambdaClass());

                sam = findSAM(lambdaType);

                if (sam != null)
                    devDebug("    SAM is: " + sam);
                else
                    throw new IllegalStateException("Found no potential abstract method to override" + " at " + sourceAndLine());

                devDebug("    parameters: " + parameters.keySet());
                devDebug("    method parameter types: " + newLambdaParameterTypes);
                devDebug("    expression type: " + expressionType);
                devDebug("    mutable locals: " + getMutableLocals().keySet());
                devDebug("    final locals: " + locals.keySet());
                if (parent != null) {
                    devDebug("    parent: " + parent.getStart() + " -> " + parent.getEnd());
                }

                if (newLambdaParameterTypes.size() != parameters.size())
                    throw new IllegalStateException("Got " + parameters.keySet() + " as parameters need exactly "
                            + newLambdaParameterTypes.size()  + " at " + sourceAndLine());
            }

            void devDebugPrintInstructionHeader() {
                if (!devDebug)
                    return;
                out.print("index" + "\t");
                out.print("stack");
                out.println("");
            }

            void devDebugPrintInstruction(int index, int textIndex, Frame frame, AbstractInsnNode n) {
                if (!devDebug)
                    return;
                n.accept(devDebugAsm);
                out.print(index + "\t");
                out.print(frame + "\t\t");
                out.print(devDebugAsm.getText().get(textIndex));
            }

            void resolveParentLambda() {
                for (LambdaAnalyzer lambda : lambdas)
                    if (lambda.getEnd() > getEnd() && lambda.getStart() < getStart())
                        parent = lambda;
            }

            @SuppressWarnings("unchecked")
            Method findSAM(Type type) throws IOException {
                ClassNode cn = readClassNoCode(type.getInternalName());
                for (MethodNode mn : (List<MethodNode>) cn.methods)
                    if (hasAccess(mn, ACC_ABSTRACT)
                            && getArgumentTypes(mn.desc).length == newLambdaParameterTypes.size())
                        return new Method(mn.name, mn.desc);

                for (String anInterface : (List<String>) cn.interfaces) {
                    Method foundSam = findSAM(getObjectType(anInterface));
                    if (foundSam != null)
                        return foundSam;
                }

                if (cn.superName != null)
                    return findSAM(getObjectType(cn.superName));

                return null;
            }

            void lambdaParameter(FieldInsnNode fin) throws IOException {
                FieldNode f = findField(fin);

                if (!parameters.containsKey(f.name)) {
                    if (parameters.size() == newLambdaParameterTypes.size()) {
                        if (!resolveParameter(fin))
                            throw new IllegalStateException("Tried to define extra parameter, " + f.name
                                    + ", arity is " + newLambdaParameterTypes.size() + ", defined parameters are "
                                    + parameters.keySet() + " at " + sourceAndLine());
                        return;
                    }

                    Type argumentType = getType(fin.desc);

                    parameters.put(f.name, f);
                    parameterOwners.put(f.name, fin.owner);
                    boolean hasDefaultValue = fin.getOpcode() == PUTSTATIC;
                    if (hasDefaultValue)
                        parametersWithDefaultValue.add(f.name);

                    devDebug("  --  defined parameter "
                            + fin.name
                            + " "
                            + argumentType.getClassName()
                            + " as "
                            + parameters.size()
                            + " out of "
                            + newLambdaParameterTypes.size()
                            + (hasDefaultValue ? " (has default value starting at "
                                    + argumentRanges.get(getParameterIndex(f.name))[0] + ")" : ""));
                } else
                    devDebug("  --  accessed parameter " + fin.name + " " + getType(f.desc).getClassName() + " ("
                            + (fin.getOpcode() == PUTSTATIC ? "write" : "read") + ")");
            }

            boolean resolveParameter(FieldInsnNode fin) throws IOException {
                if (parameters.containsKey(fin.name)) {
                    if (fin.getOpcode() == PUTSTATIC)
                        parametersMutableFromChildLambdas.add(fin.name);
                    return true;
                }

                if (parent == null)
                    return false;

                if (fin.getOpcode() == PUTSTATIC)
                    parametersMutableFromChildLambdas.add(fin.name);
                parentParameters.put(fin.name, findField(fin));
                return parent.resolveParameter(fin);
            }

            void methodLocalVariable(VarInsnNode vin) {
                LocalVariableNode local = localVariable(vin);
                if (local == null)
                    throw new IllegalStateException("Debug information is needed to close over local variables or parameters, please recompile with -g.");
                
                locals.put(local.name, local);

                devDebug("  --  accessed var " + local.index + " " + local.name + " "
                        + getType(local.desc).getClassName() + " write: " + isStoreInstruction(vin));
            }

            String lambdaClass() {
                String lambdaClass = lambdaType.getInternalName();
                lambdaClass = lambdaClass.substring(lambdaClass.lastIndexOf("/") + 1, lambdaClass.length());
                return c.name + "$" + (line > 0 ? String.format("%04d_", line) : "") + lambdaClass + "_" + id;
            }

            List<Type> getParameterTypes() {
                List<Type> result = new ArrayList<Type>();
                for (FieldNode f : parameters.values())
                    result.add(getType(f.desc));
                return result;
            }

            Type getLocalVariableType(String local) {
                return getType(locals.get(local).desc);
            }

            boolean returnNeedsUnboxing() {
                return isReference(expressionType) && isPrimitive(sam.getReturnType());
            }

            boolean returnNeedsBoxing() {
                return isPrimitive(expressionType) && isReference(sam.getReturnType());
            }

            boolean parameterDefaultValueNeedsBoxing(String name) {
                return isPrimitive(getType(parameters.get(name).desc));
            }

            boolean parameterNeedsUnboxing(String name) {
                int index = getParameterIndex(name);
                Type samParameterType = sam.getArgumentTypes()[index];
                return isPrimitive(getParameterTypes().get(index)) && isReference(samParameterType);
            }

            boolean parameterNeedsBoxing(String name) {
                int index = getParameterIndex(name);
                Type samParameterType = sam.getArgumentTypes()[index];
                return isReference(getParameterTypes().get(index)) && isPrimitive(samParameterType);
            }

            int getParameterIndex(String name) {
                return new ArrayList<String>(parameters.keySet()).indexOf(name);
            }

            String getParameter(int index) {
                return new ArrayList<String>(parameters.keySet()).get(index);
            }

            boolean parameterNeedsNarrowConversionFromActualArgument(String name) {
                return parameterNarrowConversionOpcode(name) != -1;
            }

            int parameterNarrowConversionOpcode(String name) {
                int index = getParameterIndex(name);
                Type samParameterType = sam.getArgumentTypes()[index];
                Type parameterType = getParameterTypes().get(index);
                if (parameterType.equals(samParameterType))
                    return -1;

                if (DOUBLE_TYPE == samParameterType) {
                    switch (parameterType.getSort()) {
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        return D2I;
                    case Type.FLOAT:
                        return D2F;
                    case Type.LONG:
                        return D2L;
                    }
                } else if (LONG_TYPE == samParameterType)
                    return L2I;
                return -1;
            }

            Map<String, LocalVariableNode> getMutableLocals() {
                Map<String, LocalVariableNode> result = new HashMap<String, LocalVariableNode>(methodMutableLocals);
                result.keySet().retainAll(locals.keySet());
                return result;
            }

            int getStart() {
                return argumentRanges.get(0)[0];
            }

            int getEnd() {
                return argumentRanges.get(argumentRanges.size() - 1)[1] + 1;
            }

            int getBodyStart() {
                return argumentRanges.get(argumentRanges.size() - 1)[0];
            }
        }

        boolean isMethodParameter(LocalVariableNode local) {
            return local.index != 0 && local.index < method.getArgumentTypes().length;
        }

        boolean isNewLambdaMethod(MethodInsnNode mi) throws IOException {
            MethodNode m = findMethod(mi);
            if (m == null)
                return false;
            boolean hasAnnotation = hasAnnotation(m, newLambdaAnnotation);
            if (hasAnnotation) {
                if (hasAccess(m, ACC_STATIC))
                    return true;
                throw new IllegalStateException("Tried to call non static new lambda method " + m.name  + " at " + sourceAndLine());
            }
            return false;
        }

        boolean isLambdaParameterField(FieldInsnNode fi) throws IOException {
            FieldNode f = findField(fi);
            if (f == null)
                return false;
            boolean hasAnnotation = hasAnnotation(f, lambdaParameterAnnotation);
            if (hasAnnotation) {
                if (hasAccess(f, ACC_STATIC))
                    return true;
                throw new IllegalStateException("Tried to define non static lambda parameter " + f.name  + " at " + sourceAndLine());
            }
            return false;
        }

        String sourceAndLine() {
            return c.sourceFile != null ? "(" + c.sourceFile + ":" + line + ")" : "(Unknown Source)";
        }
    }

    Type toArrayType(Type type) {
        return getType("[" + type.getDescriptor());
    }

    boolean isReference(Type type) {
        return type.getSort() == OBJECT || type.getSort() == ARRAY;
    }

    boolean isPrimitive(Type type) {
        return !isReference(type);
    }

    boolean hasAccess(ClassNode c, int acc) {
        return (c.access & acc) != 0;
    }

    boolean hasAccess(MethodNode m, int acc) {
        return m != null && (m.access & acc) != 0;
    }

    boolean hasAccess(FieldNode f, int acc) {
        return f != null && (f.access & acc) != 0;
    }

    @SuppressWarnings("unchecked")
    MethodNode findMethod(MethodInsnNode mn) throws IOException {
        for (MethodNode m : (List<MethodNode>) readClassNoCode(mn.owner).methods)
            if (m.name.equals(mn.name) && m.desc.equals(mn.desc))
                return m;
        return null;
    }

    @SuppressWarnings("unchecked")
    FieldNode findField(FieldInsnNode fn) throws IOException {
        for (FieldNode f : (List<FieldNode>) readClassNoCode(fn.owner).fields)
            if (f.name.equals(fn.name) && f.desc.equals(fn.desc))
                return f;
        return null;
    }

    ClassNode readClassNoCode(String in) throws IOException {
        if (in.equals(c.name))
            return c;
        ClassNode cn = new ClassNode();
        String className = getObjectType(in).getClassName();
        if (isEnum(className))
            return cn;
        try {
            new ClassReader(className).accept(cn, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
        } catch (IOException ignore) {
        }
        return cn;
    }

    boolean isEnum(String className) {
        return className.endsWith("[]");
    }

    @SuppressWarnings("unchecked")
    boolean hasAnnotation(MemberNode mn, Type a) {
        if (mn.invisibleAnnotations == null)
            return false;
        for (AnnotationNode an : (List<AnnotationNode>) mn.invisibleAnnotations)
            if (getType(an.desc).equals(a))
                return true;
        return false;
    }

    String getSimpleClassName(Type type) {
        String name = type.getClassName();
        if (!name.contains("."))
            return name;
        return name.substring(name.lastIndexOf('.') + 1, name.length());
    }

    void devDebugAsm(MethodNode m) {
        if (!devDebug)
            return;
        ASMifierMethodVisitor asm = new ASMifierMethodVisitor();
        m.instructions.accept(asm);
        PrintWriter pw = new PrintWriter(out);
        asm.print(pw);
        pw.flush();
    }

    static Type getConfigurableAnnotationType(String property, String name) {
        return getType("L" + getProperty(property, name).replace('.', '/') + ";");
    }
}
