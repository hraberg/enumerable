package org.enumerable.lambda.support.scala;

import static java.util.Arrays.*;
import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.support.scala.LambdaScala.*;
import static org.junit.Assert.*;

import groovy.lang.Closure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;


import org.enumerable.lambda.Fn1;
import org.enumerable.lambda.Lambda;
import org.enumerable.lambda.enumerable.Enumerable;
import org.enumerable.lambda.support.clojure.ClojureTest;
import org.enumerable.lambda.support.clojure.LambdaClojure;
import org.enumerable.lambda.support.groovy.GroovyTest;
import org.enumerable.lambda.support.groovy.LambdaGroovy;
import org.enumerable.lambda.support.javascript.JavaScriptTest;
import org.enumerable.lambda.support.javascript.LambdaJavaScript;
import org.enumerable.lambda.support.jruby.JRubyTest;
import org.enumerable.lambda.support.jruby.LambdaJRuby;
import org.enumerable.lambda.support.scala.EnumerableJavaScalaTest;
import org.enumerable.lambda.support.scala.FunctionFn1;
import org.jruby.RubyProc;
import org.junit.Test;

import scala.Function1;
import scala.Function2;
import scala.tools.nsc.Interpreter;
import scala.tools.nsc.Settings;
import sun.org.mozilla.javascript.internal.Function;
import clojure.lang.IFn;

@SuppressWarnings("unchecked")
public class ScalaTest {
    static ScalaInterpreter scala = new ScalaInterpreter();

    @Test
    public void interactingWithScala() {
        scala.bind("f", "Function1[Int, Int]", function(n, n * 2));
        assertEquals(4, scala.eval("f(2);"));

        scala.bind("f", "Function1[Boolean, Boolean]", function(b, !b));
        assertTrue((Boolean) scala.eval("f(false);"));

        scala.bind("f", "Function1[String, String]", function(s, s.toUpperCase()));
        assertEquals("HELLO", scala.eval("f(\"hello\");"));

        scala.bind("f", "Function[Any, Any]", function(obj, obj));
        assertNull(scala.eval("f(null);"));
    }

    @Test
    public void fullScalaFunctionTypesAreCreatedWithNoAbstractMethods() {
        FunctionFn1<Integer, Integer> function = function(n, n * 2);
        Function1<Integer, Integer> composed = function.compose(function);
        assertEquals(8, (int) composed.apply(2));
    }

    @Test
    public void convertFnToFunction() throws ScriptException {
        Function1<String, String> f = toFunction(Lambda.λ(s, s.toUpperCase()));
        scala.bind("f", "Function1[String, String]", f);
        assertEquals("HELLO", scala.eval("f(\"hello\");"));
    }

    @Test
    public void convertFunctionToFn() throws ScriptException {
        Function1<String, String> f = (Function1<String, String>) scala.eval("(s: String) => s.toUpperCase");
        assertEquals("HELLO", toFn1(f).call("hello"));
    }

    @Test
    public void interactingWithEnumerableJava() throws Exception {
        List<Integer> list = asList(1, 2, 3);
        Fn1<Integer, Integer> block = toFn1((Function1<Integer, Integer>) scala.eval("(n: Int) => n * 2"));
        assertEquals(asList(2, 4, 6), Enumerable.collect(list, block));
    }

    @Test
    public void interactingWithClojure() throws Exception {
        IFn star = (IFn) ClojureTest.getClojureEngine().eval("*");
        Function2<Object, Object, Object> times = toFunction(LambdaClojure.toFn2(star));

        assertEquals(6, times.apply(2, 3));

        scala.bind("timesClojure", "Function2[Any, Any, Any]", times);
        assertEquals(120, scala.eval("List(1, 2, 3, 4, 5).reduceLeft(timesClojure)"));
    }

    @Test
    public void interactingWithJRuby() throws Exception {
        RubyProc proc = (RubyProc) JRubyTest.getJRubyEngine().eval(":*.to_proc");
        Function2<Object, Object, Object> times = toFunction(LambdaJRuby.toFn2(proc));

        assertEquals(6L, times.apply(2, 3));

        scala.bind("timesRuby", "Function2[Any, Any, Any]", times);
        assertEquals(120L, scala.eval("List(1, 2, 3, 4, 5).reduceLeft(timesRuby)"));
    }

    @Test
    public void interactingWithJavaScript() throws Exception {
        ScriptEngine js = JavaScriptTest.getJavaScriptEngine();

        Function f = (Function) js.eval("var f = function(n, m) { return n * m; }; f;");
        Function2<Object, Object, Object> times = toFunction(LambdaJavaScript.toFn2(f));

        assertEquals(6.0, times.apply(2, 3));

        scala.bind("timesJS", "Function2[Any, Any, Any]", times);
        assertEquals(120.0, scala.eval("List(1, 2, 3, 4, 5).reduceLeft(timesJS)"));
    }

    @Test
    public void interactingWithGroovy() throws Exception {
        ScriptEngine groovy = GroovyTest.getGroovyEngine();

        Closure closure = (Closure) groovy.eval("{ n, m -> n * m }");
        Function2<Object, Object, Object> times = toFunction(LambdaGroovy.toFn2(closure));

        assertEquals(6, times.apply(2, 3));

        scala.bind("timesGroovy", "Function2[Any, Any, Any]", times);
        assertEquals(120, scala.eval("List(1, 2, 3, 4, 5).reduceLeft(timesGroovy)"));
    }
    
    @Test
    public void enumerableJavaLambdasCanBeEmbeddedInScala() throws Exception {
        //    def toUpperCase() = {
        //        λ(s, s.toUpperCase)
        //    }
        assertEquals("HELLO", EnumerableJavaScalaTest.toUpperCase().call("hello"));
    }
    

    public static class ScalaInterpreter {
        static Interpreter interpreter;

        static {
            Settings settings = new Settings();
            settings.classpath()
                    .v_$eq("lib/scala-library-2.8.0.final.jar" + File.pathSeparator
                            + "lib/scala-compiler-2.8.0.final.jar");
            interpreter = new Interpreter(settings);
        }

        public void bind(String name, String scalaType, Object value) {
            PrintStream realOut = System.out;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                System.setOut(new PrintStream(out));
                interpreter.bind(name, scalaType, value);
            } catch (RuntimeException e) {
                realOut.println(out);
                throw e;
            } finally {
                System.setOut(realOut);
            }
        }

        public Object eval(String expression) {
            PrintStream realOut = System.out;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                System.setOut(new PrintStream(out));
                Object[] _result = new Object[1];
                bind("_result", "Array[Any]", _result);
                interpreter.interpret("_result(0) = " + expression);
                return _result[0];
            } catch (RuntimeException e) {
                realOut.println(out);
                throw e;
            } finally {
                System.setOut(realOut);
            }
        }
    }

    public static ScalaInterpreter getScalaInterpreter() {
        return scala;
    }
}
