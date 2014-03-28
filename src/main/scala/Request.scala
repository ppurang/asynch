package org.purang.net

package http

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import scala.collection.mutable.ArrayBuffer

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
    var t = ArrayBuffer[T]()
    ~>>(2000)(executor, adapter).runAsync {
        case -\/(ex) => t = t += f((ex, this).left)
        case \/-(r) => t += f(r.right)
    }
    t(0)
  }

  override def ~>>(timeout: Timeout)(implicit executor: NonBlockingExecutor, adapter: RequestModifier) =  executor(timeout)(adapter(this))


  override def toString = body match {
    case Some(x) =>"""%s%n%n%s""".format(incompleteToString, x)
    case _ => incompleteToString
  }

  private lazy val incompleteToString: String = """%s %s%n%s""".format(method, url, headers.mkString("\n"))

  //the following shouldn't
}

/*
  TODO Would have been nice to do the following

  def ~>>[T](additionalHeaders: Seq[Header])(implicit f: ResponseHandler[T]) = copy(headers = headers ++ additionalHeaders) ~> f

  def ~>>[T](entity: String)(implicit f: ResponseHandler[T]) = copy(body = Some(entity)) ~> f
*/
