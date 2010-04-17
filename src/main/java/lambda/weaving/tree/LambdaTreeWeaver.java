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
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MemberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.ASMifierMethodVisitor;

public class LambdaTreeWeaver implements Opcodes {
    int currentLambdaId;

    @SuppressWarnings("unchecked")
    public ClassNode transform(ClassReader cr) throws Exception {
        ClassNode cn = new ClassNode();
        cr.accept(cn, EXPAND_FRAMES);

        out.println(cn.name);
        out.println();

        currentLambdaId = 1;
        for (MethodNode m : (List<MethodNode>) cn.methods)
            method(cn, m);
        return cn;
    }

    void method(ClassNode c, MethodNode m) throws IOException {
        out.println(m.name + m.desc);
        StringWriter before = new StringWriter();
        print(m, before);

        AnalyzerAdapter analyzerAdapter = new AnalyzerAdapter(c.name, m.access, m.name, m.desc, new EmptyVisitor());

        List<Integer> stackDepth = new ArrayList<Integer>();
        boolean hasLambdas = false;
        for (int i = 0; i < m.instructions.size(); i++) {
            AbstractInsnNode node = m.instructions.get(i);
            node.accept(analyzerAdapter);
            stackDepth.add(analyzerAdapter.stack == null ? 0 : analyzerAdapter.stack.size());

            if (m.instructions.get(i).getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mi = (MethodInsnNode) node;

                if (isNewLambdaMethod(mi)) {
                    hasLambdas = true;
                    int end = i;
                    int start = findInstructionAtStartOfLambda(stackDepth, end);

                    new LambdaDef(currentLambdaId++, c, m, mi).lambda(stackDepth, end, start);
                }
            }
        }
        if (hasLambdas) {
            out.println();
            out.println("before ================ ");
            out.println(before);
            out.println("after ================= ");
            print(m, new OutputStreamWriter(out));
        }
    }

    void print(MethodNode m, Writer w) {
        ASMifierMethodVisitor asm = new ASMifierMethodVisitor();
        m.instructions.accept(asm);
        PrintWriter pw = new PrintWriter(w);
        asm.print(pw);
        pw.flush();
    }

    class LambdaDef {
        int id;
        MethodNode m;
        Method nlm;

        List<Type> methodParameterTypes;
        Type lambdaType;
        Type expressionType;
        List<Type> argumentTypes;

        Map<String, FieldNode> parameters;
        Map<String, LocalVariableNode> locals;
        Map<String, LocalVariableNode> mutableLocals;
        ClassNode c;
        int line;

        LambdaDef(int id, ClassNode c, MethodNode m, MethodInsnNode mi) {
            this.id = id;
            this.c = c;
            this.m = m;
            nlm = new Method(mi.name, mi.desc);

            methodParameterTypes = asList(nlm.getArgumentTypes());

            if (methodParameterTypes.size() > 1)
                methodParameterTypes = methodParameterTypes.subList(0, methodParameterTypes.size() - 1);
            else
                methodParameterTypes = new ArrayList<Type>();

            lambdaType = nlm.getReturnType();

            expressionType = methodParameterTypes.isEmpty() ? getType(Object.class) : methodParameterTypes
                    .get(methodParameterTypes.size() - 1);

            argumentTypes = new ArrayList<Type>();
            parameters = new LinkedHashMap<String, FieldNode>();

            locals = new LinkedHashMap<String, LocalVariableNode>();
            mutableLocals = new LinkedHashMap<String, LocalVariableNode>();

        }

        String lambdaClass() {
            String lambdaClass = lambdaType.getInternalName();
            lambdaClass = lambdaClass.substring(lambdaClass.lastIndexOf("/") + 1, lambdaClass.length());
            return c.name + "$" + (line > 0 ? String.format("%04d_", line) : "") + lambdaClass + "_" + id;
        }

