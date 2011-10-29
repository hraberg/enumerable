package org.enumerable.lambda.weaving;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;


import org.enumerable.lambda.weaving.InMemoryCompiler;
import org.enumerable.lambda.weaving.LambdaLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ErrorHandlingTest {
    InMemoryCompiler compiler = new InMemoryCompiler();
    PrintStream realErr;
    ByteArrayOutputStream err;

    @Before
    public void redirectStdErr() {
        realErr = System.err;
        err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
    }

    @After
    public void restoreStdErr() {
        System.setErr(realErr);
    }
    
    @After
    public void forgetTrasformationFailed() throws Exception {
        Field transformationFailed = LambdaLoader.class.getDeclaredField("transformationFailed");
        transformationFailed.setAccessible(true);
        transformationFailed.set(null, false);
    }
    
    @Test
    public void givesExceptionForInstanceLambdaParamter() throws Exception {
        String className = "InstanceLambdaParamter";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("import static org.enumerable.lambda.Lambda.*;");
        out.println("import org.enumerable.lambda.annotation.LambdaParameter;");
        out.println("class " + className + " {");
        out.println("  @LambdaParameter");
        out.println("  public String instance;");
        out.println("  public void method() {");
        out.println("    λ(instance, instance);");
        out.println("  }");
        out.println("}");
        out.close();

        assertNotNull((Class<?>) compiler.compile(className, writer.toString()));
        assertErrContains("IllegalStateException: Tried to define non static lambda parameter instance at line ");
    }

    @Test
    public void givesExceptionForStaticNotAnnotatedAsLambdaParamter() throws IOException {
        String className = "NotAnnotatedAsLambdaParamter";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("import static org.enumerable.lambda.Lambda.*;");
        out.println("import org.enumerable.lambda.annotation.LambdaParameter;");
        out.println("class " + className + " {");
        out.println("  public static String noAnnotation;");
        out.println("  public void method() {");
        out.println("    λ(noAnnotation, noAnnotation);");
        out.println("  }");
        out.println("}");
        out.close();

        assertNotNull(compiler.compile(className, writer.toString()));
        assertErrContains("IllegalStateException: Got [] as parameters need exactly 1 at line ");
    }

    @Test
    public void givesExceptionForMethodCallUsedAsLambdaParamter() throws IOException {
        String className = "MethodCallUsedAsLambdaParamter";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("import static org.enumerable.lambda.Lambda.*;");
        out.println("import org.enumerable.lambda.annotation.LambdaParameter;");
        out.println("class " + className + " {");
        out.println("  public String noAnnotation() { return \"\"; };");
        out.println("  public void method() {");
        out.println("    λ(noAnnotation(), noAnnotation());");
        out.println("  }");
        out.println("}");
        out.close();

        assertNotNull(compiler.compile(className, writer.toString()));
        assertErrContains("IllegalStateException: Got [] as parameters need exactly 1 at line ");
    }

    @Test
    public void givesExceptionForInstanceNewLambdaMethod() throws Exception {
        String className = "InstanceNewLambdaMethod";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("import org.enumerable.lambda.exception.LambdaWeavingNotEnabledException;");
        out.println("import org.enumerable.lambda.annotation.NewLambda;");
        out.println("class " + className + " {");
        out.println("  @NewLambda");
        out.println("  public Runnable instance() {");
        out.println("    throw new LambdaWeavingNotEnabledException();");
        out.println("  }");
        out.println("  public void method() {");
        out.println("    Runnable r = instance();");
        out.println("  }");
        out.println("}");
        out.close();

        
        assertNotNull((Class<?>) compiler.compile(className, writer.toString()));
        assertErrContains("IllegalStateException: Tried to call non static new lambda method instance at line ");
    }
    
    @Test
    public void givesExceptionForMissingDebugInfo() throws Exception {
        String className = "CloseOverParameter";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("import static org.enumerable.lambda.Lambda.*;");
        out.println("import org.enumerable.lambda.annotation.NewLambda;");
        out.println("class " + className + " {");
        out.println("  public void method(String param) {");
        out.println("    λ(param);");
        out.println("  }");
        out.println("}");
        out.close();

        compiler.debugInfo(false);
        assertNotNull((Class<?>) compiler.compile(className, writer.toString()));
        assertErrContains("IllegalStateException: Debug information is needed to close over local variables or parameters, please recompile with -g.");
    }
    
    void assertErrContains(String message) {
        if (!err.toString().contains(message)) {
            restoreStdErr();
            System.err.println(err.toString());
            fail();
        }
    }
}
