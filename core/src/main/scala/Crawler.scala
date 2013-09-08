package org.nisshiee.crawler

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.Exception.allCatch

import java.nio.charset.Charset

import akka.actor._
import akka.util.{ Timeout => ATimeout }
import akka.pattern.ask
import dispatch._
import scalaz._, Scalaz._

sealed trait Crawler

object Crawler extends Crawler {

  def apply[A, T]
  (urlStr: String, params: (String, String)*)
  (parse: A => ValidationNel[ParseError, T])
  (implicit parser: Parser[A], timeout: Timeout = Timeout(60), retry: Retry = Retry(3), interval: Interval = Interval(1000))
  : Fv[T] = {
    implicit val t = ATimeout(30 days)
    (actor ? Get(urlStr, params, timeout, retry, interval) map {
      case Success(bodyBytes: Array[Byte]) =>
        parser.parse(bodyBytes) flatMap { a =>
          try parse(a) catch { case e: Throwable => ParseError(s"$urlStr: ${e.toString}").failNel }
        }
      case Failure(e: CrawlError) => e.failNel
    }) |> FvImpl.apply
  }

  lazy val system = ActorSystem("crawler")
  lazy val actor = system.actorOf(Props[CrawlActor], "http")
  lazy val http = Http.configure {
    _
    .setFollowRedirects(true)
    .setRequestTimeoutInMs(60 * 1000)
  }

  case class Get(urlStr: String, params: Traversable[(String, String)], timeout: Timeout, retry: Retry, interval: Interval)

  class CrawlActor extends Actor {

    def receive = {
      case Get(urlStr, _, _, Retry(r), _) if r <= 0 => sender ! ConnectionError(urlStr).failure
      case Get(urlStr, params, Timeout(t), Retry(r), Interval(i)) => {
        val resultOpt = allCatch opt {
          val svc = url(urlStr) <<? params
          val future = http(svc OK as.Bytes)
          Await.result(future, t seconds)
        }
        resultOpt match {
          case Some(bodyBytes) => sender ! bodyBytes.success
          case None => self.tell(Get(urlStr, params, Timeout(t), Retry(r - 1), Interval(i)), sender)
        }
        if (i > 0)
          Thread.sleep(i)
      }
    }
  }

  def shutdown: Unit = {
    system.shutdown
    http.shutdown
  }
}
