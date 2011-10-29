package org.enumerable.lambda.weaving;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static org.enumerable.lambda.exception.UncheckedException.uncheck;


public class InMemoryCompiler {
    public static boolean useECJ = Boolean.valueOf(getProperty("lambda.support.expression.useECJ"));
    static JavaCompiler compiler;

    static boolean expressionSupportEnabled;
    
    static {
        try {
            Class.forName("japa.parser.JavaParser");
            expressionSupportEnabled = true;
            compiler = createCompiler();
        } catch (ClassNotFoundException expressionSupportNotEnabled) {
        }
    }

    public static Map<String, byte[]> bytesByClassName = new HashMap<String, byte[]>();

    public static void registerLambda(String name, byte[] bs) {
        if (expressionSupportEnabled)
            bytesByClassName.put(name, bs);
    }

    static JavaCompiler createCompiler() {
        try {
            if (useECJ)
                return (JavaCompiler) Class.forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler")
                        .newInstance();
            return ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    private boolean debugInfo = true;
    
    public InMemoryCompiler debugInfo(boolean debugInfo){
        this.debugInfo = debugInfo;
        return this;
    }
    
    public Class<?> compile(String className, String source) throws IOException {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaFileManager manager = new ForwardingJavaFileManager<StandardJavaFileManager>(compiler
                .getStandardFileManager(null, null, null)) {
            public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
                    FileObject sibling) throws IOException {
                return new ByteArrayFileObject(className.replace('/', '.'));
            }
        };

        JavaFileObject file = new JavaSourceFromString(className, source);
        List<String> options = new ArrayList<String>(asList("-source", "1.5", "-target", "1.5"));
        
        if (debugInfo){
            options.add("-g");
        }
        
        if (useECJ) {
            options.add("-warn:-raw");
            options.add("-warn:-deadCode");
            options.add("-warn:-serial");
        }

        CompilationTask task = compiler.getTask(null, manager, diagnostics, options, null, Arrays.asList(file));

        boolean success = task.call();

        if (success) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw uncheck(e);
            }
        } else {
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic.getCode());
                System.out.println(diagnostic.getKind());
                System.out.println(diagnostic.getPosition());
                System.out.println(diagnostic.getStartPosition());
                System.out.println(diagnostic.getEndPosition());
                System.out.println(diagnostic.getSource());
                System.out.println(diagnostic.getMessage(null));
            }
            return null;
        }
    }

    static class ByteArrayFileObject extends SimpleJavaFileObject {
        String className;

        ByteArrayFileObject(String className) {
            super(URI.create("file://" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
            this.className = className;
        }

        public OutputStream openOutputStream() throws IOException {
            return new ByteArrayOutputStream() {
                public void close() throws IOException {
                    super.close();
                    bytesByClassName.put(className, toByteArray());
                    new ClassInjector().inject(getClass().getClassLoader(), className, toByteArray());
                }
            };
        }
    }

    static class JavaSourceFromString extends SimpleJavaFileObject {
        String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("file:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}