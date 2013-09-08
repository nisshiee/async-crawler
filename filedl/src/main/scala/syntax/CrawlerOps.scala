package org.nisshiee.crawler.filedl

import java.io.File

import scalaz._, Scalaz._

import org.nisshiee.crawler._

class CrawlerOps(val self: Crawler) extends AnyVal {

  def file
  (urlStr: String, filePath: String, params: (String, String)*)
  (implicit timeout: Timeout = Timeout(60), retry: Retry = Retry(3), interval: Interval = Interval(1000))
  : Fv[File] = {
    implicit val parser = fileParser(filePath)
    self.apply(urlStr, params: _*)((_: File).successNel)
  }
}

trait ToOptionOps {

  implicit def ToOptionOps(crawler: Crawler) = new CrawlerOps(crawler)
}
