# Enumerable.java 0.1.0

Copyright 2010 Håkan Råberg - Released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).


Ruby/Smalltalk style internal iterators for Java 5 using bytecode transformation to capture expressions as closures.

http://github.com/hraberg/enumerable/


## Introduction

Enumarable allows you to write blocks in valid Java like this:

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


Block parameters are defined using annotated static fields. For more examples see [EnumerableExample](http://github.com/hraberg/enumerable/blob/master/src/test/java/lambda/enumerable/EnumerableExample.java) which has plenty of comments.

**Note: The actual blocks are limited to one expression.** 

## Usage

Enumerable.java is packaged as a [java agent](http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html). ASM has been moved to a local package (lambda.asm).

    java -javaagent:enumerable-agent-0.1.0.jar [...]


Look at [LamdaLoader](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/weaving/LambdaLoader.java) if you have different class loading needs.

This file is also the actual Enumerable.java library itself, and is needed as a compile time dependency.

The API is very similar to the [Enumerabe module in Ruby](http://ruby-doc.org/core/classes/Enumerable.html). You will be mainly importing static methods and fields from [Enumerable](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/Enumerable.java), [Lambda](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Lambda.java) and [Parameters](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Parameters.java)

If you're using Eclipse, you can add the agent as a default VM argument under Installed JREs. You can also add Lambda, Paramters and Enumerable as Favorites in the Java Content Assist settings. Finally, you can create a Java Editor Template to easier insert closures in your code, see *lambda.Lambda* for an example.

Enumerable.java requires your classes to have local variable debugging info (-g:vars or -g in javac).

### Binary Distribution

`enumerable-agent-0.1.0.jar` is both the actual library, and the java agent enabling load time weaving of lambdas.

The binary distribution, when downloaded as a .tgz archive, or built using `ant dist`, doubles as an example project which can be directly imported into Eclipse.
Open *lambda.enumerable.EnumerableExample* to get started. The example bootstraps itself if needed, so you don't need to configure the javaagent. There's also a `build.xml` in the `example` folder.

### Verifier

If you add [`asm-all-3.2.jar`](http://forge.ow2.org/project/download.php?group_id=23&file_id=12944) to your classpath, classes will be verified before loaded into the JVM and problems will be logged to the console.

### System Properties

* `lambda.weaving.debug` - will log to System.out and write all generated classes to disk if set to true.
* `lambda.weaving.debug.classes.dir` - where to write the classes. Defaults to `target/generated-classes`.
* `lambda.weaving.skipped.packages` - is a comma separeted list of package prefixes to skip.

### LambdaParameter

You probably want to use the *@LambdaParameter* annotation to mark fields of your own types to be used in blocks via static imports:

    public class MyDomainLambdaParameters {
        @LambdaParameter
        public static Money m;
    }

Accessing a static field marked with *@LambdaParameter* outside of a block will either start a new block or throw an exception depending on the situation. The fields are never really used, as all accesses are redirected.

## Implementation

Enumerable.java uses Ant to build. Run `ant tests`, `ant example` or `ant agent-jar`.

The transformation is implemented in two passes. The first pass identifies all blocks and their arities and which local variables they access, if any. The second pass does the actual transformation, which has three main elements:

* Moving the actual block expression into a new inner class implementing Fn1, Fn2 or F3.
* Wrapping any accessed local variables in arrays which are passed into the block constructor.
* Replacing the original expression with code that constructs the new block.

To understand the transformation better, a good point to start is running `ant example -Dlambda.weaving.debug=true`.

### How expressions are transformed into blocks

Take this block:

    import static lambda.enumerable.Enumerable.*;
    import static lambda.Lambda.*;

    // ...

    each(strings, fn(s, out.printf("Country: %s\n", s)));
    
The first pass starts by looking for any static fields marked with the *@LambdaParameter* annotation. Once it sees access to one, *s* in this case, it will start moving the code into a new *Fn1* (or *Fn2*) implementation. A block ends with a call to a static method marked with *@NewLambda*: *fn*. (Remember when reading the code that all arguments are (obviously) evaluated before the method call, so *s* is accessed first, and *fn* called last.)

The first pass also keeps track of any local varible that is accessed from within a block, so that it can be wrapped in an array when initialized. This allows the block to properly close over local variables.

The Enumerable methods themselves are implemented using plain old Java. You can call them using normal anonymous inner classes (as seen in the beginning of this document).

## Notes on the Architecture

Enumerable.java has 3 layers:

#### lambda.weaving - transforms expressions into anonymous inner classes

This layer uses ASM, and is directed by the annotations *@LambdaParameter*, and *@NewLambda* and the class *Unused* (for parameters). It's not coupled to the layer above, and you can build your own bridge layer by using these annotations.

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


BlocksInJava, old c2 wiki article about everyones favorite missing feature:
http://c2.com/cgi/wiki?BlocksInJavaIntro


My blog post which outlined my final try using Dynamic Proxies:
http://www.jroller.com/ghettoJedi/entry/using_hamcrest_for_iterators


A library that's quite close to what I suggested there is LambdaJ:
http://code.google.com/p/lambdaj/

## License

Enumerable.java is released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).

ASM 3.2 is Copyright (c) 2000-2005 INRIA, France Telecom, see [asm-3.2.license](http://github.com/hraberg/enumerable/blob/master/lib/asm-3.2.license) or [ASM License](http://asm.ow2.org/license.html).
