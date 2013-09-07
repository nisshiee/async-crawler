package org.nisshiee.crawler

import scalaz._, Scalaz._

import org.specs2._
import org.specs2.scalaz.ValidationMatchers._

class CrawlerSpec extends Specification { def is =

  "Crawler結合テスト"                                                           ^
    "正常系"                                                                    ^
      "1リクエスト送信して結果を取得できる"                                     ! e1^
      "2リクエストを送信してfor式で結果を非同期取得できる"                      ! e2^
      "UTF-8日本語のページをデフォルトで取得できる"                             ! e3^
      "Shift_JIS日本語のページをCharset指定つきで取得できる"                    ! e4^
                                                                                p^
    "異常系"                                                                    ^
      "接続できないURLにリクエスト送信したらConnectionErrorが返る"              ! e5^
                                                                                end

  def e1 = Crawler("http://example.com"){ s: String => s.successNel }.result() must beSuccessful.like {
    case body: String => body must contain("<h1>Example Domain</h1>")
  }

  def e2 = {
    val fv = for {
      r1 <- Crawler("http://example.com"){ s: String => s.successNel }
      r2 <- Crawler("http://example.com"){ s: String => s.successNel }
    } yield r1 -> r2
    fv.result() must beSuccessful.like {
      case (r1, r2) => (r1 must contain("<h1>Example Domain</h1>")) and (r2 must contain("<h1>Example Domain</h1>"))
    }
  }

  def e3 = Crawler("http://nisshiee.github.io/async-crawler/testcase/utf8.html"){ s: String => s.successNel }.result() must beSuccessful.like {
    case body: String => body must contain("UTF-8日本語")
  }

  def e4 = {
    implicit val charset = java.nio.charset.Charset.forName("SJIS")
    Crawler("http://nisshiee.github.io/async-crawler/testcase/sjis.html"){ s: String => s.successNel }.result() must beSuccessful.like {
      case body: String => body must contain("Shift_JIS日本語")
    }
  }

  def e5 = Crawler("http://gqworvqpqwviasdjqhwrvunhqiwuaf.com"){ s: String => s.successNel }.result() must beLike {
    case Failure(NonEmptyList(ConnectionError(url))) => url must equalTo("http://gqworvqpqwviasdjqhwrvunhqiwuaf.com")
    case _ => ko
  }
}

