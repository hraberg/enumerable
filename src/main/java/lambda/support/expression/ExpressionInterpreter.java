package lambda.support.expression;

import static lambda.exception.UncheckedException.*;
import static org.objectweb.asm.Type.*;
import japa.parser.JavaParser;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.ReferenceType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;
import org.objectweb.asm.util.ASMifierMethodVisitor;

/**
 * This interpreter is a simple decompiler for methods with a single expression.
 * It uses {@link JavaParser} as its target AST.
 * <p>
 * This Interpreter started as a copy of {@link BasicInterpreter} by Eric
 * Bruneton and Bing Ran.
 */
public class ExpressionInterpreter implements Opcodes, Interpreter {
    static final PrimitiveType PRIMITIVE_BOOLEAN = new PrimitiveType(Primitive.Boolean);
    static final PrimitiveType PRIMITIVE_INT = new PrimitiveType(Primitive.Int);
    static final PrimitiveType PRIMITIVE_FLOAT = new PrimitiveType(Primitive.Float);
    static final PrimitiveType PRIMITIVE_LONG = new PrimitiveType(Primitive.Long);
    static final PrimitiveType PRIMITIVE_DOUBLE = new PrimitiveType(Primitive.Double);

    LocalVariableNode[] parameters;

    static class ExpressionValue implements Value {
        Expression expression;
        japa.parser.ast.type.Type type;
        int size = 1;

        ExpressionValue(japa.parser.ast.type.Type type, Expression expression) {
            this.type = type;
            this.expression = expression;

            if (type instanceof PrimitiveType) {
                Primitive primitive = ((PrimitiveType) type).getType();
                size = primitive == Primitive.Long || primitive == Primitive.Double ? 2 : 1;
            }
        }

        public int getSize() {
            return size;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((expression == null) ? 0 : expression.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExpressionValue other = (ExpressionValue) obj;
            if (expression == null) {
                if (other.expression != null)
                    return false;
            } else if (!expression.equals(other.expression))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
    }

    ExpressionInterpreter(LocalVariableNode... parameters) {
        this.parameters = parameters;
    }

    public static Expression parseExpression(String expression) {
        try {
            Class<?> parserClass = Class.forName("japa.parser.ASTParser");
            Constructor<?> ctor = parserClass.getConstructor(Reader.class);
            ctor.setAccessible(true);
            Object parser = ctor.newInstance(new StringReader(expression));
            Method method = parserClass.getMethod("Expression");
            method.setAccessible(true);
            return (Expression) method.invoke(parser);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static Expression parseExpressionFromMethod(Method method, String... parameters) {
        try {
            MethodNode mn = findMethodNode(method);

            LocalVariableNode[] parameterLocals = new LocalVariableNode[parameters.length];
            Type[] argumentTypes = getArgumentTypes(mn.desc);
            int realIndex = 1;
            for (int i = 0; i < parameters.length; i++) {
                parameterLocals[i] = new LocalVariableNode(parameters[i], argumentTypes[i].getDescriptor(), null,
                        null, null, realIndex);
                realIndex += argumentTypes[i].getSize();
            }

            ExpressionInterpreter interpreter = new ExpressionInterpreter(parameterLocals);
            new Analyzer(interpreter).analyze(getInternalName(method.getDeclaringClass()), mn);
            return interpreter.expression;
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    public static void printASMifiedMethod(Method method) {
        try {
            MethodNode mn = findMethodNode(method);
            ASMifierMethodVisitor asm = new ASMifierMethodVisitor();
            mn.accept(asm);
            PrintWriter pw = new PrintWriter(System.out);
            asm.print(pw);
            pw.flush();
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    @SuppressWarnings("unchecked")
    static MethodNode findMethodNode(Method method) throws IOException {
        String className = method.getDeclaringClass().getName();
        ClassReader cr;
        if (InMemoryCompiler.bytesByClassName.containsKey(className))
            cr = new ClassReader(InMemoryCompiler.bytesByClassName.get(className));

        else
            cr = new ClassReader(className);

        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        String descriptor = getMethodDescriptor(method);
        for (MethodNode mn : (List<MethodNode>) cn.methods) {
            if (method.getName().equals(mn.name) && descriptor.equals(mn.desc))
                return mn;
        }
        throw new IllegalStateException("Cannot find method which does exist");
    }

    Expression expression;

    public Value newValue(final Type type) {
        if (type == null) {
            return new ExpressionValue(null, null);
        }
        switch (type.getSort()) {
        case Type.VOID:
            return null;
        case Type.BOOLEAN:
            return new ExpressionValue(PRIMITIVE_BOOLEAN, null);
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
            return new ExpressionValue(PRIMITIVE_INT, null);
        case Type.FLOAT:
            return new ExpressionValue(PRIMITIVE_FLOAT, null);
        case Type.LONG:
            return new ExpressionValue(PRIMITIVE_LONG, null);
        case Type.DOUBLE:
            return new ExpressionValue(PRIMITIVE_DOUBLE, null);
        case Type.ARRAY:
        case Type.OBJECT:
            return new ExpressionValue(createClassOrInterfaceType(Object.class.getName()), null);
        default:
            throw new Error("Internal error");
        }
    }

    public Value newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        switch (insn.getOpcode()) {
        case ACONST_NULL:
            return new ExpressionValue(createClassOrInterfaceType(Object.class.getName()), new NullLiteralExpr());
        case ICONST_M1:
            return new ExpressionValue(PRIMITIVE_INT, new UnaryExpr(new IntegerLiteralExpr("1"),
                    UnaryExpr.Operator.negative));
        case ICONST_0:
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("0"));
        case ICONST_1:
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("1"));
        case ICONST_2:
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("2"));
        case ICONST_3:
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("3"));
        case ICONST_4:
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("4"));
        case ICONST_5:
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("5"));
        case LCONST_0:
            return new ExpressionValue(PRIMITIVE_LONG, new LongLiteralExpr("0L"));
        case LCONST_1:
            return new ExpressionValue(PRIMITIVE_LONG, new LongLiteralExpr("1L"));
        case FCONST_0:
            return new ExpressionValue(PRIMITIVE_FLOAT, new DoubleLiteralExpr("0.0f"));
        case FCONST_1:
            return new ExpressionValue(PRIMITIVE_FLOAT, new DoubleLiteralExpr("1.0f"));
        case FCONST_2:
            return new ExpressionValue(PRIMITIVE_FLOAT, new DoubleLiteralExpr("2.0"));
        case DCONST_0:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new DoubleLiteralExpr("0.0"));
        case DCONST_1:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new DoubleLiteralExpr("1.0"));
        case BIPUSH:
        case SIPUSH:
            int operand = ((IntInsnNode) insn).operand;
            if (operand < 0)
                return new ExpressionValue(PRIMITIVE_INT, new UnaryExpr(new IntegerLiteralExpr(""
                        + Math.abs(operand)), UnaryExpr.Operator.negative));
            return new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr("" + operand));
        case LDC:
            Object cst = ((LdcInsnNode) insn).cst;

