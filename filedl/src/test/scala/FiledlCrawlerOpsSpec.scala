package org.nisshiee.crawler.filedl

import java.io.File

import scalaz._, Scalaz._
import org.specs2._
import org.specs2.scalaz.ValidationMatchers._
import scalax.file._

import org.nisshiee.crawler._

class FiledlCrawlerOpsSpec extends Specification { def is =

  "filedl.CrawlerOpsの結合テスト"                                               ^
    "正常系"                                                                    ^
      "Crawler.fileでファイルがダウンロードされる"                              ! e1^
      "存在しないディレクトリ下にダウンロードするよう指定した場合親ディレクトリも作成される" ! e2^
                                                                                p^
    "異常系"                                                                    ^
      "接続できないURLだった場合はファイルが作成されない"                       ! e3^
                                                                                end

  val dir = "filedl/target/testworkspace"

  def cleaning[T](t: => T): T = synchronized {
    val p = Path.fromString(dir)
    p.deleteRecursively()
    p.createDirectory()
    t
  }

  def e1 = cleaning {
    Crawler.file("http://example.com", s"$dir/test1.html").result() must beSuccessful.like {
      case f: File => (f.isFile must beTrue) and (f.length must be_>(0L))
    }
  }

  def e2 = cleaning {
    Crawler.file("http://example.com", s"$dir/test2/test2.html").result() must beSuccessful.like {
      case f: File => (f.isFile must beTrue) and (f.length must be_>(0L))
    }
  }

  def e3 = cleaning {
    Crawler.file("http:/awsvoawaowe4ngaowedsber.com", s"$dir/test3.html").result() must beLike {
      case Failure(NonEmptyList(ConnectionError(url))) =>
        (url must equalTo("http:/awsvoawaowe4ngaowedsber.com")) and
        (new File(s"$dir/test3.html").exists must beFalse)
    }
  }
}
