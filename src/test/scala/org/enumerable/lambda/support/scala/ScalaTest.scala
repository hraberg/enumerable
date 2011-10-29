package org.enumerable.lambda.support.scala

import org.enumerable.lambda.Lambda._
import org.enumerable.lambda.Parameters._

object EnumerableJavaScalaTest {
	def toUpperCase() = {
		λ(s, s.toUpperCase)
	}
}
