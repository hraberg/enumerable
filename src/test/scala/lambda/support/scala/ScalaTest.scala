package lambda.support.scala

import lambda.Lambda._
import lambda.Parameters._

object EnumerableJavaScalaTest {
	def toUpperCase() = {
		λ(s, s.toUpperCase)
	}
}
