package org.purang.net.http

import cats.data.NonEmptyChain
import cats.Show
import cats.syntax.show._
import cats.syntax.foldable._

trait Header {
  val name: String
  val values: NonEmptyChain[String]

  final val value: String = values.head

  override def toString: String = name + ": " + values.mkString_(",")
}

object Header {
  implicit val show: Show[Header] = Show.fromToString

  def apply(name: String, values: NonEmptyChain[String]) : Header = HeaderImpl(name, values)
}

final case class HeaderImpl(name: String, values: NonEmptyChain[String]) extends Header

case class HeaderValues(values: NonEmptyChain[String]) {
  def `:`(name: String): HeaderImpl = {
    HeaderImpl(name, values)
  }
}

object ContentType extends Function[HeaderValues, Header] {
  def apply(headerValues: HeaderValues): Header = "CONTENT-TYPE" `:` headerValues
}

object Accept extends Function[HeaderValues, Header] {
  def apply(headerValues: HeaderValues): Header = "ACCEPT" `:` headerValues
}

object TextPlain extends HeaderValues(NonEmptyChain("text/plain"))
object TextHtml extends HeaderValues(NonEmptyChain("text/html"))
object ApplicationJson extends HeaderValues(NonEmptyChain("application/json"))
object MultipartMixed extends HeaderValues(NonEmptyChain("multipart/mixed"))
