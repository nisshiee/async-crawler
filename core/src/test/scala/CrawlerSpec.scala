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
      "GetParameter関連"                                                        ^
        "Parameterなしで呼び出せる"                                             ! e6^
        "可変長引数でGetParameterを渡せる(パラメータ1つ)"                       ! e7^
        "可変長引数でGetParameterを渡せる(パラメータ2つ)"                       ! e8^
        "Seq[(String, String)]を可変長引数展開で渡せる"                         ! e9^
                                                                                p^
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

  def e6 = Crawler("http://httpbin.org/get"){ s: String => s.successNel }.result() must beSuccessful.like {
    case body: String => body must contain(""""args": {}""")
  }

  def e7 = Crawler("http://httpbin.org/get", "arg1" -> "value1"){ s: String => s.successNel }.result() must beSuccessful.like {
    case body: String => body must contain(""""arg1": "value1"""")
  }

  def e8 =
    Crawler("http://httpbin.org/get", "arg1" -> "value1", "arg2" -> "value2"){ s: String => s.successNel }.result() must beSuccessful.like {
      case body: String => (body must contain(""""arg1": "value1"""")) and (body must contain(""""arg2": "value2""""))
    }

  def e9 = {
    val params = Map("arg1" -> "value1", "arg2" -> "value2").toSeq
    Crawler("http://httpbin.org/get", params: _*){ s: String => s.successNel }.result() must beSuccessful.like {
      case body: String => (body must contain(""""arg1": "value1"""")) and (body must contain(""""arg2": "value2""""))
    }
  }
}

