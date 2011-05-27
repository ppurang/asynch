package org.purang.net

package http

trait Header {
  require(name != null && values != null)
  val name: String
  final val value: String = values.head
  val values: Vector[String]

  override def toString = name + ": " + values.mkString(", ")
}

case class HeaderImpl(name: String, values: Vector[String]) extends Header

case class HeaderValues(values: Vector[String])  {
  def `:`(name: String) = {
    HeaderImpl(name, values)
  }
}

object ContentType extends Function[HeaderValues, Header] {
  def apply(headerValues: HeaderValues) : Header =  "Content-Type" `:` headerValues
}

object Accept extends Function[HeaderValues, Header] {
  def apply(headerValues: HeaderValues) : Header =  "Accept" `:` headerValues
}

object ApplicationJson extends HeaderValues("application/json")
object MultipartMixed extends HeaderValues("multipart/mixed")
