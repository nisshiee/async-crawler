package org.nisshiee.crawler.json4s

import scalaz._, Scalaz._
import org.specs2._
import org.specs2.scalaz.ValidationMatchers._

import org.json4s._
import org.json4s.jackson.JsonMethods._

import org.nisshiee.crawler._

class Json4sCrawlerSpec extends Specification { def is =

  "JValueInstance結合テスト"                                                    ^
    "正常系"                                                                    ^
      "json4sのJValueオブジェクトからスクレイピングできる"                      ! e1^
                                                                                p^
    "異常系"                                                                    ^
      "JSONパースエラーは上位レイヤーで補足する"                                ! e2^
                                                                                end

  def e1 = Crawler("http://httpbin.org/get") { jv: JValue =>
    jv \ "url" match {
      case JString(url) => url.successNel
      case _ => ParseError("JSON形式不正").failNel
    }
  }.result() must beSuccessful.like {
    case s: String => s must equalTo("http://httpbin.org/get")
  }

  def e2 = Crawler("http://example.com"){ jv: JValue => jv.successNel }.result() must beLike {
    case Failure(NonEmptyList(ParseError(mes))) => mes must contain("JsonParseException")
  }
}
