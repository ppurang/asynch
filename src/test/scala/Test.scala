package org.purang.net

package http

object Test {
  def main(args: Array[String]) {
    import ning._
    println((GET > args(0)) ~> {
      _.fold(
        t => "error: " + t._1.getMessage,
        x => x match {
          case (200, _, Some(body), _) => "ok: [" + body + "]"
          case (z, _, _, _) => "unexpected status: " + z
        }
      )
    })
  }
}