package org.nisshiee.crawler

sealed trait CrawlError
case class ParseError(message: String) extends CrawlError
case class ConnectionError(url: String) extends CrawlError
