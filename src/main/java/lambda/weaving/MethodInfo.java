package lambda.weaving;

import java.util.*;

import lambda.annotation.Unused;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.*;

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
        return !accessedLocalsByIndex.get(local).mutable;
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
        Map<String, VariableInfo> parametersByName = new LinkedHashMap<String, VariableInfo>();
        Set<String> definedParameters = new HashSet<String>();
        Type type;
        MethodInfo method;
        Type[] newLambdaParameterTypes;

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
            if (parametersByName.containsKey(name))
                return;
            VariableInfo value = new VariableInfo();
            value.name = name;
            value.type = type;
            value.mutable = true;
            parametersByName.put(name, value);
        }

        String getParametersString() {
            return toParameterString(getParameters());
        }

        String getTypedParametersString() {
            List<String> result = new ArrayList<String>();
            for (String parameter : getParameters())
                result.add(getSimpleClassName(getParameterType(parameter)) + " " + parameter);
            return toParameterString(result);
        }

        int getParameterIndex(String name) {
            return new ArrayList<String>(getParameters()).indexOf(name);
        }

        boolean isParameterDefined(String name) {
            return definedParameters.contains(name);
        }

        boolean hasParameter(String name) {
            return getParameters().contains(name);
        }

        boolean allParametersAreDefined() {
            return definedParameters.equals(getParameters());
        }

        void defineParameter(String name) {
            definedParameters.add(name);
        }

        int getArity() {
            return getParameters().size();
        }

        Type getType() {
            return type;
        }

        Type[] getParameterTypes() {
            List<Type> result = new ArrayList<Type>();
            for (VariableInfo parameter : parametersByName.values())
                result.add(parameter.type);
            return result.toArray(new Type[0]);
        }

        String getFieldNameForLocal(int local) {
            return getNameOfLocal(local) + "$" + local;
        }

        Set<String> getParameters() {
            return parametersByName.keySet();
        }

        Type getParameterType(String name) {
            return parametersByName.get(name).type;
        }

        Type getExpressionType() {
            return newLambdaParameterTypes[newLambdaParameterTypes.length -1];
        }

        void setLambdaMethod(MethodInfo method) {
            this.method = method;
        }

        public MethodInfo getLambdaMethod() {
            return method;
        }

        public int getParameterRealLocalIndex(String name) {
            Type[] parameterTypes = getParameterTypes();
            int index = 1;
            for (int i = 0; i < parameterTypes.length; i++)
                if (getParameterIndex(name) == i)
                    break;
                else
                    index += parameterTypes[i].getSize();
            return index;
        }

        String getParameterByIndex(int index) {
            return new ArrayList<String>(getParameters()).get(index - 1);
        }

        void setNewLambdaParameterTypes(Type[] newLambdaParameterTypes) {
            this.newLambdaParameterTypes = newLambdaParameterTypes;
        }

        Type getNewMethodParameterType(String name) {
            int parameterIndex = getParameterIndex(name);
            int i = 0;
            for (Type type : newLambdaParameterTypes) {
                if (type != Type.getType(Unused.class))
                    if (i == parameterIndex)
                        return type;
                    else
                        i++;
            }
            return null;
        }
    }
}