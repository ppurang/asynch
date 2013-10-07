package org.purang.net

package http

import scalaz._
import Scalaz._

object `package` {
  //types
  type Url = String
  type Status = Int
  type Headers = Vector[Header]
  type Body = Option[String]

  type RequestModifier = Request => Request

  type FailedRequest =  (Throwable, Request)

  type AResponse = (Status, Headers, Body, Request)

  type or[+E, +A] = Validation[E, A]

  type ExecutedRequest = FailedRequest or AResponse

  type Executor = Request => ExecutedRequest

  type ExecutedRequestHandler[T] = (ExecutedRequest => T)

  /** the following overshadows the scala.Predef.conforms TODO is there a better way?*/
  //implicit val conforms : RequestModifier = (req:Request) => req

  implicit def responseToString(response: AResponse) = response match {
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

  def throwableToFailure[T](req: Request)(block: => T): Validation[(java.lang.Throwable, Request), T] =
    try {
      block.success
    } catch {
      case ex : Throwable => (ex, req).fail
    }

  implicit def responseSuccessFunctionToTuple[T](f: (Status, Headers, Body, Request) => T ) = f.tupled

  implicit def responseFailureFunctionToTuple[T](f: (Throwable, Request) => T ) = f.tupled
}



