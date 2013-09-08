package org.nisshiee.crawler.filedl

import scala.util.control.Exception.allCatch
import java.io.File

import scalax.io.JavaConverters._
import scalaz._, Scalaz._

import org.nisshiee.crawler._

trait FileInstances {

  def fileParser(filePath: String) = new Parser[File] {

    override val parse: Array[Byte] => ValidationNel[ParseError, File] = { bytes =>
      val file = new File(filePath)
      val v = allCatch.either {
        file.asOutput.write(bytes)
        file
      }.disjunction.validation
      v.swapped { e =>
        e.map { _.toString |> ParseError.apply }
      } toValidationNel
    }
  }
}
