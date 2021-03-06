package org.enumerable.lambda.support.expression;

import static org.objectweb.asm.Type.*;
import japa.parser.JavaParser;
import japa.parser.ast.expr.*;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.PrimitiveType.Primitive;
import japa.parser.ast.type.ReferenceType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;
import org.objectweb.asm.util.AbstractVisitor;

/**
 * This interpreter is a simple decompiler for methods with a single expression.
 * It uses {@link JavaParser} as its target AST.
 * <p>
 * It is fully TDD in both good and bad ways, meaning that most things are here
 * just to make the tests pass, and no deep analysis has gone into it.
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
    static final PrimitiveType PRIMITIVE_CHAR = new PrimitiveType(Primitive.Char);
    static final PrimitiveType PRIMITIVE_BYTE = new PrimitiveType(Primitive.Byte);
    static final PrimitiveType PRIMITIVE_SHORT = new PrimitiveType(Primitive.Short);

    LocalVariableNode[] parameters;
    Frame currentFrame;
    Analyzer analyzer;
    MethodNode mn;

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

        public String toString() {
            return expression + " (" + type + ")";
        }
    }

    ExpressionInterpreter(MethodNode mn, LocalVariableNode... parameters) {
        this.mn = mn;
        this.parameters = parameters;
    }

    Expression expression;

    UnaryExpr iinc;
    AssignExpr iincAssign;
    ExpressionValue assign;
    ConditionalExpr conditional;
    boolean cmpConditional;
    Set<Integer> returns = new HashSet<Integer>();

    void setCurrentFrame(Frame frame) {
        currentFrame = frame;
        if (iinc != null) {
            if (frame.getStackSize() > 0) {
                ExpressionValue value = new ExpressionValue(PRIMITIVE_INT, iinc);
                ExpressionValue previous = (ExpressionValue) frame.pop();
                if (previous.type == PRIMITIVE_INT && previous.expression instanceof NameExpr) {
                    frame.push(value);
                    iinc = null;
                } else {
                    frame.push(previous);
                }
            } else {
                if (iinc.getOperator() == UnaryExpr.Operator.posIncrement)
                    iinc.setOperator(UnaryExpr.Operator.preIncrement);

                else
                    iinc.setOperator(UnaryExpr.Operator.preDecrement);
            }
        }
        if (iincAssign != null) {
            if (frame.getStackSize() > 0) {
                ExpressionValue value = new ExpressionValue(PRIMITIVE_INT, iincAssign);
                ExpressionValue previous = (ExpressionValue) frame.pop();
                if (previous.type == PRIMITIVE_INT && previous.expression instanceof NameExpr) {
                    frame.push(value);
                    iincAssign = null;
                } else {
                    frame.push(previous);
                }
            }
        }
        if (assign != null) {
            if (frame.getStackSize() > 0) {
                frame.pop();
                frame.push(assign);
                assign = null;
            }
        }
    }

    void newControlFlowEdge(int insn, int successor) {
    }

    public Value newValue(final Type type) {
        if (type == null)
            return new ExpressionValue(null, null);

        Expression value = null;

        switch (type.getSort()) {
        case Type.VOID:
            return null;
        case Type.BOOLEAN:
            return new ExpressionValue(PRIMITIVE_BOOLEAN, value);
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
            return new ExpressionValue(PRIMITIVE_INT, value);
        case Type.FLOAT:
            return new ExpressionValue(PRIMITIVE_FLOAT, value);
        case Type.LONG:
            return new ExpressionValue(PRIMITIVE_LONG, value);
        case Type.DOUBLE:
            return new ExpressionValue(PRIMITIVE_DOUBLE, value);
        case Type.ARRAY:
        case Type.OBJECT:
            return new ExpressionValue(createClassOrInterfaceType(removeJavaLang(type.getClassName())), value);
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
            return new ExpressionValue(PRIMITIVE_FLOAT, new DoubleLiteralExpr("2.0f"));
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
            throw new UnsupportedOperationException(AbstractVisitor.OPCODES[insn.getOpcode()]);
        case GETSTATIC:
            FieldInsnNode fieldNode = (FieldInsnNode) insn;
            ExpressionValue getField = (ExpressionValue) newValue(getType(fieldNode.desc));
            getField.expression = new FieldAccessExpr(new NameExpr(removeJavaLang(getObjectType(fieldNode.owner)
                    .getClassName())), fieldNode.name);
            return getField;
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

    String removeJavaLang(String className) {
        if (className.startsWith("java.lang."))
            className = className.substring("java.lang.".length());
        return className;
    }

    boolean isStoreInstruction(VarInsnNode vin) {
        return vin.getOpcode() >= ISTORE && vin.getOpcode() <= ASTORE;
    }

    public Value copyOperation(final AbstractInsnNode insn, final Value value) throws AnalyzerException {
        ExpressionValue expressionValue = (ExpressionValue) value;
        if (insn instanceof VarInsnNode) {
            VarInsnNode node = (VarInsnNode) insn;
            if (isStoreInstruction(node)) {
                AssignExpr.Operator op = AssignExpr.Operator.assign;
                NameExpr target = new NameExpr(getLocalVariable(node.var).name);
                Expression assignmentValue = expressionValue.expression;

                if (expressionValue.expression instanceof BinaryExpr) {
                    BinaryExpr binary = (BinaryExpr) expressionValue.expression;
                    if (binary.getLeft().equals(target)) {
                        if (binary.getOperator() == BinaryExpr.Operator.plus)
                            op = AssignExpr.Operator.plus;
                        if (binary.getOperator() == BinaryExpr.Operator.minus)
                            op = AssignExpr.Operator.minus;
                        if (binary.getOperator() == BinaryExpr.Operator.times)
                            op = AssignExpr.Operator.star;
                        if (binary.getOperator() == BinaryExpr.Operator.divide)
                            op = AssignExpr.Operator.slash;
                        if (binary.getOperator() == BinaryExpr.Operator.binAnd)
                            op = AssignExpr.Operator.and;
                        if (binary.getOperator() == BinaryExpr.Operator.binOr)
                            op = AssignExpr.Operator.or;
                        if (binary.getOperator() == BinaryExpr.Operator.xor)
                            op = AssignExpr.Operator.xor;
                        if (binary.getOperator() == BinaryExpr.Operator.remainder)
                            op = AssignExpr.Operator.rem;
                        if (binary.getOperator() == BinaryExpr.Operator.lShift)
                            op = AssignExpr.Operator.lShift;
                        if (binary.getOperator() == BinaryExpr.Operator.rSignedShift)
                            op = AssignExpr.Operator.rSignedShift;
                        if (binary.getOperator() == BinaryExpr.Operator.rUnsignedShift)
                            op = AssignExpr.Operator.rUnsignedShift;

                        if (op != AssignExpr.Operator.assign)
                            assignmentValue = binary.getRight();
                    }

                }
                if (expressionValue.expression instanceof UnaryExpr)
                    assign = expressionValue;

                else
                    assign = new ExpressionValue(expressionValue.type, new AssignExpr(target, assignmentValue, op));

            } else {
                if (node.var == 0)
                    return new ExpressionValue(expressionValue.type, new ThisExpr());

                return new ExpressionValue(expressionValue.type, new NameExpr(getLocalVariable(node.var).name));
            }
        }
        if (expressionValue.expression == null)
            return expressionValue;
        return new ExpressionValue(expressionValue.type, LambdaExpressionTrees
                .parseExpression(expressionValue.expression.toString()));
    }

    public Value unaryOperation(final AbstractInsnNode insn, final Value value) throws AnalyzerException {
        ExpressionValue expressionValue = (ExpressionValue) value;
        switch (insn.getOpcode()) {
        case INEG:
            return new ExpressionValue(PRIMITIVE_INT, new UnaryExpr(expressionValue.expression,
                    UnaryExpr.Operator.negative));
        case IINC:
            IincInsnNode node = (IincInsnNode) insn;
            NameExpr nameExpr = new NameExpr(getLocalVariable(node.var).name);
            if (node.incr == 1)
                iinc = new UnaryExpr(nameExpr, UnaryExpr.Operator.posIncrement);
            if (node.incr == -1)
                iinc = new UnaryExpr(nameExpr, UnaryExpr.Operator.posDecrement);

            if (node.incr > 1)
                iincAssign = new AssignExpr(nameExpr, new IntegerLiteralExpr(node.incr + ""),
                        AssignExpr.Operator.plus);
            if (node.incr < -1)
                iincAssign = new AssignExpr(nameExpr, new IntegerLiteralExpr(-node.incr + ""),
                        AssignExpr.Operator.minus);

            return value;
        case L2I:
        case F2I:
        case D2I:
            return new ExpressionValue(PRIMITIVE_INT, new CastExpr(PRIMITIVE_INT, expressionValue.expression));
        case I2B:
            return new ExpressionValue(PRIMITIVE_BYTE, new CastExpr(PRIMITIVE_BYTE, expressionValue.expression));
        case I2C:
            return new ExpressionValue(PRIMITIVE_CHAR, new CastExpr(PRIMITIVE_CHAR, expressionValue.expression));
        case I2S:
            return new ExpressionValue(PRIMITIVE_SHORT, new CastExpr(PRIMITIVE_SHORT, expressionValue.expression));
        case FNEG:
            return new ExpressionValue(PRIMITIVE_FLOAT, new UnaryExpr(expressionValue.expression,
                    UnaryExpr.Operator.negative));
        case I2F:
        case L2F:
        case D2F:
            return new ExpressionValue(PRIMITIVE_FLOAT, new CastExpr(PRIMITIVE_FLOAT, expressionValue.expression));
        case LNEG:
            return new ExpressionValue(PRIMITIVE_LONG, new UnaryExpr(expressionValue.expression,
                    UnaryExpr.Operator.negative));
        case I2L:
        case F2L:
        case D2L:
            return new ExpressionValue(PRIMITIVE_LONG, new CastExpr(PRIMITIVE_LONG, expressionValue.expression));
        case DNEG:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new UnaryExpr(expressionValue.expression,
                    UnaryExpr.Operator.negative));
        case I2D:
        case L2D:
        case F2D:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new CastExpr(PRIMITIVE_DOUBLE, expressionValue.expression));
        case IFEQ:
            if (conditional != null) {
                if (conditional.getCondition() instanceof BinaryExpr && cmpConditional) {
                    ((BinaryExpr) conditional.getCondition()).setOperator(BinaryExpr.Operator.notEquals);
                    cmpConditional = false;

                } else {
                    handleNestedConditional(expressionValue.expression);
                }

            } else {
                conditional = new ConditionalExpr(expressionValue.expression, null, null);
            }
            return null;
        case IFNE:
            if (conditional != null) {
                if (conditional.getCondition() instanceof BinaryExpr && cmpConditional) {
                    ((BinaryExpr) conditional.getCondition()).setOperator(BinaryExpr.Operator.equals);
                    cmpConditional = false;

                } else {
                    handleNestedConditional(new UnaryExpr(expressionValue.expression, UnaryExpr.Operator.not));
                }

            } else {
                conditional = new ConditionalExpr(
                        new UnaryExpr(expressionValue.expression, UnaryExpr.Operator.not), null, null);
            }
            return null;
        case IFGT:
            ((BinaryExpr) conditional.getCondition()).setOperator(BinaryExpr.Operator.lessEquals);
            cmpConditional = false;
            return null;
        case IFLE:
            ((BinaryExpr) conditional.getCondition()).setOperator(BinaryExpr.Operator.greater);
            cmpConditional = false;
            return null;
        case IFLT:
            ((BinaryExpr) conditional.getCondition()).setOperator(BinaryExpr.Operator.greaterEquals);
            cmpConditional = false;
            return null;
        case IFGE:
            ((BinaryExpr) conditional.getCondition()).setOperator(BinaryExpr.Operator.less);
            cmpConditional = false;
            return null;
        case TABLESWITCH:
        case LOOKUPSWITCH:
            throw new UnsupportedOperationException(AbstractVisitor.OPCODES[insn.getOpcode()]);
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:
            return null;
        case PUTSTATIC:
            FieldInsnNode fieldNode = (FieldInsnNode) insn;
            ExpressionValue putField = (ExpressionValue) newValue(getType(fieldNode.desc));
            putField.expression = new AssignExpr(new FieldAccessExpr(new NameExpr(removeJavaLang(getObjectType(
                    fieldNode.owner).getClassName())), fieldNode.name), expressionValue.expression,
                    AssignExpr.Operator.assign);
            assign = putField;
            return null;
        case GETFIELD:
            fieldNode = (FieldInsnNode) insn;
            ExpressionValue getField = (ExpressionValue) newValue(Type.getType(fieldNode.desc));
            getField.expression = new FieldAccessExpr(expressionValue.expression, fieldNode.name);
            return getField;
        case NEWARRAY:
            PrimitiveType type;
            switch (((IntInsnNode) insn).operand) {
            case T_BOOLEAN:
                type = PRIMITIVE_BOOLEAN;
                break;
            case T_CHAR:
                type = PRIMITIVE_CHAR;
                break;
            case T_BYTE:
                type = PRIMITIVE_BYTE;
                break;
            case T_SHORT:
                type = PRIMITIVE_SHORT;
                break;
            case T_INT:
                type = PRIMITIVE_INT;
                break;
            case T_FLOAT:
                type = PRIMITIVE_FLOAT;
                break;
            case T_DOUBLE:
                type = PRIMITIVE_DOUBLE;
                break;
            case T_LONG:
                type = PRIMITIVE_LONG;
                break;
            default:
                throw new AnalyzerException(insn, "Invalid array type");
            }
            ArrayList<Expression> dimensions = new ArrayList<Expression>();
            dimensions.add(expressionValue.expression);
            return new ExpressionValue(new ReferenceType(type, 1), new ArrayCreationExpr(type, dimensions, 0));

        case ANEWARRAY:
            ExpressionValue newArray = (ExpressionValue) newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            dimensions = new ArrayList<Expression>();
            dimensions.add(expressionValue.expression);
            newArray.expression = new ArrayCreationExpr(newArray.type, dimensions, 0);
            return newArray;

        case ARRAYLENGTH:
            return new ExpressionValue(PRIMITIVE_INT, new FieldAccessExpr(expressionValue.expression, "length"));
        case ATHROW:
            throw new UnsupportedOperationException(AbstractVisitor.OPCODES[insn.getOpcode()]);
        case CHECKCAST:
            ExpressionValue cast = (ExpressionValue) newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            cast.expression = new CastExpr(new ReferenceType(cast.type), expressionValue.expression);
            return cast;
        case INSTANCEOF:
            ExpressionValue instanceOf = (ExpressionValue) newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            instanceOf.expression = new InstanceOfExpr(expressionValue.expression, new ReferenceType(instanceOf.type));
            return instanceOf;
        case MONITORENTER:
        case MONITOREXIT:
            throw new UnsupportedOperationException(AbstractVisitor.OPCODES[insn.getOpcode()]);
        case IFNULL:
            handleNestedConditional(new BinaryExpr(expressionValue.expression, new NullLiteralExpr(),
                    BinaryExpr.Operator.notEquals));
            return null;
        case IFNONNULL:
            handleNestedConditional(new BinaryExpr(expressionValue.expression, new NullLiteralExpr(),
                    BinaryExpr.Operator.equals));
            return null;
        default:
            throw new Error("Internal error.");
        }
    }

    public Value binaryOperation(final AbstractInsnNode insn, final Value value1, final Value value2)
            throws AnalyzerException {
        ExpressionValue expressionValue1 = (ExpressionValue) value1;
        ExpressionValue expressionValue2 = (ExpressionValue) value2;
        switch (insn.getOpcode()) {
        case IALOAD:
            return new ExpressionValue(PRIMITIVE_INT, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case BALOAD:
            return new ExpressionValue(PRIMITIVE_BYTE, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case CALOAD:
            return new ExpressionValue(PRIMITIVE_CHAR, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case SALOAD:
            return new ExpressionValue(PRIMITIVE_SHORT, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case IADD:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.plus));
        case ISUB:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.minus));
        case IMUL:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.times));
        case IDIV:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.divide));
        case IREM:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.remainder));
        case ISHL:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.lShift));
        case ISHR:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.rSignedShift));
        case IUSHR:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.rUnsignedShift));
        case IAND:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.binAnd));
        case IOR:
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.binOr));
        case IXOR:
            if (expressionValue2.expression.toString().equals("-1"))
                return new ExpressionValue(PRIMITIVE_INT, new UnaryExpr(expressionValue1.expression,
                        UnaryExpr.Operator.inverse));
            return new ExpressionValue(PRIMITIVE_INT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.xor));
        case FALOAD:
            return new ExpressionValue(PRIMITIVE_FLOAT, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case FADD:
            return new ExpressionValue(PRIMITIVE_FLOAT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.plus));
        case FSUB:
            return new ExpressionValue(PRIMITIVE_FLOAT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.minus));
        case FMUL:
            return new ExpressionValue(PRIMITIVE_FLOAT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.times));
        case FDIV:
            return new ExpressionValue(PRIMITIVE_FLOAT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.divide));
        case FREM:
            return new ExpressionValue(PRIMITIVE_FLOAT, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.remainder));
        case LALOAD:
            return new ExpressionValue(PRIMITIVE_LONG, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case LADD:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.plus));
        case LSUB:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.minus));
        case LMUL:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.times));
        case LDIV:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.divide));
        case LREM:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.remainder));
        case LSHL:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.lShift));
        case LSHR:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.rSignedShift));
        case LUSHR:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.rUnsignedShift));
        case LAND:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.binAnd));
        case LOR:
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.binOr));
        case LXOR:
            if (expressionValue2.expression.toString().equals("-1L"))
                return new ExpressionValue(PRIMITIVE_LONG, new UnaryExpr(expressionValue1.expression,
                        UnaryExpr.Operator.inverse));
            return new ExpressionValue(PRIMITIVE_LONG, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.xor));
        case DALOAD:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case DADD:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.plus));
        case DSUB:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.minus));
        case DMUL:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.times));
        case DDIV:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.divide));
        case DREM:
            return new ExpressionValue(PRIMITIVE_DOUBLE, new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.remainder));
        case AALOAD:
            return new ExpressionValue(expressionValue1.type, new ArrayAccessExpr(expressionValue1.expression,
                    expressionValue2.expression));
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
            cmpConditional = true;
            conditional = new ConditionalExpr(new BinaryExpr(expressionValue1.expression,
                    expressionValue2.expression, BinaryExpr.Operator.notEquals), null, null);
            return new ExpressionValue(PRIMITIVE_INT, null);
        case IF_ICMPEQ:
            Expression condition = null;
            if (booleanValue(expressionValue2.expression, expressionValue1).toString().equals("true"))
                condition = new UnaryExpr(expressionValue1.expression, UnaryExpr.Operator.not);

            else if (booleanValue(expressionValue1.expression, expressionValue2).toString().equals("true"))
                condition = new UnaryExpr(expressionValue2.expression, UnaryExpr.Operator.not);

            else if (booleanValue(expressionValue1.expression, expressionValue2).toString().equals("false"))
                condition = expressionValue2.expression;

            else
                condition = new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                        BinaryExpr.Operator.notEquals);

            handleNestedConditional(condition);

            return null;
        case IF_ICMPNE:
            condition = null;
            if (booleanValue(expressionValue2.expression, expressionValue1).toString().equals("true"))
                condition = expressionValue1.expression;

            else if (booleanValue(expressionValue1.expression, expressionValue2).toString().equals("true"))
                condition = expressionValue2.expression;

            else if (booleanValue(expressionValue1.expression, expressionValue2).toString().equals("false"))
                condition = new UnaryExpr(expressionValue2.expression, UnaryExpr.Operator.not);

            else
                condition = new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                        BinaryExpr.Operator.equals);

            handleNestedConditional(condition);

            return null;
        case IF_ICMPLT:
            handleNestedConditional(new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                    BinaryExpr.Operator.greaterEquals));
            return null;
        case IF_ICMPGE:
            handleNestedConditional(new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                    BinaryExpr.Operator.less));
            return null;
        case IF_ICMPGT:
            handleNestedConditional(new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                    BinaryExpr.Operator.lessEquals));
            return null;
        case IF_ICMPLE:
            handleNestedConditional(new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                    BinaryExpr.Operator.greater));
            return null;
        case IF_ACMPEQ:
            handleNestedConditional(new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                    BinaryExpr.Operator.notEquals));
            return null;
        case IF_ACMPNE:
            handleNestedConditional(new BinaryExpr(expressionValue1.expression, expressionValue2.expression,
                    BinaryExpr.Operator.equals));
            return null;
        case PUTFIELD:
            FieldInsnNode fieldNode = (FieldInsnNode) insn;
            ExpressionValue putField = (ExpressionValue) newValue(Type.getType(fieldNode.desc));
            putField.expression = new AssignExpr(new FieldAccessExpr(expressionValue1.expression, fieldNode.name),
                    expressionValue2.expression, AssignExpr.Operator.assign);
            assign = putField;
            return null;
        default:
            throw new Error("Internal error.");
        }
    }

    void handleNestedConditional(Expression condition) {
        if (conditional == null)
            conditional = new ConditionalExpr(condition, null, null);
        else {
            Expression currentCondition = conditional.getCondition();
            if (currentCondition instanceof UnaryExpr
                    && ((UnaryExpr) currentCondition).getOperator() == UnaryExpr.Operator.not) {
                conditional.setCondition(new BinaryExpr(((UnaryExpr) currentCondition).getExpr(), condition,
                        BinaryExpr.Operator.or));

            } else {
                conditional.setCondition(new BinaryExpr(currentCondition, condition, BinaryExpr.Operator.and));
            }
        }
    }

    public Value ternaryOperation(final AbstractInsnNode insn, final Value value1, final Value value2,
            final Value value3) throws AnalyzerException {
        ExpressionValue expressionValue1 = (ExpressionValue) value1;
        ExpressionValue expressionValue2 = (ExpressionValue) value2;
        ExpressionValue expressionValue3 = (ExpressionValue) value3;
        switch (insn.getOpcode()) {
        case IASTORE:
        case BASTORE:
        case CASTORE:
        case SASTORE:
        case FASTORE:
        case DASTORE:
        case LASTORE:
        case AASTORE:
            if (expressionValue1.expression instanceof ArrayCreationExpr) {
                ArrayCreationExpr arrayCreationExpression = (ArrayCreationExpr) expressionValue1.expression;
                ArrayInitializerExpr initializer = arrayCreationExpression.getInitializer();
                if (initializer == null)
                    initializer = new ArrayInitializerExpr();

                List<Expression> values = initializer.getValues();
                if (values == null)
                    values = new ArrayList<Expression>();
                values.add(expressionValue3.expression);

                initializer.setValues(values);
                arrayCreationExpression.setInitializer(initializer);
                arrayCreationExpression.setDimensions(null);
                arrayCreationExpression.setArrayCount(1);

                assign = expressionValue1;

            } else {
                assign = new ExpressionValue(expressionValue1.type, new AssignExpr(new ArrayAccessExpr(
                        expressionValue1.expression, expressionValue2.expression), expressionValue3.expression,
                        AssignExpr.Operator.assign));
            }
            return null;
        default:
            throw new Error("Internal error.");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Value naryOperation(final AbstractInsnNode insn, final List values) throws AnalyzerException {
        if (insn.getOpcode() == MULTIANEWARRAY) {
            throw new UnsupportedOperationException(AbstractVisitor.OPCODES[insn.getOpcode()]);

        } else {
            MethodInsnNode node = (MethodInsnNode) insn;
            ClassOrInterfaceType returnType = createClassOrInterfaceType(getReturnType(node.desc).getClassName());
            Expression scope = null;
            boolean isConstructor = node.getOpcode() == INVOKESPECIAL && "<init>".equals(node.name);

            if (node.getOpcode() == INVOKESTATIC) {
                String className = getObjectType(node.owner).getClassName();
                scope = new NameExpr(removeJavaLang(className));
            } else if (!isConstructor) {
                ExpressionValue target = (ExpressionValue) values.remove(0);
                if (!(target.expression instanceof ThisExpr))
                    scope = target.expression;
            }

            List<Expression> arguments = values.isEmpty() ? null : new ArrayList<Expression>();
            for (ExpressionValue value : (List<ExpressionValue>) values)
                arguments.add(value.expression);

            if (isConstructor) {
                arguments.remove(0);
                ExpressionValue newExpressionValue = (ExpressionValue) values.remove(0);
                ClassOrInterfaceType type = (ClassOrInterfaceType) newExpressionValue.type;

                newExpressionValue.expression = new ObjectCreationExpr(scope, type, arguments.isEmpty() ? null
                        : arguments);

                return new ExpressionValue(returnType, newExpressionValue.expression);
            }

            return new ExpressionValue(returnType, new MethodCallExpr(scope, node.name, arguments));
        }
    }

    public void returnOperation(final AbstractInsnNode insn, final Value value, final Value expected)
            throws AnalyzerException {
        returns.add(mn.instructions.indexOf(insn));
        expression = ((ExpressionValue) value).expression;
        expression = booleanValue(expression, expected);

        if (conditional != null) {
            Expression elseExpr = conditional.getElseExpr();
            if (elseExpr == null)
                conditional.setElseExpr(expression);

            else {
                Expression condition = conditional.getCondition();
                if (currentFrame.getStackSize() > 0) {
                    Value then = currentFrame.pop();
                    currentFrame.push(then);
                    conditional.setThenExpr(booleanValue(((ExpressionValue) then).expression, expected));

                } else {
                    Expression thenExpr = conditional.getThenExpr();

                    if (thenExpr == null && expression != null)
                        conditional.setThenExpr(expression);

                    else
                        conditional.setThenExpr(new BooleanLiteralExpr(false));
                }

                Expression thenExpr = conditional.getThenExpr();
                if (thenExpr instanceof BooleanLiteralExpr && elseExpr instanceof BooleanLiteralExpr) {
                    if (thenExpr.toString().equals("true") && elseExpr.toString().equals("false")) {
                        expression = condition;

                    } else if (thenExpr.toString().equals("false") && elseExpr.toString().equals("true")) {

                        if (condition instanceof BinaryExpr) {
                            BinaryExpr binaryExpr = (BinaryExpr) condition;

                            expression = condition;
                            if (binaryExpr.getLeft() instanceof BinaryExpr) {
                                BinaryExpr left = (BinaryExpr) binaryExpr.getLeft();

                                if (binaryExpr.getOperator() == BinaryExpr.Operator.and) {
                                    binaryExpr.setOperator(BinaryExpr.Operator.or);
                                    flipOperator(left);
                                }
                            }
                            if (couldBeECJ()) {
                                if (binaryExpr.getRight() instanceof BinaryExpr) {
                                    BinaryExpr right = (BinaryExpr) binaryExpr.getRight();

                                    if (binaryExpr.getOperator() == BinaryExpr.Operator.or)
                                        flipOperator(right);
                                }
                                if (binaryExpr.getRight() instanceof UnaryExpr) {
                                    UnaryExpr right = (UnaryExpr) binaryExpr.getRight();

                                    if (right.getOperator() == UnaryExpr.Operator.not)
                                        binaryExpr.setRight(right.getExpr());
                                }
                            }

                        } else
                            expression = new UnaryExpr(condition, UnaryExpr.Operator.not);

                    }
                } else {
                    expression = conditional;
                }

                conditional = null;
            }
        }
    }

    boolean couldBeECJ() {
        return returns.size() > 1;
    }

    BinaryExpr flipOperator(BinaryExpr binaryExpr) {
        BinaryExpr.Operator op = binaryExpr.getOperator();
        if (op == BinaryExpr.Operator.notEquals) {
            binaryExpr.setOperator(BinaryExpr.Operator.equals);
        } else if (op == BinaryExpr.Operator.equals) {
            binaryExpr.setOperator(BinaryExpr.Operator.notEquals);
        } else if (op == BinaryExpr.Operator.greater) {
            binaryExpr.setOperator(BinaryExpr.Operator.lessEquals);
        } else if (op == BinaryExpr.Operator.less) {
            binaryExpr.setOperator(BinaryExpr.Operator.greaterEquals);
        } else if (op == BinaryExpr.Operator.greaterEquals) {
            binaryExpr.setOperator(BinaryExpr.Operator.less);
        } else if (op == BinaryExpr.Operator.lessEquals) {
            binaryExpr.setOperator(BinaryExpr.Operator.greater);
        }
        return binaryExpr;
    }

    Expression booleanValue(Expression expression, Value expected) {
        if (((ExpressionValue) expected).type == PRIMITIVE_BOOLEAN && expression instanceof IntegerLiteralExpr)
            if ("1".equals(expression.toString()))
                expression = new BooleanLiteralExpr(true);
            else if ("0".equals(expression.toString()))
                expression = new BooleanLiteralExpr(false);
        return expression;
    }

    public Value merge(final Value v, final Value w) {
        if (!v.equals(w)) {
            return new ExpressionValue(null, null);
        }
        return v;
    }
}
