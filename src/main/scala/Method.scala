package org.purang.net

package http


sealed trait Method {
  def >(url: String): Request = Request(this, url)

  override def toString: String
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

case object POST extends Method {
  override def toString: String = "POST"
}

case object PATCH extends Method {
  override def toString: String = "PATCH"
}

