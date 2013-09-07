package org.nisshiee.crawler

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration

import scalaz._, Scalaz._

sealed abstract class Fv[+T] {

  def self: Future[Vld[T]]

  def result(atMost: Duration = Duration.Inf): Vld[T] = Await.result(self, atMost)
}

final case class FvImpl[+T](self: Future[Vld[T]]) extends Fv[T]

object Fv {

  def apply[T](t: T): Fv[T] = Applicative[Fv].point(t)
}
