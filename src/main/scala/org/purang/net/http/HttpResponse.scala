package org.purang.net

package http

import cats.Show
import cats.syntax.show._

final case class HttpStatus(s: Int) extends AnyVal

object HttpStatus {
  implicit val show: Show[HttpStatus] = Show.show {
    _.s.toString
  }
}

final case class HttpResponse(status: HttpStatus, responseHeaders: Option[Headers], body: Option[Body])

object HttpResponse {

  implicit val show : Show[HttpResponse] = show("\n")
  
  def show(separator: String): Show[HttpResponse] = Show.show {
    res => {
      val firstLine = s"""${res.status.show}$separator"""
      val headers = res.responseHeaders.fold("")(Headers.show(separator).show(_))
      val resMsgTillHeaders = s"""$firstLine$headers"""

      if res.body.isDefined && res.responseHeaders.isDefined then
        s"""$resMsgTillHeaders$separator${res.body.fold("")(_.show)}"""
      else if res.body.isDefined then
        s"""$resMsgTillHeaders${res.body.fold("")(_.show)}"""
      else s"""$resMsgTillHeaders$separator"""
    }
  }

}
