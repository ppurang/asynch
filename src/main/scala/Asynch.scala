package org.purang.net

package http

import scalaz._
import Scalaz._

object `package` {
  //types
  implicit class Url(val url: String) extends AnyVal {
    override def toString(): String = url
  }

  object Url {
    implicit def unapply(url: Url): String = url.url
  }

  type Status = Int
  type Headers = Vector[Header]
  type Body = Option[String]

  implicit class Timeout(val timeout: Long) extends AnyVal {
    def +(delta: Long): Long = timeout + delta
  }
  object Timeout {
    implicit def unapply(timeout: Timeout): Long = timeout.timeout
  }


  trait RequestModifier {
    def modify: Request => Request
  }

  type FailedRequest =  (Throwable, Request)

  type AResponse = (Status, Headers, Body, Request)

  type or[+E, +A] = \/[E, A]

  type ExecutedRequest = FailedRequest or AResponse

  type NonBlockingExecutedRequest = scalaz.concurrent.Task[AResponse]

  trait NonBlockingExecutor extends (Timeout => Request => NonBlockingExecutedRequest)

  type ExecutedRequestHandler[T] = (ExecutedRequest => T)


  private lazy val property: String = System.getProperty("asynch.debug")

  private[http] def debug(msg: => String) = {
    if (property != null && property.toBoolean) {
      println("[ASYNCH] " + msg + "[/ASYNCH]")
    }
  }

  def requestModifier(mod: Request => Request) : RequestModifier = new RequestModifier {
    override def modify = mod
  }

  private object NoopRequestModifier extends RequestModifier {
    override def modify: (Request) => Request = req => req
  }

  implicit val noop: RequestModifier = NoopRequestModifier

  implicit def responseToString(response: AResponse): String = response match {
    case (_,_,Some(x),_) =>"""%s%n%n%s""".format(incompleteResponseToString(response), x)
    case _ => incompleteResponseToString(response)
  }

  private def incompleteResponseToString(response: AResponse) : String = """%s%n%s""".format(response._1, response._2.mkString("\n"))

  implicit object StringToVector extends Function[String, Vector[String]] {
    def apply(string: String) = Vector(string)
  }

  implicit object MethodToString extends Function[Method, String] {
    def apply(method: Method) = method.toString
  }

  implicit def scalaIterableToHeaderValues(values: Iterable[String]): HeaderValues = {
    HeaderValues(Vector[String]() ++ values)
  }

  implicit def stringToHeaderValue(value: String): HeaderValues = HeaderValues(value)

  implicit def headerToVector(header: Header): Vector[Header] = Vector[Header](header)

  implicit def headersToString(headers: Vector[Header]): String = headers.mkString("\n")

  implicit def stringToRequest(url: String): Request = Request(url)

  def throwableToFailure[T](req: Request)(block: => T): (java.lang.Throwable, Request) or T =
    try {
      block.right
    } catch {
      case ex : Throwable => (ex, req).left
    }

  implicit def responseSuccessFunctionToTuple[T](f: (Status, Headers, Body, Request) => T ): ((Status, Headers, Body, Request)) => T = f.tupled

  implicit def responseFailureFunctionToTuple[T](f: (Throwable, Request) => T ): ((Throwable, Request)) => T = f.tupled
}



