# Enumerable.java

Copyright 2010 Håkan Råberg - Released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).


Ruby/Smalltalk style internal iterators for Java 5 using bytecode transformation to capture expressions as closures.


## Introduction

Enumarable allows you to write blocks in valid Java like this:

    Fn1 square = fn(n, n * n);
    List<Integer> result = collect(integers, square);

  
Which is expanded using the [ASM Toolkit for Bytecode Manipulation](http://asm.ow2.org/) into this:

    Fn1 square = new Fn1() {
        public Object call(Object arg) {
            return ((Integer) arg) * n;
        }
    };
    List<Integer> result = collect(integers, square);


Closure works as expected, by trasforming local variables to arrays:

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

    java -javaagent:enumerable-agent.jar [...]


Look at [LamdaLoader](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/weaving/LambdaLoader.java) if you have different class loading needs.

The API is very similar to the [Enumerabe module in Ruby](http://ruby-doc.org/core/classes/Enumerable.html). You will be mainly importing static methods and fields from [Enumerable](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/Enumerable.java) and [Lambda](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Lambda.java).

If you're using Eclipse, you can add the agent as a default VM argument under Installed JREs. You can also add Lambda and Enumerable as Favorites in the Java Content Assist settings.

### System Properties

* `lambda.weaving.debug` - will log to System.err and write all generated classes to disk if set to true.
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

* Moving the actual block expression into a new inner class implementing Fn1 or Fn2.
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
