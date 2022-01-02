package org.purang.net.http

import cats.Show 

/**
 * The following makes it impossible to define your own HTTP Methods which might be a good thing
 */
sealed trait Method {
  def >(url: String): HttpRequest = HttpRequest.apply(this, Url(url))

  override def toString: String
}

object Method {
  implicit val show: Show[Method] = Show.fromToString
}
//idempotent
case object GET extends Method {
  override def toString: String = "GET"
}
case object HEAD extends Method {
  override def toString: String = "HEAD"
}
case object PUT extends Method {
  override def toString: String = "PUT"
}
case object DELETE extends Method {
  override def toString: String = "DELETE"
}
//inherently idempotent
case object OPTIONS extends Method {
  override def toString: String = "OPTIONS"
}
case object TRACE extends Method {
  override def toString: String = "TRACE"
}
//not idempotent
case object POST extends Method {
  override def toString: String = "POST"
}
//idempotent? no but..
case object PATCH extends Method {
  override def toString: String = "PATCH"
}