        @SuppressWarnings("unchecked")
        void lambda(List<Integer> stackDepth, int end, int start) throws IOException {
            out.println("lambda ================ " + start + " -> " + end);
            out.print("index" + "\t");
            out.print("stack depth");
            out.println();

            ASMifierMethodVisitor asMifierMethodVisitor = new ASMifierMethodVisitor();
            List<String> text = asMifierMethodVisitor.getText();

            int lastParameterStart = start;
            for (int i = start; i <= end; i++) {
                AbstractInsnNode n = m.instructions.get(i);

                n.accept(asMifierMethodVisitor);
                printInstruction(i, stackDepth.get(i), text.get(text.size() - 1));

                if (n.getType() == LINE)
                    line = ((LineNumberNode) n).line;

                if (n.getType() == VAR_INSN)
                    localVariable((VarInsnNode) n);

                if (n.getType() == FIELD_INSN) {
                    lambdaParameter(lastParameterStart, (FieldInsnNode) n);
                    lastParameterStart = i;
                }
            }

            ListIterator<AbstractInsnNode> li = m.instructions.iterator(start);
            for (int i = start; i <= end; i++) {
                li.next();
                li.remove();
            }

            out.println("end =================== " + start + " -> " + end);
            out.println("    body starts at: " + lastParameterStart);
            out.println("    type: " + lambdaType);
            out.println("    class: " + lambdaClass());

            List<MethodNode> sams = findPotentialSAMs();

            if (sams.size() == 1)
                out.println("    SAM is: " + toString(sams).get(0));
            else
                for (String mn : toString(sams))
                    out.println(" -- potential SAM is: " + mn);

            out.println("    parameters: " + parameters.keySet());
            out.println("    method parameter types: " + methodParameterTypes);
            out.println("    argument types: " + argumentTypes);
            out.println("    expression type: " + expressionType);
            out.println("    mutable locals: " + mutableLocals);
            out.println("    final locals: " + locals);

            if (sams.size() > 1) {
                throw new IllegalStateException("Found more than one potential abstract method to override: "
                        + toString(sams));
            }
            if (sams.isEmpty())
                throw new IllegalStateException("Found no potential abstract method to override");
        }

        List<String> toString(List<MethodNode> ms) {
            List<String> result = new ArrayList<String>();
            for (MethodNode m : ms)
                result.add(m.name + m.desc);
            return result;
        }

        @SuppressWarnings("unchecked")
        List<MethodNode> findPotentialSAMs() throws IOException {
            List<MethodNode> sams = new ArrayList<MethodNode>();
            for (MethodNode mn : (List<MethodNode>) readClassNoCode(lambdaType.getInternalName()).methods)
                if (hasAccess(mn, ACC_ABSTRACT) && getArgumentTypes(mn.desc).length == argumentTypes.size())
                    sams.add(mn);
            return sams;
        }

        void lambdaParameter(int lastParameterStart, FieldInsnNode fin) throws IOException {
            if (isLambdaParameterField(fin)) {
                FieldNode f = findField(fin);

                if (!parameters.containsKey(f.name)) {
                    if (parameters.size() == methodParameterTypes.size())
                        throw new IllegalStateException("Tried to define extra parameter, " + f.name
                                + ", arity is " + methodParameterTypes.size() + ", defined parameters are "
                                + parameters.keySet());

                    parameters.put(f.name, f);
                    Type argumentType = getType(fin.desc);
                    argumentTypes.add(argumentType);

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
            LocalVariableNode local = (LocalVariableNode) m.localVariables.get(vin.var);

            if (vin.getOpcode() == getType(local.desc).getOpcode(ISTORE))
                mutableLocals.put(local.name, local);
            else
                locals.put(local.name, local);

            out.println("  --  accessed var " + local.index + " " + local.name + " "
                    + getType(local.desc).getClassName() + " write: "
                    + (vin.getOpcode() == getType(local.desc).getOpcode(ISTORE)));
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

    int findInstructionAtStartOfLambda(List<Integer> stackDepth, int end) {
        int depth = stackDepth.get(end);
        for (int i = end - 1; i >= 0; i--)
            if (stackDepth.get(i) == depth - 1)
                return i;
        throw new IllegalStateException("Could not find previous stack depth of " + depth);
    }

    void printInstruction(int index, int depth, String asm) {
        out.print(index + "\t");
        out.print(depth + "\t\t");
        out.print(asm);
    }
}
