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
  implicit val conforms : RequestModifier = (req:Request) => req

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
    HeaderValues(Vector() ++ values)
  }

  implicit def stringToHeaderValue(value: String): HeaderValues = HeaderValues(value)

  implicit def headerToVector(header: Header): Vector[Header] = Vector[Header](header)

  implicit def headersToString(headers: Vector[Header]): String = headers.mkString("\n")

  implicit def stringToRequest(url: String): Request = Request(url)

  @deprecated("no alternative", "0.2.3")
  def throwableToLeft[T](block: => T): Either[java.lang.Throwable, T] =
    try {
      Right(block)
    } catch {
      case ex => Left(ex)
    }

  def throwableToFailure[T](req: Request)(block: => T): Validation[(java.lang.Throwable, Request), T] =
    try {
      block.success
    } catch {
      case ex => (ex, req).fail
    }

  implicit def responseSuccessFunctionToTuple[T](f: (Status, Headers, Body, Request) => T ) = f.tupled

  implicit def responseFailureFunctionToTuple[T](f: (Throwable, Request) => T ) = f.tupled

  @deprecated("no alternative", "0.2.3")
  def responseFailureToString(t: Throwable, req: Request): String = throwableToString(t)

  /**
   * The following has been lifted from sbt/xsbt.
   */
  @deprecated("no alternative", "0.2.3")
  def throwableToString(t: Throwable): String = {
    def isSbtClass(name: String) = name.startsWith("sbt") || name.startsWith("xsbt")

    def trimmed(throwable: Throwable, d: Int = 50): String = {
      require(d >= 0)
      val b = new StringBuilder()
      def appendStackTrace(t: Throwable, first: Boolean) {
        val include: StackTraceElement => Boolean =
          if (d == 0)
            element => !isSbtClass(element.getClassName)
          else {
            var count = d - 1
            (_ => {
              count -= 1; count >= 0
            })
          }
        def appendElement(e: StackTraceElement) {
          b.append("\tat ")
          b.append(e)
          b.append('\n')
        }
        if (!first)
          b.append("Caused by: ")
        b.append(t)
        b.append('\n')
        val els = t.getStackTrace()
        var i = 0
        while ((i < els.size) && include(els(i))) {
          appendElement(els(i))
          i += 1
        }
      }
      appendStackTrace(throwable, true)
      var c = throwable
      while (c.getCause() != null) {
        c = c.getCause()
        appendStackTrace(c, false)
      }
      b.toString()
    }

    trimmed(t);
  }
}



