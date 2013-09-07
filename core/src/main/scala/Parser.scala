package org.nisshiee.crawler

import scalaz._, Scalaz._

trait Parser[A] {

  val parse: Array[Byte] => ValidationNel[ParseError, A]
}

object Parser {

  def by[A, B](f: A => ValidationNel[ParseError, B])(implicit base: Parser[A]): Parser[B] = new Parser[B] {

    override val parse: Array[Byte] => ValidationNel[ParseError, B] = base.parse(_) flatMap f
  }
}
