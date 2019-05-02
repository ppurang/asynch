package org.purang.net
package http
import scalaz._
import concurrent.Task

case class TestExecutor(expected : Map[Request, ExecutedRequest]) extends NonBlockingExecutor {
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

case class MisbehavingExecutor(time: Long)  extends NonBlockingExecutor {
  assert(time > 1000, s"well you are an optimist! you provided $time up it a few seconds")
  def apply(t: Timeout) = _ => Task({
      Thread.sleep(time)
      throw new Exception(s"Seriously! You waited $time ms for a response.")
  })
}
