package org.purang.net
package http
import scalaz._, Scalaz._
import concurrent.Task

case class TestExecutor(expected : Map[Request, ExecutedRequest]) extends (Timeout => Request => NonBlockingExecutedRequest) {
  def apply(t: Timeout) = req => Task({
    expected.get(req) match {
      case Some(x) => x fold (
          tr => {debug(s"$req => throwing exception");throw  tr._1},
          y => y
        )
      case _ => throw new Exception("not found")
    }
  })
}
