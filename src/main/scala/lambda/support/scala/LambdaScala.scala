package lambda.support.scala

import lambda.{Fn0, Fn1, Fn2, Fn3}

abstract class FunctionFn0[R] extends Function0[R] {
	def apply():R
}

abstract class FunctionFn1[A1, R] extends Function1[A1, R] {
	def apply(a1: A1):R
}

abstract class FunctionFn2[A1, A2, R] extends Function2[A1, A2, R] {
	def apply(a1: A1, a2: A2):R
}

abstract class FunctionFn3[A1, A2, A3, R] extends Function3[A1, A2, A3, R] {
	def apply(a1: A1, a2: A2, a3: A3):R
}

object LambdaScalaFactory {
	def toFunction[R](fn: Fn0[R]) = {
		() => { fn.call() }
	}

	def toFunction[A1, R](fn: Fn1[A1, R]) = {
		(a1: A1) => { fn.call(a1) }
	}

	def toFunction[A1, A2, R](fn: Fn2[A1, A2, R]) = {
		(a1: A1, a2: A2) => { fn.call(a1, a2) }
	}

	def toFunction[A1, A2, A3, R](fn: Fn3[A1, A2, A3, R]) = {
		(a1: A1, a2: A2, a3: A3) => { fn.call(a1, a2, a3) }
	}
}
