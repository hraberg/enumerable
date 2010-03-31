package lambda.weaving;

import static lambda.weaving.LambdaTransformer.*;
import static org.objectweb.asm.Type.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lambda.LambdaParameter;
import lambda.NewLambda;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class FirstPassClassVisitor implements ClassVisitor, MethodVisitor {
	Map<String, MethodInfo> methodsByName = new HashMap<String, MethodInfo>();

	boolean inLambda;
	MethodInfo currentMethod;

	private String className;

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		currentMethod = new MethodInfo(name, desc);
		methodsByName.put(currentMethod.getFullName(), currentMethod);
		return this;
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		try {
			if (owner.equals(className)) {
				return;
			}
			Field field = LambdaTransformer.findField(owner, name);
			if (!inLambda && field.isAnnotationPresent(LambdaParameter.class)) {
				inLambda = true;
				currentMethod.newLambda();
			}
		} catch (NoSuchFieldException ignore) {
			debug(ignore.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void visitIincInsn(int var, int increment) {
		if (inLambda) {
			currentMethod.accessLocalFromLambda(var);
		}
	}

	public void visitVarInsn(int opcode, int operand) {
		if (inLambda) {
			currentMethod.accessLocalFromLambda(operand);
		}
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		currentMethod.setTypeOfLocal(index, getType(desc));
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		try {
			if (owner.equals(className) || !inLambda) {
				return;
			}
			Method method = LambdaTransformer.findMethod(owner, name, desc);
			if (method.isAnnotationPresent(NewLambda.class)) {
				currentMethod.setLambdaArity(method.getParameterTypes().length - 1);
				inLambda = false;
			}
		} catch (NoSuchMethodException ignore) {
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.className = name;
	}

	boolean hasNoLambdas() {
		for (MethodInfo method : methodsByName.values()) {
			if (!method.lambdas.isEmpty())
				return false;
		}
		return true;
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	public void visitCode() {
	}

	public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
	}

	public void visitInsn(int opcode) {
	}

	public void visitIntInsn(int opcode, int operand) {
	}

	public void visitJumpInsn(int opcode, Label label) {
	}

	public void visitLabel(Label label) {
	}

	public void visitLdcInsn(Object cst) {
	}

	public void visitLineNumber(int line, Label start) {
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	}

	public void visitMaxs(int maxStack, int maxLocals) {
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
	}

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		return null;
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
	}

	public void visitTypeInsn(int opcode, String type) {
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	public void visitAttribute(Attribute attr) {
	}

	public void visitEnd() {
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	public void visitSource(String source, String debug) {
	}
}