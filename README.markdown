# Enumerable.java

Copyright 2010 Håkan Råberg


Ruby/Smalltalk style internal iterators for Java 5 using bytecode transformation to capture expressions as closures.


## Introduction:

Enumarable allows you to write blocks in valid Java like this:

    Fn1 square = fn(n, n * 2);
    List<Integer> result = select(integers, square));

  
Which is expanded using the [ASM Toolkit for Bytecode Manipulation](http://asm.ow2.org/) into this:

    Fn1 square = new Fn1() {
        public Object call(Object arg) {
            return ((Integer) arg) * 2;
        }
    }));
    List<Integer> result = select(integers, square));


Closure works as expected, by trasforming local variables to arrays:

    int i = 0;
    fn(n, i += n).call(10);
    assert i == 10;

    
Becomes:

    int[] i = new int[] { 0 };
    Fn1 square = new Fn1(i) {
        public Object call(Object arg) {
            return i[0] += (Integer) arg;
        }
    })).call(10);
    assert i[0] == 10;


Block parameters are defined using annotated static fields.
The actual blocks are limited to one expression. For more examples, see: [EnumerableExampleAndRegressionTest](http://github.com/hraberg/enumerable/blob/master/src/test/java/lambda/enumerable/EnumerableExampleAndRegressionTest.java)


## Usage:

Enumerable.java is packaged as a [java agent](http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html). ASM has been moved to a local package (lambda.asm).

    java -javaagent:enumerable-jarjar.jar [...]

You can also launch your application like this:

    java -jar enumerable.jar my.company.MyClass [args...]

Look at [LamdaLoader](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/weaving/LambdaLoader.java) if you have different class loading needs.

The API of lambda.enumerable.Enumerable is very similar to the [Enumerabe module in Ruby](http://ruby-doc.org/core/classes/Enumerable.html). You will be mainly importing static methods and fields from [Enumerable](http://github.com/hraberg/enumerable/blob/master/src/main/java/lambda/enumerable/Enumerable.java)


You probably want to use the *@LambdaParameter* annotation to mark fields of your own types to be used in blocks via static imports:

    public class MyDomainLambdaParameters {
        @LambdaParameter
        public static Money m;
    }

Accessing a field marked with *@LambdaParameter* outside of a block will either start a new block or throw an exception depending on the situation. The fields are never really used, as all accesses is be redirected.


## Implementation

Enumerable.java uses [Maven 2](http://maven.apache.org/) to build. You need to run mvn package -DskipTests the first time to get the java agent jar required to run the tests. Then run mvn test. The class loader will pick up your changes over the ones in the jar, so there's no need to rebuild the jar all the time.

The transformation is implemented in two passes. The first pass identifies all blocks and their arities and which local variables they access, if any. The second pass does the actual transformation, which has three main elements:

* Moving the actual block expression into a new inner class implementing Fn1 or Fn2.
* Wrapping any accessed local variables in arrays which are passed into the block constructor.
* Replacing the original expression with code that constructs the new block.


# How expressions are transformed into blocks

Take this block:

    import static lambda.enumerable.Enumerable.*;

    // ...

    each(strings, fn(s, out.printf("Country: %s\n", s)));

    
The first pass starts by looking for any static fields marked with the *@LambdaParameter* annotation.
Once it sees access to one, *s* in this case, it will start moving the code into a new *Fn1* (or *Fn2*) implementation. A block ends with a call to a static method marked with *@NewLambda*: *fn*.

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


Released under the [EPL license](http://www.eclipse.org/legal/epl-v10.html).