            if (cst instanceof Number) {
                ExpressionValue value = null;
                if (cst instanceof Integer) {
                    value = new ExpressionValue(PRIMITIVE_INT, new IntegerLiteralExpr(cst.toString()));
                } else if (cst instanceof Float) {
                    value = new ExpressionValue(PRIMITIVE_FLOAT, new DoubleLiteralExpr(cst.toString() + "f"));
                } else if (cst instanceof Long) {
                    value = new ExpressionValue(PRIMITIVE_LONG, new LongLiteralExpr(cst.toString() + "L"));
                } else if (cst instanceof Double) {
                    value = new ExpressionValue(PRIMITIVE_DOUBLE, new DoubleLiteralExpr(cst.toString()));
                }
                if (((Number) cst).intValue() < 0) {
                    StringLiteralExpr expr = (StringLiteralExpr) value.expression;
                    expr.setValue(expr.getValue().substring("-".length()));
                    value.expression = new UnaryExpr(expr, UnaryExpr.Operator.negative);
                }
                return value;

            } else if (cst instanceof Type) {
                ClassExpr classExpr = new ClassExpr(new ReferenceType(createClassOrInterfaceType(((Type) cst)
                        .getClassName())));
                return new ExpressionValue(createClassOrInterfaceType(Class.class.getName()), classExpr);
            } else {
                return new ExpressionValue(createClassOrInterfaceType(String.class.getName()),
                        new StringLiteralExpr(cst.toString()));
            }
        case JSR:
            return new ExpressionValue(null, null);
        case GETSTATIC:
            return newValue(Type.getType(((FieldInsnNode) insn).desc));
        case NEW:
            return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
        default:
            throw new Error("Internal error.");
        }
    }

    ClassOrInterfaceType createClassOrInterfaceType(String className) {
        if (!className.contains("."))
            return new ClassOrInterfaceType(className);
        ClassOrInterfaceType parent = null;
        for (String name : className.split("\\."))
            parent = new ClassOrInterfaceType(parent, name);
        return parent;
    }

    LocalVariableNode getLocalVariable(int var) {
        for (LocalVariableNode local : parameters)
            if (var == local.index)
                return local;
        return null;
    }

    public Value copyOperation(final AbstractInsnNode insn, final Value value) throws AnalyzerException {
        ExpressionValue expressionValue = (ExpressionValue) value;
        if (insn instanceof VarInsnNode) {
            int index = ((VarInsnNode) insn).var;
            if (index == 0)
                return new ExpressionValue(expressionValue.type, new ThisExpr());
            return new ExpressionValue(expressionValue.type, new NameExpr(getLocalVariable(index).name));
        }
        return new ExpressionValue(expressionValue.type, parseExpression(expressionValue.toString()));
    }

    public Value unaryOperation(final AbstractInsnNode insn, final Value value) throws AnalyzerException {
        switch (insn.getOpcode()) {
        case INEG:
        case IINC:
        case L2I:
        case F2I:
        case D2I:
        case I2B:
        case I2C:
        case I2S:
            return new ExpressionValue(PRIMITIVE_INT, null);
        case FNEG:
        case I2F:
        case L2F:
        case D2F:
            return new ExpressionValue(PRIMITIVE_FLOAT, null);
        case LNEG:
        case I2L:
        case F2L:
        case D2L:
            return new ExpressionValue(PRIMITIVE_LONG, null);
        case DNEG:
        case I2D:
        case L2D:
        case F2D:
            return new ExpressionValue(PRIMITIVE_DOUBLE, null);
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case TABLESWITCH:
        case LOOKUPSWITCH:
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:
        case PUTSTATIC:
            return null;
        case GETFIELD:
            return newValue(Type.getType(((FieldInsnNode) insn).desc));
        case NEWARRAY:
            switch (((IntInsnNode) insn).operand) {
            case T_BOOLEAN:
                return newValue(Type.getType("[Z"));
            case T_CHAR:
                return newValue(Type.getType("[C"));
            case T_BYTE:
                return newValue(Type.getType("[B"));
            case T_SHORT:
                return newValue(Type.getType("[S"));
            case T_INT:
                return newValue(Type.getType("[I"));
            case T_FLOAT:
                return newValue(Type.getType("[F"));
            case T_DOUBLE:
                return newValue(Type.getType("[D"));
            case T_LONG:
                return newValue(Type.getType("[J"));
            default:
                throw new AnalyzerException("Invalid array type");
            }
        case ANEWARRAY:
            String desc = ((TypeInsnNode) insn).desc;
            return newValue(Type.getType("[" + Type.getObjectType(desc)));
        case ARRAYLENGTH:
            return new ExpressionValue(PRIMITIVE_INT, null);
        case ATHROW:
            return null;
        case CHECKCAST:
            desc = ((TypeInsnNode) insn).desc;
            return newValue(Type.getObjectType(desc));
        case INSTANCEOF:
            return new ExpressionValue(PRIMITIVE_INT, null);
        case MONITORENTER:
        case MONITOREXIT:
        case IFNULL:
        case IFNONNULL:
            return null;
        default:
            throw new Error("Internal error.");
        }
    }

    public Value binaryOperation(final AbstractInsnNode insn, final Value value1, final Value value2)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
        case IALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case IADD:
        case ISUB:
        case IMUL:
        case IDIV:
        case IREM:
        case ISHL:
        case ISHR:
        case IUSHR:
        case IAND:
        case IOR:
        case IXOR:
            return new ExpressionValue(PRIMITIVE_INT, null);
        case FALOAD:
        case FADD:
        case FSUB:
        case FMUL:
        case FDIV:
        case FREM:
            return new ExpressionValue(PRIMITIVE_FLOAT, null);
        case LALOAD:
        case LADD:
        case LSUB:
        case LMUL:
        case LDIV:
        case LREM:
        case LSHL:
        case LSHR:
        case LUSHR:
        case LAND:
        case LOR:
        case LXOR:
            return new ExpressionValue(PRIMITIVE_LONG, null);
        case DALOAD:
        case DADD:
        case DSUB:
        case DMUL:
        case DDIV:
        case DREM:
            return new ExpressionValue(PRIMITIVE_DOUBLE, null);
        case AALOAD:
            return new ExpressionValue(createClassOrInterfaceType(Object.class.getName()), null);
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
            return new ExpressionValue(PRIMITIVE_INT, null);
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case PUTFIELD:
            return null;
        default:
            throw new Error("Internal error.");
        }
    }

    public Value ternaryOperation(final AbstractInsnNode insn, final Value value1, final Value value2,
            final Value value3) throws AnalyzerException {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Value naryOperation(final AbstractInsnNode insn, final List values) throws AnalyzerException {
        if (insn.getOpcode() == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else {
            return newValue(Type.getReturnType(((MethodInsnNode) insn).desc));
        }
    }

    public void returnOperation(final AbstractInsnNode insn, final Value value, final Value expected)
            throws AnalyzerException {
        expression = ((ExpressionValue) value).expression;

        if (((ExpressionValue) expected).type == PRIMITIVE_BOOLEAN && expression instanceof IntegerLiteralExpr)
            if ("1".equals(expression.toString()))
                expression = new BooleanLiteralExpr(true);
            else if ("0".equals(expression.toString()))
                expression = new BooleanLiteralExpr(false);
    }

    public Value merge(final Value v, final Value w) {
        if (!v.equals(w)) {
            return new ExpressionValue(null, null);
        }
        return v;
    }
}
