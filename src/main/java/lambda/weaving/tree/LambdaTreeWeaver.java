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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import lambda.annotation.LambdaParameter;
import lambda.annotation.NewLambda;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MemberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.ASMifierMethodVisitor;

public class LambdaTreeWeaver implements Opcodes {
    ClassNode c;
    int currentLambdaId;
    List<MethodAnalyzer> methods = new ArrayList<MethodAnalyzer>();

    @SuppressWarnings("unchecked")
    public ClassNode transform(ClassReader cr) throws Exception {
        c = new ClassNode();
        cr.accept(c, EXPAND_FRAMES);

        out.println(c.name);
        out.println();

        currentLambdaId = 1;
        for (MethodNode m : (List<MethodNode>) c.methods) {
            MethodAnalyzer ma = new MethodAnalyzer(m);
            ma.analyze();
            methods.add(ma);
        }
        return c;
    }

    class MethodAnalyzer {
        MethodNode m;
        List<Integer> stackDepth;
        int line;
        List<LambdaAnalyzer> lambdas = new ArrayList<LambdaAnalyzer>();
        Map<String, LocalVariableNode> methodLocals = new LinkedHashMap<String, LocalVariableNode>();
        Map<String, LocalVariableNode> methodMutableLocals = new LinkedHashMap<String, LocalVariableNode>();

        MethodAnalyzer(MethodNode m) {
            this.m = m;
        }

