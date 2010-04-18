package lambda.weaving.tree;

import static java.lang.System.*;
import static java.util.Arrays.*;
import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    List<MethodAnalyzer> methods = new ArrayList<MethodAnalyzer>();

    Map<String, FieldNode> fieldsThatNeedStaticAccessMethod = new HashMap<String, FieldNode>();
    Map<String, MethodNode> methodsThatNeedStaticAccessMethod = new HashMap<String, MethodNode>();
    ClassReader cr;

    LambdaTreeWeaver(ClassReader cr) {
        this.cr = cr;
    }

    @SuppressWarnings("unchecked")
    LambdaTreeWeaver analyze() throws Exception {
        if (c != null)
            return this;

        c = new ClassNode();
        cr.accept(c, EXPAND_FRAMES);

        out.println(c.name);
        out.println();

        for (MethodNode m : (List<MethodNode>) c.methods) {
            MethodAnalyzer ma = new MethodAnalyzer(m);
            ma.analyze();
            methods.add(ma);
        }
        return this;
    }

    ClassNode transform() throws Exception {
        if (c == null)
            analyze();

        for (MethodAnalyzer ma : (methods))
            ma.transform();

        return c;
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
        Frame[] frames;

        List<LambdaAnalyzer> lambdas = new ArrayList<LambdaAnalyzer>();
        Map<String, LocalVariableNode> methodLocals = new LinkedHashMap<String, LocalVariableNode>();
        Map<String, LocalVariableNode> methodMutableLocals = new LinkedHashMap<String, LocalVariableNode>();

        MethodAnalyzer(MethodNode m) {
            this.m = m;
        }

        void transform() throws IOException {
            if (lambdas.isEmpty())
                return;

            int currentLambda = 0;

            InsnList instructions = m.instructions;

            m.instructions = new InsnList();
            initAccessedLocalsAndMethodArgumentsAsArrays();

            for (int i = 0; i < instructions.size(); i++) {
                AbstractInsnNode n = instructions.get(i);
                if (currentLambda < lambdas.size() && i == lambdas.get(currentLambda).start) {
                    LambdaAnalyzer lambda = lambdas.get(currentLambda);

                    lambda.transform(instructions);
                    lambda.instantiate(m);

                    i = lambdas.get(currentLambda).end;

                    currentLambda++;

                } else if (n.getType() == VAR_INSN) {
                    VarInsnNode vin = (VarInsnNode) n;
                    LocalVariableNode local = (LocalVariableNode) m.localVariables.get(vin.var);

                    if (methodMutableLocals.containsKey(local.name)) {
                        loadArrayFromLocal(local);

                        if (isStoreInstruction(vin))
                            storeTopOfStackInArray(getType(local.desc));

                        else
                            loadFirstElementOfArray(getType(local.desc));
                    }
                } else if (n.getType() == IINC_INSN) {
                    IincInsnNode iinc = (IincInsnNode) n;
                    LocalVariableNode local = (LocalVariableNode) m.localVariables.get(iinc.var);

                    if (methodMutableLocals.containsKey(local.name)) {
                        loadArrayFromLocal(local);
                        incrementInArray(m, iinc.incr);

                    } else {
                        n.accept(m);
                    }
                } else {
                    n.accept(m);
                }
            }

            out.println("after ================= ");
            print(m, new OutputStreamWriter(out));
        }

        void analyze() throws Exception {
            out.println(m.name + m.desc);
            StringWriter before = new StringWriter();
            print(m, before);

            frames = new Analyzer(new BasicInterpreter()).analyze(c.name, m);

            int line = 0;
            for (int i = 0; i < m.instructions.size(); i++) {
                AbstractInsnNode n = m.instructions.get(i);

                int type = n.getType();
                if (type == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode mi = (MethodInsnNode) n;

                    if (isNewLambdaMethod(mi)) {
                        int end = i;
                        int start = findInstructionAtStartOfLambda(end, getArgumentTypes(mi.desc).length);

                        LambdaAnalyzer lambda = new LambdaAnalyzer(currentLambdaId++, line, mi, start, end);
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
                out.println();
                out.println("before ================ ");
                out.println(before);
            }
        }

        int findInstructionAtStartOfLambda(int end, int arguments) {
            int depth = getStackSize(end) - arguments;
            for (int i = end - 1; i >= 0; i--) {
                if (getStackSize(i) == depth)
                    return i;
            }
            throw new IllegalStateException("Could not find previous stack depth of " + depth);
        }

        int getStackSize(int i) {
            return frames == null ? 0 : frames[i].getStackSize();
        }

        LocalVariableNode accessLocal(VarInsnNode vin) {
            if (vin.var >= m.localVariables.size())
                return null;
            LocalVariableNode local = (LocalVariableNode) m.localVariables.get(vin.var);

            if (isStoreInstruction(vin) && methodLocals.containsKey(local.name))
                methodMutableLocals.put(local.name, local);

            methodLocals.put(local.name, local);
            return local;
        }

        LocalVariableNode accessLocal(IincInsnNode iin) {
            if (iin.var >= m.localVariables.size())
                return null;
            LocalVariableNode local = (LocalVariableNode) m.localVariables.get(iin.var);
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

        void loadFirstElementOfArray(Type type) {
            m.visitInsn(ICONST_0);
            m.visitInsn(type.getOpcode(IALOAD));
        }

        void storeTopOfStackInArray(Type type) {
            // x is at the top of the stack, as it was to be stored
            // directly into a local
            // x a[]
            m.visitInsn(ICONST_0);
            // x a[] 0
            m.visitInsn(type.getSize() == 2 ? DUP2_X2 : DUP2_X1);
            // a[] 0 x a[] 0
            m.visitInsn(POP2);
            // a[] 0 x
            m.visitInsn(type.getOpcode(IASTORE));
        }

        void initAccessedLocalsAndMethodArgumentsAsArrays() {
            for (LocalVariableNode local : methodMutableLocals.values())
                initArray(local);
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

            Map<String, Integer> parametersWithDefaultValue = new HashMap<String, Integer>();
            Map<String, FieldNode> parameters = new LinkedHashMap<String, FieldNode>();

            Map<String, LocalVariableNode> locals = new LinkedHashMap<String, LocalVariableNode>();

            int bodyStart;
            int start;
            int end;
            Method sam;
            ClassNode lambda;
            MethodNode saMethod;

            LambdaAnalyzer(int id, int line, MethodInsnNode mi, int start, int end) {
                this.id = id;
                this.line = line;
                this.start = start;
                this.end = end;
                newLambdaMethod = new Method(mi.name, mi.desc);

                newLambdaParameterTypes = asList(newLambdaMethod.getArgumentTypes());
                newLambdaParameterTypes = newLambdaParameterTypes.subList(0, newLambdaParameterTypes.size() - 1);

                expressionType = newLambdaParameterTypes.isEmpty() ? getType(Object.class)
                        : newLambdaParameterTypes.get(newLambdaParameterTypes.size() - 1);

                lambdaType = resolveLambdaType();
            }

            void transform(InsnList insns) throws IOException {
                lambda = new ClassNode();
                lambda.visit(V1_5, ACC_FINAL | ACC_SYNTHETIC, lambdaClass(), null,
                        getSuperType().getInternalName(), getLambdaInterfaces());

                createLambdaConstructor();
                createSAMethod();

                for (int i = start; i <= end; i++) {
                    AbstractInsnNode n = insns.get(i);

                    if (isInSAMBody(i)) {
                        int type = n.getType();
                        if (type == VAR_INSN) {
                            VarInsnNode vin = (VarInsnNode) n;
                            LocalVariableNode local = (LocalVariableNode) m.localVariables.get(vin.var);
                            loadLambdaField(local);

                            if (methodMutableLocals.containsKey(local.name)) {
                                if (isStoreInstruction(vin))
                                    storeTopOfStackInArray(getType(local.desc));

                                else
                                    loadFirstElementOfArray(getType(local.desc));
                            }
                        } else if (type == FIELD_INSN) {
                            FieldInsnNode fin = (FieldInsnNode) n;
                            if (isLambdaParameterField(fin)) {
                                accessParameter(fin);

                            }
                        } else if (type == IINC_INSN) {
                            IincInsnNode iinc = (IincInsnNode) n;
                            LocalVariableNode local = (LocalVariableNode) m.localVariables.get(iinc.var);
                            loadLambdaField(local);
                            incrementInArray(m, iinc.incr);

                        } else {
                            n.accept(saMethod);
                        }
                    }
                }

                returnFromSAMethod();

                out.println("new lambda ================= ");
                lambda.accept(new ASMifierClassVisitor(new PrintWriter(out)));
            }

            void accessParameter(FieldInsnNode fin) {
                Type type = getType(fin.desc);
                int realLocalIndex = getParameterRealLocalIndex(fin.name);

                if (fin.getOpcode() == PUTFIELD) {
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

            void loadLambdaField(LocalVariableNode local) {
                saMethod.visitVarInsn(ALOAD, 0);
                saMethod.visitFieldInsn(GETFIELD, lambdaClass(), getFieldNameForLocal(local), local.desc);
            }

            void createSAMethod() {
                saMethod = (MethodNode) lambda.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, sam.getName(), sam
                        .getDescriptor(), null, null);
            }

            void returnFromSAMethod() {
                saMethod.visitInsn(sam.getReturnType().getOpcode(IRETURN));
                saMethod.visitMaxs(0, 0);
                saMethod.visitEnd();
            }

            boolean isInSAMBody(int index) {
                return index >= bodyStart && index != end;
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

            Type resolveLambdaType() {
                Type lambdaType = newLambdaMethod.getReturnType();
                if (getType(Object.class).equals(lambdaType) && m.instructions.size() > end) {
                    AbstractInsnNode n = m.instructions.get(end + 1);
                    if (n.getOpcode() == CHECKCAST)
                        lambdaType = getObjectType(((TypeInsnNode) n).desc);
                }
                return lambdaType;
            }

            void analyze() throws IOException {
                out.println("lambda ================ " + start + " -> " + end);
                out.print("index" + "\t");
                out.print("stack depth");
                out.println();

                ASMifierMethodVisitor asm = new ASMifierMethodVisitor();

                int lastParameterStart = start;
                for (int i = start; i <= end; i++) {
                    AbstractInsnNode n = m.instructions.get(i);

                    n.accept(asm);
                    printInstruction(i, i - start, getStackSize(i), asm);

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
                            lambdaParameter(lastParameterStart, fin);
                            lastParameterStart = i;

                        } else if (fin.owner.equals(c.name)) {
                            FieldNode f = findField(fin);
                            if (hasAccess(f, ACC_PRIVATE))
                                fieldsThatNeedStaticAccessMethod.put(f.name, f);
                        }
                    }
                }
                if (!parameters.isEmpty()) {
                    bodyStart = lastParameterStart + 1;
                    if (parameterHasConversionAtDefinition(getParameter(parameters.size() - 1)))
                        bodyStart++;
                } else
                    bodyStart = start;

                out.println("end =================== " + start + " -> " + end);
                out.println("    body starts at: " + bodyStart);
                out.println("    type: " + lambdaType);
                out.println("    class: " + lambdaClass());

                sam = findSAM(lambdaType);

                if (sam != null)
                    out.println("    SAM is: " + sam);
                else
                    throw new IllegalStateException("Found no potential abstract method to override");

                out.println("    parameters: " + parameters.keySet());
                out.println("    method parameter types: " + newLambdaParameterTypes);
                out.println("    expression type: " + expressionType);
                out.println("    mutable locals: " + getMutableLocals());
                out.println("    final locals: " + locals);

                if (newLambdaParameterTypes.size() != parameters.size())
                    throw new IllegalStateException("Got " + parameters.keySet() + " as parameters need exactly "
                            + newLambdaParameterTypes.size());
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

            void lambdaParameter(int lastParameterStart, FieldInsnNode fin) throws IOException {
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
                        parametersWithDefaultValue.put(f.name, lastParameterStart);

                    out.println("  --  defined parameter "
                            + fin.name
                            + " "
                            + argumentType.getClassName()
                            + " as "
                            + parameters.size()
                            + " out of "
                            + newLambdaParameterTypes.size()
                            + (hasDefaultValue ? " (has default value starting at " + lastParameterStart + ")" : ""));
                } else
                    out.println("  --  accessed parameter " + fin.name + " " + getType(f.desc).getClassName()
                            + " (" + (fin.getOpcode() == PUTSTATIC ? "write" : "read") + ")");
            }

            void localVariable(VarInsnNode vin) {
                LocalVariableNode local = accessLocal(vin);
                locals.put(local.name, local);

                out.println("  --  accessed var " + local.index + " " + local.name + " "
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

            boolean parameterHasConversionAtDefinition(String name) {
                int index = getParameterIndex(name);
                Type parameterType = getParameterTypes().get(index);
                Type methodParameterType = newLambdaParameterTypes.get(index);

                if (isPrimitive(parameterType) && isPrimitive(methodParameterType))
                    return !parameterType.equals(methodParameterType);

                return (isPrimitive(parameterType) && isReference(methodParameterType))
                        || (isReference(parameterType) && isPrimitive(methodParameterType));
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
        }
    }

    Type toArrayType(Type type) {
        return getType("[" + type.getDescriptor());
    }

    boolean isReference(Type type) {
        return type.getSort() == OBJECT || type.getSort() == ARRAY;
    }

    boolean isPrimitive(Type type) {
        return !isReference(type) && type.getSort() != VOID;
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
        return (m.access & acc) != 0;
    }

    boolean hasAccess(FieldNode f, int acc) {
        return (f.access & acc) != 0;
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

    void printInstruction(int index, int textIndex, int depth, ASMifierMethodVisitor asm) {
        out.print(index + "\t");
        out.print(depth + "\t\t");
        out.print(asm.getText().get(textIndex));
    }

    void print(MethodNode m, Writer w) {
        ASMifierMethodVisitor asm = new ASMifierMethodVisitor();
        m.instructions.accept(asm);
        PrintWriter pw = new PrintWriter(w);
        asm.print(pw);
        pw.flush();
    }
}
