package org.purang.net

package http

object `package` {
  //types
  type Url = String
  //todo would be nice to see a method like \n to concatenate headers together
  type Headers = Vector[Header]
  type Body = Option[String]

  implicit object StringToVector extends Function[String, Vector[String]] {
    def apply(string: String) = Vector(string)
  }

  implicit object MethodToString extends Function[Method, String] {
    def apply(method: Method) = method.toString
  }

  //type HeaderPartialFunction = String => Function[String, Header]
  //implicit def stringToHeaderValue(value: String): HeaderValue = HeaderValue(value)

  implicit def scalaIterableToHeaderValues(values: Iterable[String]): HeaderValues = {
    HeaderValues(Vector() ++ values)
  }

  implicit def stringToHeaderValue(value: String): HeaderValues = HeaderValues(value)

  implicit def headerToVector(header: Header): Vector[Header] = Vector[Header](header)

  //implicit def headerToHeaders(header: Header): Headers = Headers(Vector(header))

  //implicit def headersToHeaderList(headers: Headers): Vector[Header] = headers.headers

  implicit def headersToString(headers: Vector[Header]): String = headers.mkString("\n")

  implicit def stringToRequest(url: String): Request = Request(url)

  type Response = Either[Throwable, Tuple3[Int, Vector[Header], Option[String]]]
  type ResponseHandler[T] = Response => T


  def throwableToLeft[T](block: => T): Either[java.lang.Throwable, T] =
    try {
      Right(block)
    } catch {
      case ex => Left(ex)
    }
}



