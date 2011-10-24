package lambda.support.expression;

import static org.junit.Assert.*;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.BlockStmt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lambda.weaving.InMemoryCompiler;

import org.junit.Test;

public class InMemoryCompilerTest {
    InMemoryCompiler compiler = new InMemoryCompiler();

    @Test
    public void inMemoryCompilationFromJavaParserAST() throws Exception {
        CompilationUnit cu = createCU();
        assertEquals("Hello World from AST", invokeMain(compiler.compile(getFullyQualifiedName(cu), cu.toString()))
                .trim());
    }

    @Test
    public void inMemoryCompilationFromPlainString() throws Exception {
        String className = "HelloWorld";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("class " + className + " {");
        out.println("  public static void main(String args[]) {");
        out.println("    System.out.println(\"Hello World from String\");");
        out.println("  }");
        out.println("}");
        out.close();

        assertEquals("Hello World from String", invokeMain(compiler.compile(className, writer.toString())).trim());
    }

    @Test
    public void inMemoryCompilationFromPlainStringViaJavaParserAST() throws Exception {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("class HelloWorldAST {");
        out.println("  public static void main(String args[]) {");
        out.println("    System.out.println(\"Hello World from JavaParser\");");
        out.println("  }");
        out.println("}");
        out.close();

        CompilationUnit cu = JavaParser.parse(new ByteArrayInputStream(writer.toString().getBytes()));
        assertEquals("Hello World from JavaParser", invokeMain(
                compiler.compile(getFullyQualifiedName(cu), cu.toString())).trim());
    }

    @Test
    public void inMemoryCompilationSharesClassPath() throws Exception {
        String className = "HelloWorldSharedClassPath";

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("import japa.parser.*;");
        out.println("class " + className + " {");
        out.println("  public static void main(String args[]) {");
        out.println("    System.out.println(JavaParser.class.getName());");
        out.println("  }");
        out.println("}");
        out.close();

        assertEquals(JavaParser.class.getName(), invokeMain(compiler.compile(className, writer.toString())).trim());
    }

    // This is borrowed from the javaparser examples.
    CompilationUnit createCU() {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("parser.test")));

        ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(0, false, "GeneratedClass");
        ASTHelper.addTypeDeclaration(cu, type);

        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "main");
        method.setModifiers(ModifierSet.addModifier(method.getModifiers(), ModifierSet.STATIC));
        ASTHelper.addMember(type, method);

        Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "args");
        param.setVarArgs(true);
        ASTHelper.addParameter(method, param);

        BlockStmt block = new BlockStmt();
        method.setBody(block);

        NameExpr clazz = new NameExpr("System");
        FieldAccessExpr field = new FieldAccessExpr(clazz, "out");
        MethodCallExpr call = new MethodCallExpr(field, "println");
        ASTHelper.addArgument(call, new StringLiteralExpr("Hello World from AST"));
        ASTHelper.addStmt(block, call);

        return cu;
    }

    String invokeMain(Class<?> aClass) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream realOut = System.out;
        try {
            System.setOut(new PrintStream(out));
            Method main = aClass.getDeclaredMethod("main", new Class[] { String[].class });
            main.setAccessible(true);
            main.invoke(null, (Object) new String[0]);
            return out.toString();
        } finally {
            System.setOut(realOut);
        }
    }

    String getFullyQualifiedName(CompilationUnit cu) {
        String name = cu.getTypes().get(0).getName();
        if (cu.getPackage() != null)
            return cu.getPackage().getName() + "." + name;
        return name;
    }
}
