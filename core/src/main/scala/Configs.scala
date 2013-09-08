package org.nisshiee.crawler

case class Retry(count: Int)
case class Interval(millisec: Long)
case class Timeout(sec: Int)
