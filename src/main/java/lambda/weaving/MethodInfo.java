package lambda.weaving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

class MethodInfo {
	String name;
	String desc;

	MethodInfo(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	String getFullName() {
		return name + desc;
	}

	Map<Integer, MethodInfo.LocalInfo> accessedLocalsByIndex = new HashMap<Integer, MethodInfo.LocalInfo>();
	List<MethodInfo.LambdaInfo> lambdas = new ArrayList<MethodInfo.LambdaInfo>();

	void accessLocalFromLambda(int operand) {
		MethodInfo.LocalInfo local = accessedLocalsByIndex.get(operand);
		if (local == null) {
			local = new LocalInfo();
			accessedLocalsByIndex.put(operand, local);
		}
		lastLambda().accessedLocals.add(operand);
	}

	void newLambda() {
		lambdas.add(new LambdaInfo());
	}

	void setLambdaArity(int arity) {
		lastLambda().arity = arity;
	}

	MethodInfo.LambdaInfo lastLambda() {
		return lambdas.get(lambdas.size() - 1);
	}

	Iterator<MethodInfo.LambdaInfo> lambdas() {
		return lambdas.iterator();
	}

	void setTypeOfLocal(int index, Type type) {
		MethodInfo.LocalInfo localInfo = accessedLocalsByIndex.get(index);
		if (localInfo != null)
			localInfo.type = type;
	}

	Type getTypeOfLocal(int operand) {
		return accessedLocalsByIndex.get(operand).type;
	}

	boolean isLocalAccessedFromLambda(int index) {
		return accessedLocalsByIndex.containsKey(index);
	}

	static class LocalInfo {
		int index;
		Type type;
	}

	static class LambdaInfo {
		int arity;
		Set<Integer> accessedLocals = new HashSet<Integer>();
	}
}