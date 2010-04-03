package lambda.weaving;

import static org.objectweb.asm.Type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

    String getNameAndDesc() {
        return name + desc;
    }

    Map<Integer, LocalInfo> accessedLocalsByIndex = new HashMap<Integer, LocalInfo>();
    List<MethodInfo.LambdaInfo> lambdas = new ArrayList<MethodInfo.LambdaInfo>();

    void accessLocalFromLambda(int operand) {
        LocalInfo local = accessedLocalsByIndex.get(operand);
        if (local == null) {
            local = new LocalInfo();
            accessedLocalsByIndex.put(operand, local);
        }
        lastLambda().accessedLocals.add(operand);
    }

    void newLambda() {
        lambdas.add(new LambdaInfo());
    }

    MethodInfo.LambdaInfo lastLambda() {
        return lambdas.get(lambdas.size() - 1);
    }

    Iterator<MethodInfo.LambdaInfo> lambdas() {
        return lambdas.iterator();
    }

    void setInfoForLocal(int index, String name, Type type) {
        LocalInfo localInfo = accessedLocalsByIndex.get(index);
        if (localInfo != null) {
            localInfo.name = name;
            localInfo.type = type;
        }
    }

    Type getTypeOfLocal(int operand) {
        return accessedLocalsByIndex.get(operand).type;
    }

    String getNameOfLocal(int operand) {
        return accessedLocalsByIndex.get(operand).name;
    }

    boolean isLocalAccessedFromLambda(int index) {
        return accessedLocalsByIndex.containsKey(index);
    }

    Set<Integer> getAccessedArguments() {
        Set<Integer> accessedArguments = new HashSet<Integer>();
        for (int i = 1; i <= getArgumentTypes(desc).length; i++)
            if (accessedLocalsByIndex.keySet().contains(i))
                accessedArguments.add(i);
        return accessedArguments;
    }

    String getAccessedArgumentsAndLocalsString(Set<Integer> accessedLocals) {
        accessedLocals = new HashSet<Integer>(accessedLocals);
        Set<Integer> accessedArguments = getAccessedArguments();
        accessedArguments.retainAll(accessedLocals);
        accessedLocals.removeAll(accessedArguments);
        return "(" + getAccessedLocalsString(accessedArguments) + ")" + "[" + getAccessedLocalsString(accessedLocals) + "]";
    }

    String getAccessedLocalsString(Set<Integer> accessedLocals) {
        String s = "";
        for (Iterator<Integer> i = accessedLocals.iterator(); i.hasNext();) {
            s += getNameOfLocal(i.next());
            if (i.hasNext())
                s += ", ";
        }
        return s;
    }

    List<String> getAccessedLocalNames(Set<Integer> accessedLocals) {
        List<String> result = new ArrayList<String>();
        for (Iterator<Integer> i = accessedLocals.iterator(); i.hasNext();)
            result.add(getNameOfLocal(i.next()));
        return result;
    }

    static class LocalInfo {
        String name;
        int index;
        Type type;
    }

    static class LambdaInfo {
        int arity;
        Set<Integer> accessedLocals = new HashSet<Integer>();
        Set<String> parameters = new LinkedHashSet<String>();
        Type type;

        void setInfo(Type type, int arity) {
            this.arity = arity;
            this.type = type;
        }

        void setParameterInfo(String name, Type type) {
            parameters.add(name);
        }

        String getParametersString() {
            return parameters.toString().replace('[', '(').replace(']', ')');
        }
    }

    Set<Integer> getAccessedLocals() {
        return accessedLocalsByIndex.keySet();
    }
}