package org.purang.net.http

trait RequestModifier {
  def modify: Request => Request
}
