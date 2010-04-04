package lambda.weaving;

import static org.objectweb.asm.Type.*;

import java.util.ArrayList;
import java.util.Collection;
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

    public String toString() {
        List<String> parameters = new ArrayList<String>();
        for (Type type : getArgumentTypes(desc))
            parameters.add(getSimpleClassName(type));
        return getSimpleClassName(getReturnType(desc)) + " " + name + toParameterString(parameters);
    }

    Map<Integer, VariableInfo> accessedLocalsByIndex = new HashMap<Integer, VariableInfo>();
    List<LambdaInfo> lambdas = new ArrayList<LambdaInfo>();

    LambdaInfo newLambda() {
        LambdaInfo lambda = new LambdaInfo();
        lambdas.add(lambda);
        return lambda;
    }

    LambdaInfo lastLambda() {
        return lambdas.get(lambdas.size() - 1);
    }

    Iterator<LambdaInfo> lambdas() {
        return lambdas.iterator();
    }

    void setInfoForLocal(int index, String name, Type type) {
        VariableInfo variableInfo = accessedLocalsByIndex.get(index);
        if (variableInfo != null) {
            variableInfo.name = name;
            variableInfo.type = type;
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

    Set<Integer> getAccessedParameters() {
        Set<Integer> accessedParameters = new HashSet<Integer>();
        for (int i = 1; i <= getArgumentTypes(desc).length; i++)
            if (accessedLocalsByIndex.keySet().contains(i))
                accessedParameters.add(i);
        return accessedParameters;
    }

    String getAccessedParametersAndLocalsString(Set<Integer> accessedLocals) {
        accessedLocals = new HashSet<Integer>(accessedLocals);
        Set<Integer> accessedParameters = getAccessedParameters();

        accessedParameters.retainAll(accessedLocals);
        accessedLocals.removeAll(accessedParameters);

        List<String> parameters = getLocalNames(accessedParameters);
        List<String> locals = getLocalNames(accessedLocals);

        return (parameters.isEmpty() ? "" : toParameterString(parameters)) + (locals.isEmpty() ? "" : locals);
    }

    String toParameterString(Collection<?> parameters) {
        return parameters.toString().replace('[', '(').replace(']', ')');
    }

    List<String> getLocalNames(Set<Integer> locals) {
        List<String> result = new ArrayList<String>();
        for (Iterator<Integer> i = locals.iterator(); i.hasNext();)
            result.add(getNameOfLocal(i.next()));
        return result;
    }

    Set<Integer> getAccessedLocals() {
        return accessedLocalsByIndex.keySet();
    }

    static String getSimpleClassName(Type type) {
        String name = type.getClassName();
        if (!name.contains("."))
            return name;
        return name.substring(name.lastIndexOf('.') + 1, name.length());
    }

    boolean isThis(int operand) {
        return operand == 0;
    }

    boolean isLocalReadOnly(int local) {
        return isThis(local) || !accessedLocalsByIndex.get(local).mutable;
    }

    void makeLocalMutableFromLambda(int local) {
        if (accessedLocalsByIndex.containsKey(local))
            accessedLocalsByIndex.get(local).mutable = true;
    }

    class VariableInfo {
        String name;
        Type type;
        boolean mutable;
    }

    class LambdaInfo {
        Set<Integer> accessedLocals = new HashSet<Integer>();
        Set<String> parameters = new LinkedHashSet<String>();
        Set<String> definedParameters = new HashSet<String>();
        Type type;

        void accessLocal(int operand) {
            VariableInfo local = accessedLocalsByIndex.get(operand);
            if (local == null) {
                local = new VariableInfo();
                accessedLocalsByIndex.put(operand, local);
            }
            accessedLocals.add(operand);
        }

        void setType(Type type) {
            this.type = type;
        }

        void setParameterInfo(String name, Type type) {
            parameters.add(name);
        }

        String getParametersString() {
            return toParameterString(parameters);
        }

        int getParameterIndex(String name) {
            return new ArrayList<String>(parameters).indexOf(name) + 1;
        }

        boolean isParameterDefined(String name) {
            return definedParameters.contains(name);
        }

        boolean hasParameter(String name) {
            return parameters.contains(name);
        }

        boolean allParametersAreDefined() {
            return definedParameters.equals(parameters);
        }

        void defineParameter(String name) {
            definedParameters.add(name);
        }

        int getArity() {
            return parameters.size();
        }

        String getInternalName() {
            return type.getInternalName();
        }

        String getFieldNameForLocal(int local) {
            return isThis(local) ? "this$0" : "val$" + local;
        }
    }
}