package org.nisshiee.crawler

import java.nio.charset.Charset

import scalaz._, Scalaz._

trait StringInstances {

  implicit def stringInstance(implicit charset: Charset = Charset.forName("UTF-8")) = new Parser[String] {

    override val parse: Array[Byte] => ValidationNel[ParseError, String] = new String(_, charset).successNel
  }
}
