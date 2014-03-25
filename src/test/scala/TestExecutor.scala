package org.purang.net
package http
import scalaz._, Scalaz._

case class TestExecutor(expected : Map[Request, ExecutedRequest]) extends (Request => ExecutedRequest) {
  def apply(req: Request) =  expected.get(req) match {
    case Some(x) => x
    case _ => (new Exception("not found"), req).left
  }
}
