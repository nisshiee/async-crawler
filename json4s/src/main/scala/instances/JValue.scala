package org.nisshiee.crawler.json4s

import scala.util.control.Exception.allCatch
import java.nio.charset.Charset

import org.json4s._
import org.json4s.jackson.JsonMethods._
import scalaz._, Scalaz._

import org.nisshiee.crawler._

trait JValueInstances {

  implicit def jvalueInstance(implicit charset: Charset = Charset.forName("UTF-8")) = Parser.by[String, JValue] { body =>
    val v = allCatch.either {
      parse(body)
    }.disjunction.validation
    v swapped { _.map(_.toString) map ParseError.apply } toValidationNel
  }
}
