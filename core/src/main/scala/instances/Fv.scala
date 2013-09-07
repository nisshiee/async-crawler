package org.nisshiee.crawler

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._, Scalaz._
import scalaz.contrib.std._

trait FvInstances {

  implicit object fvInstance extends Monad[Fv] {
 
    override def bind[A, B](fa: Fv[A])(f: (A) => Fv[B]): Fv[B] = {
      val Fva: Future[Vld[A]] = fa.self
      val Fvb: Future[Vld[B]] = Fva.flatMap { va =>
        val vfvb: Vld[Fv[B]] = va.map(f)
        val vFvb: Vld[Future[Vld[B]]] = vfvb.map(_.self)
        val Fvvb: Future[Vld[Vld[B]]] = vFvb.sequence
        Fvvb.map(_.join)
      }
      FvImpl(Fvb)
    }
 
    override def point[A](a: => A): Fv[A] =
      FvImpl(Applicative[Future].point(Applicative[Vld].point(a)))
  }
}
