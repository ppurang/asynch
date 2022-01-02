package org.purang.net.http

import cats.data._
import cats.Show
import cats.syntax.show._
import cats.syntax.foldable._

sealed trait HttpRequest {
  val method: Method
  val url: Url
  val headers: Option[Headers]
  val body: Option[Body]

  def >>(additionalHeaders: Headers): HttpRequest

  def >>>(newBody: String): HttpRequest
}

object HttpRequest {
  implicit def apply(tuple: (Method, Url, Option[Headers], Option[Body])): HttpRequest = {
    RequestImpl(tuple._1, tuple._2, tuple._3, tuple._4)
  }

  implicit def apply(tuple: (Method, Url, Option[Headers])): HttpRequest = {
    RequestImpl(tuple._1, tuple._2, tuple._3)
  }

  implicit def apply: (Method, Url) => HttpRequest = {
    RequestImpl(_, _)
  }

  implicit def apply(aurl: Url): HttpRequest = {
    RequestImpl(url = aurl)
  }

  implicit val show: Show[HttpRequest] = show(Constants.CRLF)

  def show(seperator: String): Show[HttpRequest] = Show.show { req =>
    {
      val firstLine         = s"""${req.method.show} ${req.url.show}$seperator"""
      val headers           = req.headers.fold("")(Headers.show(seperator).show(_))
      val reqMsgTillHeaders = s"""$firstLine$headers"""
      if (req.body.isDefined && req.headers.isDefined) {
        s"""$reqMsgTillHeaders$seperator${req.body.fold("")(_.show)}"""
      } else if (req.body.isDefined) {
        s"""$reqMsgTillHeaders${req.body.fold("")(_.show)}"""
      } else {
        s"""$reqMsgTillHeaders$seperator"""
      }
    }
  }
}

case class RequestImpl(method: Method = GET, url: Url, headers: Option[Headers] = None, body: Option[Body] = None)
    extends HttpRequest {

  override def >>(additionalHeaders: Headers): RequestImpl = copy(
    headers = headers.map(x => Headers(x.hs ++ additionalHeaders.hs)).orElse(Option(additionalHeaders))
  )

  override def >>>(newBody: String): RequestImpl = copy(body = Option(Body(newBody)))

  override def toString: String = (this: HttpRequest).show

}
