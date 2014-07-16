package org.purang.net

package http

import scalaz._
import Scalaz._

sealed trait Request {
  val method: Method
  val url: Url
  val headers: Headers
  val body: Body
  def >> (additionalHeaders: Headers): Request
  def >>> (newBody: String) : Request
  //the following blocks     // todo and should be based on the one below
  def ~>[T](f: ExecutedRequestHandler[T], timeout: Timeout = 2000)(implicit executor: NonBlockingExecutor, adapter : RequestModifier) : T

  //the following shouldn't block
  def ~>>(timeout: Timeout = 2000)(implicit executor: NonBlockingExecutor, adapter : RequestModifier) : NonBlockingExecutedRequest
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

  override def >> (additionalHeaders: Headers) = copy(headers = headers ++ additionalHeaders)

  override def >>> (newBody: String) = copy(body = Option(newBody))

  override def ~>[T](f: ExecutedRequestHandler[T], timeout: Timeout)(implicit executor: NonBlockingExecutor, adapter: RequestModifier) = {
    debug(s"executing blocking call with $timeout. Default is 2000 ms.")
    val task = ~>>(timeout)(executor, adapter).timed(timeout + 100) // we pass timeout along and enforce it on our own too by giving it about 100 ms!
    task.attemptRun.fold (
        t => f((t, this).left),
        r => f(r.right)
    )
  }

  override def ~>>(timeout: Timeout)(implicit executor: NonBlockingExecutor, adapter: RequestModifier) =  executor(timeout)(adapter(this))


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
