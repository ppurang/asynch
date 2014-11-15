package org.purang.net

package http

import java.util.concurrent.ScheduledExecutorService

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
  def ~>[T](f: ExecutedRequestHandler[T], timeout: Timeout = 2000, modifier : RequestModifier = noop)(implicit executor: NonBlockingExecutor,  schExecutor: ScheduledExecutorService) : T

  //the following shouldn't block
  def ~>>(timeout: Timeout = 2000, adapter : RequestModifier = noop)(implicit executor: NonBlockingExecutor) : NonBlockingExecutedRequest
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

  override def ~>[T](f: ExecutedRequestHandler[T], timeout: Timeout, adapter: RequestModifier)(implicit executor: NonBlockingExecutor, schExecutor: ScheduledExecutorService) = {
    debug(s"executing blocking call with $timeout. Default is 2000 ms.")
    val task = ~>>(timeout, adapter)(executor).timed(timeout + 100) // we pass timeout along and enforce it on our own too by giving it about 100 ms!
    task.attemptRun.fold (
        t => f((t, this).left),
        r => f(r.right)
    )
  }

  override def ~>>(timeout: Timeout, adapter: RequestModifier)(implicit executor: NonBlockingExecutor) =  executor(timeout)(adapter.modify(this))


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
