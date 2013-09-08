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

sealed trait Crawler {

  def apply[A, T]
  (urlStr: String, params: (String, String)*)
  (parse: A => ValidationNel[ParseError, T])
  (implicit parser: Parser[A], timeout: Timeout = Timeout(60), retry: Retry = Retry(3), interval: Interval = Interval(1000))
  : Fv[T]
}

object Crawler extends Crawler {

  override def apply[A, T]
  (urlStr: String, params: (String, String)*)
  (parse: A => ValidationNel[ParseError, T])
  (implicit parser: Parser[A], timeout: Timeout = Timeout(60), retry: Retry = Retry(3), interval: Interval = Interval(1000))
  : Fv[T] = sendRequest(Get)(urlStr, params: _*)(parse)

  def post[A, T]
  (urlStr: String, params: (String, String)*)
  (parse: A => ValidationNel[ParseError, T])
  (implicit parser: Parser[A], timeout: Timeout = Timeout(60), retry: Retry = Retry(3), interval: Interval = Interval(1000))
  : Fv[T] = sendRequest(Post)(urlStr, params: _*)(parse)

  def sendRequest[A, T]
  (method: Method)
  (urlStr: String, params: (String, String)*)
  (parse: A => ValidationNel[ParseError, T])
  (implicit parser: Parser[A], timeout: Timeout = Timeout(60), retry: Retry = Retry(3), interval: Interval = Interval(1000))
  : Fv[T] = {
    implicit val t = ATimeout(30 days)
    (actor ? Req(urlStr, params, method, timeout, retry, interval) map {
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

  case class Req(
     urlStr: String
    ,params: Traversable[(String, String)]
    ,method: Method
    ,timeout: Timeout
    ,retry: Retry
    ,interval: Interval
  )

  sealed trait Method
  case object Get extends Method
  case object Post extends Method

  class CrawlActor extends Actor {

    def receive = {
      case Req(urlStr, _, _, _, Retry(r), _) if r <= 0 => sender ! ConnectionError(urlStr).failure
      case Req(urlStr, params, method, Timeout(t), Retry(r), Interval(i)) => {
        val resultOpt = allCatch opt {
          val svc = method match {
            case Get => url(urlStr) <<? params
            case Post => url(urlStr) << params
          }
          val future = http(svc OK as.Bytes)
          Await.result(future, t seconds)
        }
        resultOpt match {
          case Some(bodyBytes) => sender ! bodyBytes.success
          case None => self.tell(Req(urlStr, params, method, Timeout(t), Retry(r - 1), Interval(i)), sender)
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
