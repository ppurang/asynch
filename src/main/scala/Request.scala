package org.purang.net

package http

import scalaz.concurrent.Promise

sealed trait Request {
  val method: Method
  val url: Url
  val headers: Headers
  val body: Body
  def >> (additionalHeaders: Headers): Request
  def >>> (newBody: String) : Request
  def ~>[T](f: ExecutedRequestHandler[T])(implicit executor: Executor, adapter : RequestModifier) : T
  def ~>>[T](f: ExecutedRequestHandler[T])(implicit executor: Executor, adapter : RequestModifier) : Promise[T] = Promise(
   this.~>[T](f)(executor, adapter)
  )
}

object Request {
  implicit def apply(tuple: (Method, Url,  Headers, Body)) : Request = {
    RequestImpl(tuple._1, tuple._2, tuple._3, tuple._4)
  }

  implicit def apply(tuple: (Method, Url,  Headers)) : Request = {
    RequestImpl(tuple._1, tuple._2, tuple._3)
  }

  implicit def apply(tuple: (Method, Url)): Request = {
    RequestImpl(tuple._1, tuple._2)
  }

  implicit def apply(aurl: Url) : Request = {
    RequestImpl(url = aurl)
  }
}

case class RequestImpl(method: Method = GET, url: Url, headers: Headers = Vector(), body: Body = None) extends Request {

  def >> (additionalHeaders: Headers) = copy(headers = headers ++ additionalHeaders)

  def >>> (newBody: String) = copy(body = Option(newBody))

  def ~>[T](f: ExecutedRequestHandler[T])(implicit executor: Executor, adapter: RequestModifier) = f(executor(adapter(this)))


  override def toString = body match {
    case Some(x) =>"""%s%n%n%s""".format(incompleteToString, x)
    case _ => incompleteToString
  }

  private lazy val incompleteToString: String = """%s %s%n%s""".format(method, url, headers.mkString("\n"))

}

/*
  TODO Would have been nice to do the following

  def ~>>[T](additionalHeaders: Seq[Header])(implicit f: ResponseHandler[T]) = copy(headers = headers ++ additionalHeaders) ~> f

  def ~>>[T](entity: String)(implicit f: ResponseHandler[T]) = copy(body = Some(entity)) ~> f
*/