        void analyze() throws IOException {
            out.println(m.name + m.desc);
            StringWriter before = new StringWriter();
            print(m, before);
            stackDepth = new ArrayList<Integer>();

            AnalyzerAdapter analyzerAdapter = new AnalyzerAdapter(c.name, m.access, m.name, m.desc,
                    new EmptyVisitor());

            for (int i = 0; i < m.instructions.size(); i++) {
                AbstractInsnNode n = m.instructions.get(i);
                n.accept(analyzerAdapter);
                stackDepth.add(analyzerAdapter.stack == null ? 0 : analyzerAdapter.stack.size());

                int type = n.getType();
                if (type == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode mi = (MethodInsnNode) n;

                    if (isNewLambdaMethod(mi)) {
                        int end = i;
                        int start = findInstructionAtStartOfLambda(end);

                        LambdaAnalyzer lambda = new LambdaAnalyzer(currentLambdaId++, mi, start, end);
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
                out.println("after ================= ");
                print(m, new OutputStreamWriter(out));
            }
        }

        int findInstructionAtStartOfLambda(int end) {
            int depth = stackDepth.get(end);
            for (int i = end - 1; i >= 0; i--)
                if (stackDepth.get(i) == depth - 1)
                    return i;
            throw new IllegalStateException("Could not find previous stack depth of " + depth);
        }

        LocalVariableNode accessLocal(VarInsnNode vin) {
            if (vin.var >= m.localVariables.size())
                return null;
            LocalVariableNode local = (LocalVariableNode) m.localVariables.get(vin.var);

            if (vin.getOpcode() == getType(local.desc).getOpcode(ISTORE) && methodLocals.containsKey(local.name))
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

        class LambdaAnalyzer {
            int id;
            Method newLambdaMethod;

            List<Type> methodParameterTypes;
            Type lambdaType;
            Type expressionType;

            Map<String, FieldNode> parameters = new LinkedHashMap<String, FieldNode>();
            Map<String, LocalVariableNode> locals = new LinkedHashMap<String, LocalVariableNode>();
            Map<String, LocalVariableNode> mutableLocals = MethodAnalyzer.this.methodMutableLocals;

            int start;
            int end;
            Method sam;

            LambdaAnalyzer(int id, MethodInsnNode mi, int start, int end) {
                this.id = id;
                this.start = start;
                this.end = end;
                newLambdaMethod = new Method(mi.name, mi.desc);

                methodParameterTypes = asList(newLambdaMethod.getArgumentTypes());
                methodParameterTypes = methodParameterTypes.subList(0, methodParameterTypes.size() - 1);

                expressionType = methodParameterTypes.isEmpty() ? getType(Object.class) : methodParameterTypes
                        .get(methodParameterTypes.size() - 1);

                lambdaType = resolveLambdaType();
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
                    printInstruction(i, stackDepth.get(i), asm);

                    int type = n.getType();
                    if (type == VAR_INSN)
                        localVariable((VarInsnNode) n);

                    if (type == FIELD_INSN) {
                        lambdaParameter(lastParameterStart, (FieldInsnNode) n);
                        lastParameterStart = i;
                    }
                }

                out.println("end =================== " + start + " -> " + end);
                out.println("    body starts at: " + lastParameterStart);
                out.println("    type: " + lambdaType);
                out.println("    class: " + lambdaClass());

                sam = findSAM(lambdaType);

                if (sam != null)
                    out.println("    SAM is: " + sam);
                else
                    throw new IllegalStateException("Found no potential abstract method to override");

                out.println("    parameters: " + parameters.keySet());
                out.println("    method parameter types: " + methodParameterTypes);
                out.println("    expression type: " + expressionType);
                out.println("    mutable locals: " + mutableLocals);
                out.println("    final locals: " + locals);

                if (methodParameterTypes.size() != parameters.size())
                    throw new IllegalStateException("Got " + parameters.keySet() + " as parameters need exactly "
                            + methodParameterTypes.size());
            }

            @SuppressWarnings("unchecked")
            void apply() {
                ListIterator<AbstractInsnNode> li = m.instructions.iterator(start);
                for (int i = start; i <= end; i++) {
                    li.next();
                    li.remove();
                }
            }

            @SuppressWarnings("unchecked")
            Method findSAM(Type type) throws IOException {
                ClassNode cn = readClassNoCode(type.getInternalName());
                for (MethodNode mn : (List<MethodNode>) cn.methods)
                    if (hasAccess(mn, ACC_ABSTRACT)
                            && getArgumentTypes(mn.desc).length == methodParameterTypes.size())
                        return new Method(mn.name, mn.desc);

                for (String anInterface : (List<String>) cn.interfaces)
                    return findSAM(getObjectType(anInterface));

                if (cn.superName != null)
                    return findSAM(getObjectType(cn.superName));

                return null;
            }

            void lambdaParameter(int lastParameterStart, FieldInsnNode fin) throws IOException {
                if (isLambdaParameterField(fin)) {
                    FieldNode f = findField(fin);

                    if (!parameters.containsKey(f.name)) {
                        if (parameters.size() == methodParameterTypes.size())
                            throw new IllegalStateException("Tried to define extra parameter, " + f.name
                                    + ", arity is " + methodParameterTypes.size() + ", defined parameters are "
                                    + parameters.keySet());

                        Type argumentType = getType(fin.desc);
                        parameters.put(f.name, f);

                        out.println("  --  defined parameter "
                                + fin.name
                                + " "
                                + argumentType.getClassName()
                                + " as "
                                + parameters.size()
                                + " out of "
                                + methodParameterTypes.size()
                                + (fin.getOpcode() == PUTSTATIC ? " (has default value starting at "
                                        + lastParameterStart + ")" : ""));
                    } else
                        out.println("  --  accessed parameter " + fin.name + " " + getType(f.desc).getClassName()
                                + " (" + (fin.getOpcode() == PUTSTATIC ? "write" : "read") + ")");
                }
            }

            void localVariable(VarInsnNode vin) {
                LocalVariableNode local = accessLocal(vin);
                locals.put(local.name, local);

                out.println("  --  accessed var " + local.index + " " + local.name + " "
                        + getType(local.desc).getClassName() + " write: " + (1));
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

            void printInstruction(int index, int depth, ASMifierMethodVisitor asm) {
                out.print(index + "\t");
                out.print(depth + "\t\t");
                out.print(asm.getText().get(index - start));
            }
        }
    }

    boolean isNewLambdaMethod(MethodInsnNode mi) throws IOException {
        MethodNode m = findMethod(mi);
        return hasAnnotation(m, NewLambda.class) && hasAccess(m, ACC_STATIC);
    }

    boolean isLambdaParameterField(FieldInsnNode fi) throws IOException {
        FieldNode f = findField(fi);
        return hasAnnotation(f, LambdaParameter.class) && hasAccess(f, ACC_STATIC);
    }

    boolean hasAccess(MethodNode m, int acc) {
        return (m.access & acc) != 0;
    }

    boolean hasAccess(FieldNode m, int acc) {
        return (m.access & acc) != 0;
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
        ClassNode classNode = new ClassNode();
        new ClassReader(getObjectType(in).getClassName()).accept(classNode, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
        return classNode;
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

    void print(MethodNode m, Writer w) {
        ASMifierMethodVisitor asm = new ASMifierMethodVisitor();
        m.instructions.accept(asm);
        PrintWriter pw = new PrintWriter(w);
        asm.print(pw);
        pw.flush();
    }
}
