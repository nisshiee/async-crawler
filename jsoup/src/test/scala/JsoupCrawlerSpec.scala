package org.nisshiee.crawler.jsoup

import scalaz._, Scalaz._
import org.specs2._
import org.specs2.scalaz.ValidationMatchers._

import org.jsoup.nodes.Document

import org.nisshiee.crawler._

class JsoupCrawlerSpec extends Specification { def is =

  "DocumentInstance結合テスト"                                                  ^
    "正常系"                                                                    ^
      "JsoupのDocumentオブジェクトからスクレイピングできる"                     ! e1^
                                                                                p^
    "異常系"                                                                    ^
      "JsoupのDocumentオブジェクトのスクレイピング例外は上位レイヤーで補足する" ! e2^
                                                                                end

  def e1 = Crawler("http://example.com") { d: Document =>
    d.select("h1").first.text successNel
  }.result() must beSuccessful.like {
    case s: String => s must equalTo("Example Domain")
  }

  def e2 = Crawler("http://example.com") { d: Document =>
    d.select("h6").first.text successNel
  }.result() must beLike {
    case Failure(NonEmptyList(ParseError(mes))) => mes must contain("NullPointerException")
  }
}
