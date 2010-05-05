package lambda.support.expression;

import static lambda.exception.UncheckedException.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import lambda.weaving.ClassInjector;

public class InMemoryCompiler {
    static Map<String, byte[]> bytesByClassName = new HashMap<String, byte[]>();

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public Class<?> compile(String className, String source) throws IOException {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaFileManager manager = new ForwardingJavaFileManager<StandardJavaFileManager>(compiler
                .getStandardFileManager(null, null, null)) {
            public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
                    FileObject sibling) throws IOException {
                return new ByteArrayFileObject(className);
            }
        };

        JavaFileObject file = new JavaSourceFromString(className, source);
        CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null,
                (Iterable<? extends JavaFileObject>) Arrays.asList(file));

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
            super(URI.create("bytes://" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
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
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}