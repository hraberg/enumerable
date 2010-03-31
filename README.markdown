# Enumerable.java

Copyright 2010 Håkan Råberg - Released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).


Ruby/Smalltalk style internal iterators for Java 5 using bytecode transformation to capture expressions as closures.


## Introduction:

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


Block parameters are defined using annotated static fields. For more examples see [EnumerableExampleAndRegressionTest](http://github.com/hraberg/enumerable/blob/master/src/test/java/lambda/enumerable/EnumerableExampleAndRegressionTest.java) which has plenty of comments.

**Note: The actual blocks are limited to one expression.** 

## Usage:

Enumerable.java is packaged as a [java agent](http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html). ASM has been moved to a local package (lambda.asm).

    java -javaagent:enumerable-jar-with-dependencies.jar [...]


Look at [LamdaLoader](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/weaving/LambdaLoader.java) if you have different class loading needs.

The API is very similar to the [Enumerabe module in Ruby](http://ruby-doc.org/core/classes/Enumerable.html). You will be mainly importing static methods and fields from [Enumerable](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/Enumerable.java) and [Lambda](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/Lambda.java)


You probably want to use the *@LambdaParameter* annotation to mark fields of your own types to be used in blocks via static imports:

    public class MyDomainLambdaParameters {
        @LambdaParameter
        public static Money m;
    }

Accessing a field marked with *@LambdaParameter* outside of a block will either start a new block or throw an exception depending on the situation. The fields are never really used, as all accesses are redirected. Due to class loading, you cannot define a *@LambdaParameter* in the same class it's used. You can use inner static classes as an alternative if you want the definitions close to their usage.


## Implementation

Enumerable.java uses [Maven 2](http://maven.apache.org/) to build. You need to run `mvn package -DskipTests` the first time to get the java agent jar required to run the tests, `mvn test`. The class loader will pick up your changes over the ones in the jar, so there's no need to rebuild the jar all the time while developing.

The transformation is implemented in two passes. The first pass identifies all blocks and their arities and which local variables they access, if any. The second pass does the actual transformation, which has three main elements:

* Moving the actual block expression into a new inner class implementing Fn1 or Fn2.
* Wrapping any accessed local variables in arrays which are passed into the block constructor.
* Replacing the original expression with code that constructs the new block.


### How expressions are transformed into blocks

Take this block:

    import static lambda.enumerable.Enumerable.*;
    import static lambda.Lambda.*;

    // ...

    each(strings, fn(s, out.printf("Country: %s\n", s)));

    
The first pass starts by looking for any static fields marked with the *@LambdaParameter* annotation.
Once it sees access to one, *s* in this case, it will start moving the code into a new *Fn1* (or *Fn2*) implementation. A block ends with a call to a static method marked with *@NewLambda*: *fn*. (Remember when reading the code that all arguments are (obviously) evaluated before the method call, so *s* is accessed first, and *fn* called last.)

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


A library that's quite close to what I suggested there is Lamba4j:
http://code.google.com/p/lambdaj/


