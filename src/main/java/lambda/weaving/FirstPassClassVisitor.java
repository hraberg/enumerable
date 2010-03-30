package lambda.weaving;

import static org.objectweb.asm.Type.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import lambda.LambdaParameter;
import lambda.NewLambda;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

class FirstPassClassVisitor extends ClassAdapter {
	Map<String, MethodInfo> methodsByName = new HashMap<String, MethodInfo>();

	boolean inLambda;
	MethodInfo currentMethod;

	private String className;

	FirstPassClassVisitor() {
		super(new EmptyVisitor());
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		currentMethod = new MethodInfo(name, desc);
		methodsByName.put(currentMethod.getFullName(), currentMethod);
		
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		return new MethodAdapter(mv) {
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
					LambdaTransformer.debug(ignore);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void visitVarInsn(int opcode, int operand) {
				if (inLambda) {
					currentMethod.accessLocalFromLambda(operand);
				}
				super.visitIntInsn(opcode, operand);
			}

			public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
				currentMethod.setTypeOfLocal(index, getType(desc));
				super.visitLocalVariable(name, desc, signature, start, end, index);
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
		};
	}


	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.className = name;
	}

	boolean hasNoLambdas() {
		for (MethodInfo method : methodsByName.values()) {
			if (!method.lambdas.isEmpty())
				return false;
		}
		return true;
	}
}