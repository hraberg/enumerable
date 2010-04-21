package lambda.weaving.tree;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static lambda.exception.UncheckedException.*;
import static lambda.weaving.Debug.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lambda.annotation.LambdaLocal;
import lambda.annotation.LambdaParameter;
import lambda.annotation.NewLambda;
import lambda.weaving.tree.LambdaTreeWeaver.MethodAnalyzer.LambdaAnalyzer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MemberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.ASMifierMethodVisitor;

class LambdaTreeWeaver implements Opcodes {
    ClassNode c;
    int currentLambdaId = 1;
    int accessMethods = 1;
    List<MethodAnalyzer> methods = new ArrayList<MethodAnalyzer>();

    Map<String, FieldNode> fieldsThatNeedStaticAccessMethod = new HashMap<String, FieldNode>();
    Map<String, MethodNode> methodsThatNeedStaticAccessMethod = new HashMap<String, MethodNode>();
    ClassReader cr;

    LambdaTreeWeaver(ClassReader cr) {
        this.cr = cr;
    }

    @SuppressWarnings("unchecked")
    LambdaTreeWeaver analyze() {
        try {
            if (c != null)
                return this;

            c = new ClassNode();
            cr.accept(c, EXPAND_FRAMES);

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

        MethodAnalyzer(MethodNode m) {
            this.m = m;
            method = new Method(m.name, m.desc);
        }

        void transform() throws IOException {
            if (lambdas.isEmpty())
                return;

            debug("processing " + method);
            Collections.sort(lambdas, new Comparator<LambdaAnalyzer>() {
                public int compare(LambdaAnalyzer o1, LambdaAnalyzer o2) {
                    return o1.getStart() - o2.getStart();
                }
            });

            InsnList instructions = m.instructions;

            m.instructions = new InsnList();
            initAccessedLocalsAndMethodArgumentsAsArrays();

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

                    lambda.instantiate(m);

                } else if (n.getType() == VAR_INSN) {
                    VarInsnNode vin = (VarInsnNode) n;
                    LocalVariableNode local = getLocalVariable(vin.var);

                    if (local != null && getLocalsMutableFromLambdas().containsKey(local.name)) {
                        loadArrayFromLocal(local);

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
                        loadArrayFromLocal(local);
                        incrementInArray(m, iinc.incr);

                    } else {
                        n.accept(m);
                    }
                } else {
                    n.accept(m);
                }
            }
            m.visitMaxs(0, 0);

            devDebug("after ================= ");
            devDebugAsm(m);
        }

        LocalVariableNode getLocalVariable(int var) {
            if (var < m.localVariables.size())
                return (LocalVariableNode) m.localVariables.get(var);
            return null;
        }

        void analyze() throws Exception {
            frames = new Analyzer(new BasicInterpreter()).analyze(c.name, m);

            int line = 0;
            for (int i = 0; i < m.instructions.size(); i++) {
                AbstractInsnNode n = m.instructions.get(i);

                int type = n.getType();
                if (type == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode mi = (MethodInsnNode) n;

                    if (isNewLambdaMethod(mi)) {
                        int end = i;

                        List<int[]> argumentRanges = findArgumentInstructionRangesOfLambda(end,
                                getArgumentTypes(mi.desc).length);

                        LambdaAnalyzer lambda = new LambdaAnalyzer(currentLambdaId++, line, mi, argumentRanges);
                        lambda.analyze();
                        lambdas.add(lambda);
                    }
                }
                if (type == VAR_INSN)
                    accessLocal((VarInsnNode) n);

                if (type == IINC_INSN)
                    accessLocal((IincInsnNode) n);

                if (type == LINE)
                    line = ((LineNumberNode) n).line;
            }

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
                int start = findInstructionWithRelativeStackDepthOF(end, -1);
                argumentRanges.add(0, new int[] { start, end - 1 });
                end = start;
            }

            int i = 1;
            for (int[] range : argumentRanges)
                devDebug("argument: " + (i++) + " " + range[0] + " -> " + range[1]);

