package org.purang.net

package http

object `package` {

  implicit object StringToList extends Function[String, List[String]] {
    def apply(string: String) = List(string)
  }

  implicit object MethodToString extends Function[Method, String] {
    def apply(method: Method) = method.toString
  }

  //type HeaderPartialFunction = String => Function[String, Header]
  implicit def stringToHeaderValue(value: String): HeaderValue = HeaderValue(value)

  implicit def listStringToHeaderValues(values: List[String]): HeaderValues = HeaderValues(values)

  implicit def headerToHeaderList(header: Header): List[Header] = header :: Nil

  implicit def headerToHeaders(header: Header): Headers = Headers(Vector(header))

  implicit def headersToHeaderList(headers: Headers): Vector[Header] = headers.headers

  implicit def headersToString(headers: Headers): String = headers.toString

  implicit def headersToString(headers: Vector[Header]): String = headers.mkString("\n")

  type Response = Either[Throwable, Tuple3[Int, Vector[Header], Option[String]]]
  type ResponseHandler = Response => Any


  def throwableToLeft[T](block: => T): Either[java.lang.Throwable, T] =
    try {
      Right(block)
    } catch {
      case ex => Left(ex)
    }
}
