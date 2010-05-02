# Enumerable.java

Copyright 2010 Håkan Råberg - Released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).


Ruby/Smalltalk style internal iterators for Java 5 using bytecode transformation to capture expressions as closures.

http://github.com/hraberg/enumerable/


## Introduction

Enumerable allows you to write blocks in valid Java like this:

    Fn1 square = fn(n, n * n);
    List<Integer> result = collect(integers, square);

  
Which is expanded using the [ASM Toolkit for Bytecode Manipulation](http://asm.ow2.org/) into this:

    Fn1 square = new Fn1() {
        public Object call(Object arg) {
            return (Integer) arg * (Integer) arg;
        }
    };
    List<Integer> result = collect(integers, square);


Closure works as expected, by transforming local variables to arrays:

    int i = 0;
    fn(n, i += n).call(10);
    assert i == 10;

    
Becomes:

    final int[] i = new int[] { 0 };
    new Fn1() {
        public Object call(Object arg) {
            return i[0] += (Integer) arg;
        }
    }.call(10);
    assert i[0] == 10;


Block parameters are defined using annotated static fields. For more examples see [EnumerableExample](http://github.com/hraberg/enumerable/blob/master/src/example/java/lambda/enumerable/EnumerableExample.java) which has plenty of comments.

**Note: The actual blocks are limited to one expression.** 

## Usage

Enumerable.java is packaged as a [java agent](http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html). ASM has been moved to a local package (lambda.asm).

    java -javaagent:enumerable-agent-<version>.jar [...]


Look at [LamdaLoader](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/weaving/LambdaLoader.java) if you have different class loading needs.

This file is also the actual Enumerable.java library itself, and is needed as a compile time dependency.

The API is very similar to the [Enumerabe module in Ruby](http://ruby-doc.org/core/classes/Enumerable.html). You will be mainly importing static methods and fields from [Enumerable](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/Enumerable.java), [Lambda](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Lambda.java) and [Parameters](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Parameters.java)

If you're using Eclipse, you can add the agent as a default VM argument under Installed JREs. You can also add Lambda, Paramters and Enumerable as Favorites in the Java Content Assist settings. Finally, you can create a Java Editor Template to easier insert closures in your code, see *lambda.Lambda* for an example.

Enumerable.java requires your classes to have local variable debugging info (-g:vars or -g in javac).

### AOT Compilation

To avoid the use of the agent, or any other non standard class loading, you can compile your lambdas ahead of time like this:

    java -cp <project class path> lambda.weaving.LambdaCompiler project.jar

The jar will be compiled and rebuilt in place. The runtime dependency on `enumerable-agent-<version>.jar` as a library remains after AOT compilation.

See the targets `aot-compile-tests` and `aot-tests` in [build.xml](http://github.com/hraberg/enumerable/blob/master/build.xml) for an example.

### Binary Distribution

`enumerable-agent-<version>.jar` is both the actual library, and the java agent enabling load time weaving of lambdas.

The binary distribution, when downloaded as a .tgz archive, or built using `ant dist`, doubles as an example project which can be directly imported into Eclipse.
Open *lambda.enumerable.EnumerableExample* to get started. The example bootstraps itself if needed, so you don't need to configure the javaagent. There's also a `build.xml` in the `example` folder, which includes targets for AOT compilation.

### Verifier

*Disabled right now, use lambda.weaving.debug.dev instead*

If you add [`asm-all-3.2.jar`](http://forge.ow2.org/project/download.php?group_id=23&file_id=12944) to your classpath, classes will be verified before loaded into the JVM and problems will be logged to the console.

### System Properties

* `lambda.weaving.debug` - will log to System.out and write all generated classes to disk if set to true.
* `lambda.weaving.debug.classes.dir` - where to write the classes. Defaults to `target/generated-classes`.
* `lambda.weaving.debug.dev` - will log lots of ASMified information about the transfromation to System.out if set to true.
* `lambda.weaving.skipped.packages` - is a comma separated list of package prefixes to skip.

### LambdaParameter

You probably want to use the [@LambdaParameter](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/annotation/LambdaParameter.java) annotation to mark fields of your own types to be used in blocks via static imports:

    public class MyDomainLambdaParameters {
        @LambdaParameter
        public static Money m;
    }

Accessing a static field marked with *@LambdaParameter* outside of a block will either start a new block or throw an exception depending on the situation. The fields are never really used, as all accesses are redirected.

### NewLambda

Enumerable.java is not tied to the [Fn0](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Fn0.java) hierarchy or function classes. Any single abstract method interface or class can be implemented as a Lambda using *@NewLambda*:

    public class MyDomainLambdas {
        @NewLambda
        public static <R> Callable<R> callable(R block) {
            throw new LambdaWeavingNotEnabledException();
        }
    }

This allows you to create a new anonymous instance of a *Callable* like this:

    Callable<String> c = callable("you called?");

The call to the method marked with [@NewLambda](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/annotation/NewLambda.java) will be replaced with the creation of a new anonymous instance at runtime, as seen in the beginning of this document.

Alternatively, you can create an instance of any single abstract method interface or class like this:

    @LambdaParameter
    static ActionEvent event;

    // ...

    ActionListener a = delegate(event, out.printf(event + "\n"));

This approach works best for functions which always take the same non generic type, like *ActionEvent* here.

### Proxies

Once created, a lambda can be turned into an invocation handler for a proxy like this:

    @LambdaParameter
    static KeyEvent event;

    // ...

    KeyListener listener = fn(event, out.printf(event + "\n")).as(KeyListener.class);

The version above forwards all calls to the lambda. You can also limit the calls to be forwarded like this:

    KeyListener keyTyped = fn(event, out.printf(event + "\n")).as(KeyListener.class, ".*Typed", EventObject.class);

Now only calls matching the regular expression and the specidifed argument types will be forwarded.

### Default Parameter Values

Parameters to Fn1, Fn2 and Fn3 can have default values:

    Fn2<Double, Double, Double> nTimesMorPI = fn(n, m = Math.PI, n * m);
    assert 2.0 * Math.PI == nTimesMorPI.call(2.0);

The default value expression is captured as the expression assigned to the static field marked with *@LambdaParameter*, and can be more complex than just accessing a constant value like in this example.

### Concurrency JSR-166 (for Java 6)

[LamdaOps](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/extra166y/LambdaOps.java) allows you to create lambdas implementing interfaces from [extra166y.Ops](http://gee.cs.oswego.edu/dl/jsr166/dist/extra166ydocs/extra166y/Ops.html) to be used with [extra166y.ParallelArray](http://gee.cs.oswego.edu/dl/jsr166/dist/extra166ydocs/extra166y/ParallelArray.html).
You need to have `jsr166y.jar` and `extra166y.jar` on your class path. They can be downloaded from the [Concurrency JSR-166 Interest Site](http://gee.cs.oswego.edu/dl/concurrency-interest/index.html). They can also be found in this repository in [`lib`](http://github.com/hraberg/enumerable/tree/master/lib/).

The *LambdaOps* class is an example of a collection of static factory methods marked with *@NewLambda* as mentioned above. You can create your own factory classes in a similar way, Enumerable.java has no special support for the interfaces in [Ops](http://gee.cs.oswego.edu/dl/jsr166/dist/extra166ydocs/extra166y/Ops.html).

### Clojure Sequences and IFns

[LambdaClojure](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/clojure/LambdaClojure.java) allows you to create lambdas implementing the interface [clojure.lang.IFn](http://github.com/richhickey/clojure/blob/1.1.0/src/jvm/clojure/lang/IFn.java) to be used directly with Clojure or via [ClojureSeqs](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/clojure/ClojureSeqs.java) which acts as a facade for the [Clojure Seq library](http://clojure.org/sequences). You need clojure-1.1.0.jar or later on your class path. Download from [clojure.org](http://code.google.com/p/clojure/downloads/list).

### JRuby Blocks

[LambdaJRuby](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/jruby/LambdaJRuby.java) allows you to create lambdas extending [RubyProc](http://github.com/jruby/jruby/blob/1.4.0/src/org/jruby/RubyProc.java). You need jruby-1.4.0.jar or later on your class path. Download from [jruby.org](http://www.jruby.org/download).

### JavaScript (for Java 6)

[LambdaJavaScript](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/javascript/LambdaJavaScript.java) allows you to create lambdas extending  [Function](http://mxr.mozilla.org/mozilla/source/js/rhino/src/org/mozilla/javascript/Function.java). Uses [Rhino 1.6r2](http://www.mozilla.org/rhino/) which comes with Java 6.

### Google Collections

[LambdaGoogleCollections](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/googlecollect/LambdaGoogleCollections.java) allows you to create lambdas implementing [Function](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Function.html), [Predicate](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Predicate.html) and [Supplier](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Supplier.html) from [Google Collections](http://code.google.com/p/google-collections/). You need google-collect-1.0.jar on your class path. Download from [Google Collections](http://google-collections.googlecode.com/files/).

## Implementation

[Enumerable](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/Enumerable.java) and [EnumerableArrays](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/EnumerableArrays.java) act as a facades for the implementation in [EnumerableModule](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/collection/EnumerableModule.java) and [EMap](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/collection/EMap.java).

Enumerable.java uses Ant to build. Run `ant tests`, `ant example` or `ant agent-jar`.

The transformation is implemented in two passes. The first pass identifies all blocks and their arities and which local variables they access, if any. The second pass does the actual transformation, which has three main elements:

* Moving the actual block expression into a new inner class implementing the return type of the *@Newlambda* factory method, which is assumed to have one single abstract method for the lambda to override.
* Passing any accessed local variables into the block constructor. Mutable variables are wrapped in arrays.
* Replacing the original expression with code that constructs the new block.

To understand the transformation better, a good point to start is running `ant example -Dlambda.weaving.debug=true`.

### How expressions are transformed into blocks

Take this block:

    import static lambda.enumerable.Enumerable.*;
    import static lambda.Lambda.*;

    // ...

    each(strings, fn(s, out.printf("Country: %s\n", s)));
    
The first pass starts by looking for any static fields marked with the *@LambdaParameter* annotation. Once it sees access to one, *s* in this case, it will start moving the code into a new *Fn1* (or *Fn2*) implementation. A block ends with a call to a static method marked with *@NewLambda*: *fn*. (Remember when reading the code that all arguments are (obviously) evaluated before the method call, so *s* is accessed first, and *fn* called last.)

The first pass also keeps track of any local variable that is accessed from within a block, so that it can be wrapped in an array when initialized. This allows the block to properly close over local variables.

The Enumerable methods themselves are implemented using plain old Java. You can call them using normal anonymous inner classes (as seen in the beginning of this document).

## Notes on the Architecture

Enumerable.java has 3 layers:

#### lambda.weaving - transforms expressions into anonymous inner classes

This layer uses ASM, and is directed by the annotations *@LambdaParameter* and *@NewLambda*. It's not coupled to the layer above, and you can build your own bridge layer by using these annotations.

#### lambda - simple functional programming constructs

This layer is normal Java and can be used on it's own. It also uses the annotated class *lambda.Lambda* to direct the weaving process, if enabled.

This layer mainly exists to simplify implemention of bridges from the user facing closures to an actual Java API. If you want to use Enumerable.java closures for another library, you can wrap or implement its API using this layer as a starting point.

#### lambda.enumerable - a port of Ruby's Enumerable module

This layer is also normal Java and has no knowledge of the bytecode weaving.

## Links

Ruby Enumerable:
http://ruby-doc.org/core/classes/Enumerable.html


Closures for JDK7, a Straw-Man Proposal:
http://cr.openjdk.java.net/~mr/lambda/straw-man/


BlocksInJava, old c2 wiki article about everyone's favorite missing feature:
http://c2.com/cgi/wiki?BlocksInJavaIntro


My blog post which outlined my final try using Dynamic Proxies:
http://www.jroller.com/ghettoJedi/entry/using_hamcrest_for_iterators


A library that's quite close to what I suggested there is LambdaJ:
http://code.google.com/p/lambdaj/

## License

Enumerable.java is released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).

ASM 3.2 is Copyright (c) 2000-2005 INRIA, France Telecom, see [asm-3.2.license](http://github.com/hraberg/enumerable/blob/master/lib/asm-3.2.license) or [ASM License](http://asm.ow2.org/license.html).

jsr166y and extra166y:
Written by Doug Lea with assistance from members of JCP JSR-166 Expert Group and released to the public domain, as explained at http://creativecommons.org/licenses/publicdomain

Clojure, Copyright (c) Rich Hickey, released under the [EPL license](http://opensource.org/licenses/eclipse-1.0.php).

JRuby is Copyright (c) 2007-2010 The JRuby project, and is released under a [tri CPL/GPL/LGPL license](http://github.com/jruby/jruby/blob/1.4.0/COPYING).

Rhino is Copyright (C) 1997-1999 Norris Boyd/Netscape Communications Corporation. The majority of Rhino is MPL 1.1 / GPL 2.0 dual licensed.

Google Collections, Copyright (C) 2008 Google Inc. Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
