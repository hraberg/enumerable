package lambda.extra166y;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

import lambda.annotation.NewLambda;
import lambda.annotation.Unused;
import lambda.exception.LambdaWeavingNotEnabledException;
import extra166y.Ops;

class LambdaOpsGenerator {
    public static void main(String[] args) throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);

        out.println("package lambda.extra166y;");
        out.println("");
        out.println("import static " + Ops.class.getName() + ".*;");
        out.println("");
        out.println("import " + LambdaWeavingNotEnabledException.class.getName() + ";");
        out.println("import " + Unused.class.getName() + ";");
        out.println("import " + NewLambda.class.getName() + ";");
        out.println();

        out.println("/**");
        out.println(" * This is class is similar {@link lambda.Lambda}, but instead of creating lambdas");
        out
                .println(" * inheriting from {@link lambda.Fn0} it creates lambdas implementing the interfaces defined in");
        out.println(" * {@link extra166y.Ops}, to be used together with {@link extra166y.ParallelArray}.");
        out.println(" * <p>");
        out.println(" * <i>This file was generated by " + LambdaOpsGenerator.class.getName() + ".</i>");
        out.println(" */");
        out.println("public class LambdaOps {");

        for (Class<?> anInterface : Ops.class.getClasses()) {
            out.println("");
            out.println("    @NewLambda");
            out.print("    public static ");

            TypeVariable<?>[] typeParameters = anInterface.getTypeParameters();
            if (typeParameters.length > 0) {
                out.print("<");
                for (int i = 0; i < typeParameters.length; i++) {
                    out.print(typeParameters[i].getName());
                    if (i < typeParameters.length - 1)
                        out.print(", ");
                }
                out.print("> ");
            }
            out.print(anInterface.getSimpleName());
            if (typeParameters.length > 0) {
                out.print("<");
                for (int i = 0; i < typeParameters.length; i++) {
                    out.print(typeParameters[i].getName());
                    if (i < typeParameters.length - 1)
                        out.print(", ");
                }
                out.print("> ");
            }

            String simpleName = anInterface.getSimpleName();

            if (simpleName.endsWith("Procedure"))
                out.print(" procedure(");
            else if (simpleName.endsWith("Reducer"))
                out.print(" reducer(");
            else if (simpleName.endsWith("Comparator"))
                out.print(" comparator(");
            else if (simpleName.endsWith("Action"))
                out.print(" action(");
            else if (simpleName.endsWith("Generator"))
                out.print(" op(");
            else if (simpleName.endsWith("Predicate"))
                out.print(" op(");
            else
                out.print(" op(");

            Method op = anInterface.getMethods()[0];
            Class<?>[] parameterTypes = op.getParameterTypes();

            if (parameterTypes.length == 0) {
                out.print("Unused _, ");
            }

            int arg = 1;
            int typeParameter = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (parameterType == Object.class) {
                    int index = typeParameter == typeParameters.length ? typeParameters.length - 1
                            : typeParameter++;
                    out.print(typeParameters[index].getName());
                } else
                    out.print(parameterType.getName());
                out.print(" a" + arg++);
                if (i < parameterTypes.length - 1)
                    out.print(", ");
            }
            Class<?> returnType = op.getReturnType();
            int index = typeParameter == typeParameters.length ? typeParameters.length - 1 : typeParameter;
            if (parameterTypes.length != 0)
                out.print(", ");
            out.print(returnType == Void.TYPE ? "Object block"
                    : (returnType == Object.class ? typeParameters[index].getName() : returnType.getName())
                            + " block");
            out.println(") {");
            out.println("        throw new LambdaWeavingNotEnabledException();");
            out.println("    }");
        }
        out.println("}");

        FileOutputStream f = new FileOutputStream("src/main/java/lambda/extra166y/LambdaOps.java");
        f.write(stringWriter.toString().getBytes("UTF-8"));
        f.close();

        System.out.println(stringWriter);
    }
}