            return argumentRanges;
        }

        int findInstructionWithRelativeStackDepthOF(int index, int relativeDepth) {
            if (relativeDepth == 0)
                return index;
            int depth = getStackSize(index) + relativeDepth;
            for (int i = index - 1; i >= 0; i--) {
                if (getStackSize(i) == depth) {
                    if (i > 0) {
                        AbstractInsnNode n = m.instructions.get(i - 1);
                        if (n.getType() == LABEL)
                            return resolveBranches(i, (LabelNode) n);
                        if (n.getType() == JUMP_INSN)
                            continue;
                    }
                    return i;
                }
            }
            throw new IllegalStateException("Could not find previous stack depth of " + depth);
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
                            return findInstructionWithRelativeStackDepthOF(j, -1);
                        case IF_ICMPEQ:
                        case IF_ICMPNE:
                        case IF_ICMPLT:
                        case IF_ICMPGE:
                        case IF_ICMPGT:
                        case IF_ICMPLE:
                        case IF_ACMPEQ:
                        case IF_ACMPNE:
                            return findInstructionWithRelativeStackDepthOF(j, -2);
                        }
                    }
                }
            }
            return end;
        }

        int getStackSize(int i) {
            return frames[i] == null ? 0 : frames[i].getStackSize();
        }

        LocalVariableNode accessLocal(VarInsnNode vin) {
            LocalVariableNode local = getLocalVariable(vin.var);
            if (local == null)
                return null;

            if (isStoreInstruction(vin) && methodLocals.containsKey(local.name))
                methodMutableLocals.put(local.name, local);

            methodLocals.put(local.name, local);
            return local;
        }

        LocalVariableNode accessLocal(IincInsnNode iin) {
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

        void loadArrayFromLocal(LocalVariableNode local) {
            m.visitVarInsn(ALOAD, local.index);
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

        void initAccessedLocalsAndMethodArgumentsAsArrays() {
            Map<String, LocalVariableNode> localsMutableFromLambdas = getLocalsMutableFromLambdas();

            for (LocalVariableNode local : localsMutableFromLambdas.values())
                initArray(local);
        }

        Map<String, LocalVariableNode> getLocalsMutableFromLambdas() {
            Map<String, LocalVariableNode> localsMutableFromLambdas = new HashMap<String, LocalVariableNode>();

            for (LambdaAnalyzer lambda : lambdas)
                localsMutableFromLambdas.putAll(lambda.getMutableLocals());
            return localsMutableFromLambdas;
        }

        void initArray(LocalVariableNode local) {
            m.visitInsn(ICONST_1);
            newArray(getType(local.desc));

            if (isMethodArgument(local))
                initializeArrayOnTopOfStackWithCurrentValueOfLocal(local);

            m.visitVarInsn(ASTORE, local.index);
        }

        boolean isMethodArgument(LocalVariableNode local) {
            return local.index <= getArgumentTypes(m.desc).length;
        }

        void initializeArrayOnTopOfStackWithCurrentValueOfLocal(LocalVariableNode local) {
            Type type = getType(local.desc);
            // a[]
            m.visitInsn(DUP);
            // a[] a[]
            m.visitInsn(ICONST_0);
            // a[] a[] 0
            m.visitVarInsn(type.getOpcode(ILOAD), local.index);
            // a[] a[] 0 x
            m.visitInsn(type.getOpcode(IASTORE));
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
                m.visitTypeInsn(Opcodes.ANEWARRAY, type.getInternalName());
                return;
            }
            m.visitIntInsn(Opcodes.NEWARRAY, arrayType);
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
            int id;
            int line;

            Method newLambdaMethod;
            List<Type> newLambdaParameterTypes;

            Type lambdaType;
            Type expressionType;

            Set<String> parametersWithDefaultValue = new HashSet<String>();
            Map<String, FieldNode> parameters = new LinkedHashMap<String, FieldNode>();

            Map<String, LocalVariableNode> locals = new LinkedHashMap<String, LocalVariableNode>();

            List<int[]> argumentRanges;

            Method sam;
            ClassNode lambda;
            MethodNode saMethod;

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
                lambda.visit(V1_5, ACC_FINAL | ACC_SYNTHETIC, lambdaClass(), null,
                        getSuperType().getInternalName(), getLambdaInterfaces());

                createLambdaConstructor();
                createSAMethod();

                for (String parameter : parametersWithDefaultValue) {
                    int[] range = argumentRanges.get(getParameterIndex(parameter));
                    captureDefaultParameterValue(parameter, range[0], range[1], instructions);
                }

                for (int i = getBodyStart(); i < getEnd(); i++) {
                    AbstractInsnNode n = instructions.get(i);

                    if (currentLambda < lambdas.size() && i == lambdas.get(currentLambda).getStart()) {
                        LambdaAnalyzer lambda = lambdas.get(currentLambda);

                        i = lambdas.get(currentLambda).getEnd();
                        currentLambda++;

                        lambda.transform(instructions);
                        lambda.instantiate(saMethod);
                    }

                    if (isInSAMBody(i))
                        handleInsnNodeInSAM(n);
                }

                returnFromSAMethod();

                lambda.visitOuterClass(c.name, m.name, m.desc);
                lambda.visitEnd();

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

            void handleInsnNodeInSAM(AbstractInsnNode n) throws IOException {
                int type = n.getType();
                if (type == VAR_INSN) {
                    VarInsnNode vin = (VarInsnNode) n;
                    LocalVariableNode local = getLocalVariable(vin.var);

                    debugLocalVariableAccess(local, isStoreInstruction(vin));

                    if (local == null) {
                        n.accept(saMethod);

                    } else if (getLocalsMutableFromLambdas().containsKey(local.name)) {
                        loadLambdaField(local, toArrayType(getType(local.desc)));
                        if (isStoreInstruction(vin))
                            storeTopOfStackInArray(saMethod, getType(local.desc));

                        else
                            loadFirstElementOfArray(saMethod, getType(local.desc));
                    } else {
                        loadLambdaField(local, getType(local.desc));

                    }

                } else if (type == FIELD_INSN) {
                    FieldInsnNode fin = (FieldInsnNode) n;
                    if (isLambdaParameterField(fin))
                        accessParameter(fin);

                    else if (isPrivateFieldOnOwnerWhichNeedsAcccessMethodFromLambda(fin))
                        createAndCallStaticAccessMethodToReplacePrivateFieldAccess(fin);

                    else
                        n.accept(saMethod);

                } else if (type == IINC_INSN) {
                    IincInsnNode iinc = (IincInsnNode) n;
                    LocalVariableNode local = getLocalVariable(iinc.var);
                    if (local != null) {
                        loadLambdaField(local, toArrayType(getType(local.desc)));
                        incrementInArray(saMethod, iinc.incr);
                    }

                } else if (type == METHOD_INSN) {
                    MethodInsnNode mn = (MethodInsnNode) n;

                    if (isPrivateMethodOnOwnerWhichNeedsAcccessMethodFromLambda(mn))
                        createAndCallStaticAccessMethodToReplacePrivateMethodInvocation(mn);

                    else
                        n.accept(saMethod);

                } else {
                    n.accept(saMethod);
                }
            }

            void createAndCallStaticAccessMethodToReplacePrivateFieldAccess(FieldInsnNode fn) {
                List<Type> argumentTypes = new ArrayList<Type>();

                int opcode = fn.getOpcode();
                if (opcode == GETFIELD || opcode == PUTFIELD)
                    argumentTypes.add(getObjectType(fn.owner));

                if (opcode == PUTFIELD || opcode == PUTSTATIC)
                    argumentTypes.add(getType(fn.desc));

                Type returnType = getType(fn.desc);
                MethodVisitor mv = createAndCallAccessMethodAndLoadArguments(fn.owner, argumentTypes, returnType);

                fn.accept(mv);

                if (opcode == PUTFIELD || opcode == PUTSTATIC) {
                    int indexOfNewFieldValue = argumentTypes.size() - 1;
                    mv.visitVarInsn(argumentTypes.get(indexOfNewFieldValue).getOpcode(ILOAD), indexOfNewFieldValue);
                }

                mv.visitInsn(returnType.getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
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

            void createAndCallStaticAccessMethodToReplacePrivateMethodInvocation(MethodInsnNode mn) {
                List<Type> argumentTypes = new ArrayList<Type>();

                int opcode = mn.getOpcode();
                if (opcode != INVOKESTATIC)
                    argumentTypes.add(getObjectType(mn.owner));

                for (Type type : getArgumentTypes(mn.desc))
                    argumentTypes.add(type);

                MethodVisitor mv = createAndCallAccessMethodAndLoadArguments(mn.owner, argumentTypes,
                        getReturnType(mn.desc));

                mn.accept(mv);

                mv.visitInsn(getReturnType(mn.desc).getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            MethodVisitor createAndCallAccessMethodAndLoadArguments(String owner, List<Type> argumentTypes,
                    Type returnType) {
                String accessMethodDescriptor = getMethodDescriptor(returnType, argumentTypes.toArray(new Type[0]));
                String accessMethodName = "access$lambda$" + String.format("%03d_", accessMethods++);

                saMethod.visitMethodInsn(INVOKESTATIC, owner, accessMethodName, accessMethodDescriptor);

                MethodVisitor mv = c.visitMethod(ACC_STATIC + ACC_SYNTHETIC, accessMethodName,
                        accessMethodDescriptor, null, null);
                mv.visitCode();

                int i = 0;
                for (Type type : argumentTypes)
                    mv.visitVarInsn(type.getOpcode(ILOAD), i++);

                return mv;
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

                MethodNode original = saMethod;
                saMethod = mv;
                for (int i = start; i < end; i++)
                    handleInsnNodeInSAM(insns.get(i));
                saMethod = original;

                Type returnType = isReference(defaultParameterValueType) ? defaultParameterValueType
                        : getBoxedType(defaultParameterValueType);

                if (isPrimitive(defaultParameterValueType))
                    box(mv, defaultParameterValueType);

                mv.visitInsn(returnType.getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            void accessParameter(FieldInsnNode fin) {
                Type type = getType(fin.desc);
                int realLocalIndex = getParameterRealLocalIndex(fin.name);

                debugLambdaParameterAccess(fin, realLocalIndex);

                if (fin.getOpcode() == PUTSTATIC) {
                    saMethod.visitVarInsn(type.getOpcode(ISTORE), realLocalIndex);

                } else {

                    saMethod.visitVarInsn(type.getOpcode(ILOAD), realLocalIndex);
                    if (isReference(type))
                        saMethod.visitTypeInsn(CHECKCAST, type.getInternalName());
                }
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

            void loadLambdaField(LocalVariableNode local, Type type) {
                saMethod.visitVarInsn(ALOAD, 0);
                saMethod.visitFieldInsn(GETFIELD, lambdaClass(), getFieldNameForLocal(local), type.getDescriptor());
            }

            void createSAMethod() {
                saMethod = (MethodNode) lambda.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, sam.getName(), sam
                        .getDescriptor(), null, null);
                saMethod.visitCode();

                convertMethodArgumentsToLambdaParameterTypes();
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
                    unboxLocal(localIndex, lambdaParameterType);

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
                saMethod.visitVarInsn(from.getOpcode(ILOAD), local);
                saMethod.visitInsn(opcode);
                saMethod.visitVarInsn(to.getOpcode(ISTORE), local);
            }

            void unboxLocal(int local, Type type) {
                saMethod.visitVarInsn(ALOAD, local);
                unbox(type);
                saMethod.visitVarInsn(type.getOpcode(ISTORE), local);
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

                saMethod.visitTypeInsn(CHECKCAST, type.getInternalName());
                saMethod.visitMethodInsn(INVOKEVIRTUAL, type.getInternalName(), name, descriptor);
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
                    saMethod.visitInsn(Opcodes.ACONST_NULL);

                handleBoxingAndUnboxingOfReturnFromLambda(sam.getReturnType(), expressionType);
                saMethod.visitInsn(sam.getReturnType().getOpcode(IRETURN));
                saMethod.visitMaxs(0, 0);
                saMethod.visitEnd();
            }

            void handleBoxingAndUnboxingOfReturnFromLambda(Type returnType, Type lambdaExpressionType) {
                if (isPrimitive(returnType) && isReference(lambdaExpressionType)) {
                    unbox(returnType);
                    debug("unboxed return value with type " + getSimpleClassName(lambdaExpressionType) + " as "
                            + getSimpleClassName(returnType));
                }
                if (isReference(returnType) && isPrimitive(lambdaExpressionType)) {
                    box(saMethod, lambdaExpressionType);
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

            void instantiate(MethodVisitor mv) {
                mv.visitTypeInsn(NEW, lambdaClass());
                mv.visitInsn(DUP);

                loadAccessedLocals(mv);
                String descriptor = getMethodDescriptor(VOID_TYPE, getConstructorParameters());
                mv.visitMethodInsn(INVOKESPECIAL, lambdaClass(), "<init>", descriptor);
            }

            void loadAccessedLocals(MethodVisitor mv) {
                for (LocalVariableNode local : locals.values()) {
                    Type type = getType(local.desc);
                    if (methodMutableLocals.containsKey(local.name))
                        type = toArrayType(type);
                    mv.visitVarInsn(type.getOpcode(ILOAD), local.index);
                }
            }

            void invokeSuperConstructor(MethodVisitor mv) throws IOException {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, getSuperType().getInternalName(), "<init>", "()V");
            }

            void createAndInitializeFieldsWithAccessedLocals(MethodVisitor mv, Type[] parameters) {
                int i = 1;
                for (LocalVariableNode local : locals.values()) {
                    String field = getFieldNameForLocal(local);
                    Type type = parameters[i - 1];

                    FieldVisitor fieldVisitor = lambda.visitField(ACC_SYNTHETIC | ACC_PRIVATE | ACC_FINAL, field,
                            type.getDescriptor(), null, null);
                    addLambdaLocalAnnotation(local.name, fieldVisitor);
                    fieldVisitor.visitEnd();

                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(type.getOpcode(ILOAD), i++);
                    mv.visitFieldInsn(PUTFIELD, lambdaClass(), field, type.getDescriptor());
                }
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
                StringBuffer sb = new StringBuffer(parameters.toString());
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

                for (int i = getStart(); i < getEnd(); i++) {
                    AbstractInsnNode n = m.instructions.get(i);

                    devDebugPrintInstruction(i, i - getStart(), frames[i], n);

                    int type = n.getType();
                    if (type == VAR_INSN)
                        localVariable((VarInsnNode) n);

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

                        if (isLambdaParameterField(fin)) {
                            lambdaParameter(i, fin);

                        } else if (fin.owner.equals(c.name)) {
                            FieldNode f = findField(fin);
                            if (hasAccess(f, ACC_PRIVATE))
                                fieldsThatNeedStaticAccessMethod.put(f.name, f);
                        }
                    }
                }

                devDebug("end =================== " + getStart() + " -> " + getEnd());
                devDebug("    body starts at: " + getBodyStart());
                devDebug("    type: " + lambdaType);
                devDebug("    class: " + lambdaClass());

                sam = findSAM(lambdaType);

                if (sam != null)
                    devDebug("    SAM is: " + sam);
                else
                    throw new IllegalStateException("Found no potential abstract method to override");

                devDebug("    parameters: " + parameters.keySet());
                devDebug("    method parameter types: " + newLambdaParameterTypes);
                devDebug("    expression type: " + expressionType);
                devDebug("    mutable locals: " + getMutableLocals().keySet());
                devDebug("    final locals: " + locals.keySet());

                if (newLambdaParameterTypes.size() != parameters.size())
                    throw new IllegalStateException("Got " + parameters.keySet() + " as parameters need exactly "
                            + newLambdaParameterTypes.size());
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

            @SuppressWarnings("unchecked")
            Method findSAM(Type type) throws IOException {
                ClassNode cn = readClassNoCode(type.getInternalName());
                for (MethodNode mn : (List<MethodNode>) cn.methods)
                    if (hasAccess(mn, ACC_ABSTRACT)
                            && getArgumentTypes(mn.desc).length == newLambdaParameterTypes.size())
                        return new Method(mn.name, mn.desc);

                for (String anInterface : (List<String>) cn.interfaces)
                    return findSAM(getObjectType(anInterface));

                if (cn.superName != null)
                    return findSAM(getObjectType(cn.superName));

                return null;
            }

            void lambdaParameter(int i, FieldInsnNode fin) throws IOException {
                FieldNode f = findField(fin);

                if (!parameters.containsKey(f.name)) {
                    if (parameters.size() == newLambdaParameterTypes.size())
                        throw new IllegalStateException("Tried to define extra parameter, " + f.name
                                + ", arity is " + newLambdaParameterTypes.size() + ", defined parameters are "
                                + parameters.keySet());

                    Type argumentType = getType(fin.desc);

                    parameters.put(f.name, f);
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

            void localVariable(VarInsnNode vin) {
                LocalVariableNode local = accessLocal(vin);
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

            boolean skipImplicitWideningPrimitiveConversionAtLambdaParameterDefinition(Type methodParameterType,
                    Type lambdaParameterType) {
                if (DOUBLE_TYPE == methodParameterType)
                    switch (lambdaParameterType.getSort()) {
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        return true;
                    case Type.FLOAT:
                        return true;
                    case Type.LONG:
                        return true;
                    }
                else if (LONG_TYPE == methodParameterType)
                    return true;

                return false;
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

    boolean isNewLambdaMethod(MethodInsnNode mi) throws IOException {
        MethodNode m = findMethod(mi);
        if (m == null)
            return false;
        return hasAnnotation(m, NewLambda.class) && hasAccess(m, ACC_STATIC);
    }

    boolean isLambdaParameterField(FieldInsnNode fi) throws IOException {
        FieldNode f = findField(fi);
        if (f == null)
            return false;
        return hasAnnotation(f, LambdaParameter.class) && hasAccess(f, ACC_STATIC);
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
        new ClassReader(getObjectType(in).getClassName()).accept(cn, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
        return cn;
    }

    @SuppressWarnings("unchecked")
    boolean hasAnnotation(MemberNode mn, Class<? extends Annotation> a) {
        if (mn.invisibleAnnotations == null)
            return false;
        for (AnnotationNode an : (List<AnnotationNode>) mn.invisibleAnnotations)
            if (getType(an.desc).equals(getType(a)))
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
}
