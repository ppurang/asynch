package org.purang.net

package http

trait Header {
  require(name != null && values != null)
  val name: String
  val values: Vector[String]

  final val value: String = values.head

  override def toString: String = name + ": " + values.mkString(", ")
}

case class HeaderImpl(name: String, values: Vector[String]) extends Header

case class HeaderValues(values: Vector[String])  {
  def `:`(name: String): HeaderImpl = {
    HeaderImpl(name, values)
  }
}

object ContentType extends Function[HeaderValues, Header] {
  def apply(headerValues: HeaderValues) : Header =  "Content-Type" `:` headerValues
}

object Accept extends Function[HeaderValues, Header] {
  def apply(headerValues: HeaderValues) : Header =  "Accept" `:` headerValues
}

object TextPlain extends HeaderValues("text/plain")
object TextHtml extends HeaderValues("text/html")
object ApplicationJson extends HeaderValues("application/json")
object MultipartMixed extends HeaderValues("multipart/mixed")
