package org.nisshiee.crawler.jsoup

import scala.util.control.Exception.allCatch
import java.nio.charset.Charset

import org.jsoup._, nodes.Document
import scalaz._, Scalaz._

import org.nisshiee.crawler._

trait DocumentInstances {

  implicit def documentInstance(implicit charset: Charset = Charset.forName("UTF-8")) = Parser.by[String, Document] { body =>
    val v = allCatch.either {
      Jsoup.parse(body)
    }.disjunction.validation
    v swapped { _.map(_.toString) map ParseError.apply } toValidationNel
  }
}
