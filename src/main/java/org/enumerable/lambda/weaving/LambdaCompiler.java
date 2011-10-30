package org.enumerable.lambda.weaving;

import org.enumerable.lambda.weaving.tree.LambdaTreeTransformer;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static java.lang.System.exit;
import static java.lang.System.out;
import static org.enumerable.lambda.exception.UncheckedException.uncheck;
import static org.enumerable.lambda.weaving.ClassFilter.createClassFilter;
import static org.enumerable.lambda.weaving.Debug.debug;
import static org.enumerable.lambda.weaving.Version.getVersionString;


/**
 * AOT-compiler for lambdas, to side-step the need for the agent. Takes a list
 * of class directories/jars and compiles them in place, the originals will be
 * overwritten.
 * <p>
 * The classpath must contain all dependencies for the directories/jars when
 * compiling.
 * <p>
 * The enumerable-agent-<version>.jar will still be as a normal runtime
 * dependency after compilation.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class LambdaCompiler {
    static final String AOT_COMPILED_MARKER = "META-INF/lambda.aot.compiled";

    public static void main(String[] args) throws Exception {
        out.println("[compiler] " + getVersionString());
        if (args.length == 0) {
            out.println("Usage: <jar>|<dir>...");
            return;
        }

        for (String name : args) {
            File file = new File(name);
            if (!file.isDirectory() && !file.getName().endsWith(".jar")) {
                out.println(file.getAbsolutePath() + " is neither a jar or a directory, exiting");
                exit(1);
            }
        }

        new LambdaCompiler().compile(args);
    }

    LambdaTreeTransformer transformer = new LambdaTreeTransformer();
    byte[] buffer = new byte[8 * 1024];

    void compile(String[] args) throws Exception {
        for (String name : args) {
            File file = new File(name);
            debug("compiling " + file.getPath());
            if (file.isDirectory()) {
                compileClassesDirectory(file);

            } else if (file.getName().endsWith(".jar"))
                compilieJar(file);
        }
    }

    private void compileClassesDirectory(File file) throws Exception {
        File aotCompiledMarker = new File(file, AOT_COMPILED_MARKER);
        if (aotCompiledMarker.exists()) {
            out.println(file + " is already compiled, skipping.");
            return;
        }
        aotCompiledMarker.getParentFile().mkdir();
        aotCompiledMarker.createNewFile();

        compileDirectory(file);
        debug("writing generated lambdas in " + file);
        writeGeneratedLambdas(file);
    }

    void compileDirectory(File dir) throws Exception {
        for (File file : dir.listFiles())
            if (file.getName().endsWith(".class"))
                compileFile(file);
            else if (file.isDirectory())
                compileDirectory(file);
    }

    void compileFile(File file) throws Exception {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(file);
            byte[] bs = transformer.transform(ClassLoader.getSystemClassLoader(), createClassFilter(), null, in);
            in.close();
            if (bs != null) {
                out = new FileOutputStream(file);
                out.write(bs);
            }
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException silent) {
                }
            if (out != null)
                try {
                    out.close();
                } catch (IOException silent) {
                }
        }
    }

    void compilieJar(File jar) throws Exception {
        try {
            JarFile jarFile = new JarFile(jar);

            if (jarFile.getEntry(AOT_COMPILED_MARKER) != null) {
                out.println(jar + " is already compiled, skipping.");
                return;
            }

            File tempDir = unjar(jarFile);

            File aotCompiledMarker = new File(tempDir, AOT_COMPILED_MARKER);
            aotCompiledMarker.getParentFile().mkdir();
            aotCompiledMarker.createNewFile();

            compileDirectory(tempDir);
            debug("writing generated lambdas in " + jar);
            writeGeneratedLambdas(tempDir);

            File newJar = jar(tempDir);

            jar.delete();
            newJar.renameTo(jar);

            delete(tempDir);
        } catch (Exception e) {
            throw uncheck(e);
        }
    }

    void delete(File file) {
        if (file.isDirectory())
            for (File child : file.listFiles())
                delete(child);
        file.delete();
    }

    void writeGeneratedLambdas(File tempDir) throws IOException {
        for (Map.Entry<String, byte[]> lambdaClass : transformer.getLambdasByClassName().entrySet()) {
            File lambdaFile = new File(tempDir, lambdaClass.getKey().replace('.', '/'));
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(lambdaFile + ".class");
                out.write(lambdaClass.getValue());
            } finally {
                if (out != null)
                    out.close();
            }

        }
    }

    File jar(File tempDir) throws Exception {
        File newJar = new File(tempDir.getAbsolutePath() + ".jar");
        JarOutputStream out = null;
        try {
            out = new JarOutputStream(new FileOutputStream(newJar));
            addDirToJar(tempDir, tempDir, out);
        } finally {
            if (out != null)
                out.close();
        }
        return newJar;
    }

    void addDirToJar(File baseDir, File tempDir, JarOutputStream out) throws IOException {
        for (File file : tempDir.listFiles()) {
            String nameInJar = file.getPath().substring(baseDir.getPath().length() + 1).replace(File.separatorChar,
                    '/');

            if (file.isDirectory()) {
                JarEntry jarEntry = new JarEntry(nameInJar.endsWith("/") ? nameInJar : nameInJar + "/");
                jarEntry.setTime(file.lastModified());
                out.putNextEntry(jarEntry);
                out.closeEntry();
                addDirToJar(baseDir, file, out);

            } else {
                JarEntry jarEntry = new JarEntry(nameInJar);
                jarEntry.setTime(file.lastModified());
                out.putNextEntry(jarEntry);

                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    int read;
                    while ((read = in.read(buffer)) != -1)
                        out.write(buffer, 0, read);
                    out.closeEntry();
                } finally {
                    if (in != null)
                        in.close();
                }
            }
        }
    }

    File unjar(JarFile jarFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        File tempDir = File.createTempFile("lambda.compiler.jar", "");
        tempDir.delete();
        tempDir.mkdir();

        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {

                JarEntry jarEntry = entries.nextElement();

                File file = new File(tempDir, jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    file.mkdir();
                } else {
                    file.getParentFile().mkdirs();

                    in = jarFile.getInputStream(jarEntry);
                    out = new FileOutputStream(file);
                    int read;
                    while ((read = in.read(buffer)) != -1)
                        out.write(buffer, 0, read);
                    out.flush();
                }
            }
            return tempDir;
        } finally {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            jarFile.close();
        }
    }
}
