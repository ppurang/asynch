package org.purang.net

package http

//TODO using both lists and vectors is sign of a problem decide on one

trait Header {
  require(name != null && values != null)
  val name: String
  final val value: String = values.head
  val values: List[String]

  override def toString = name + ": " + values.mkString(", ")
}

private[http] case class HeaderImp(name: String, values: List[String]) extends Header

case class HeaderValue(value: String) {
  def `:`(name: String) : Header = HeaderImp(name, value)
}

case class HeaderValues(values: List[String]) {
  //def :(values: String*) : Header = HeaderImp(name, values.toList)
  def `:`(name: String) : Header = HeaderImp(name, values)
}


case class Headers(headers: Vector[Header]) {
  def +(header: Header) = Headers(headers ++ header)


  override def toString = headers.mkString("\n")

}
