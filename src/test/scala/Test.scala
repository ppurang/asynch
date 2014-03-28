package org.purang.net

package http

import com.ning.http.client.AsyncHttpClient

object Test {
  import ning._
  def main(args: Array[String]) {
    System.setProperty("asynch.debug", "true")
    //blocking(args(0))
    nonblocking(args(0))
    nonblockingexecutor.client.close()
  }

  def nonblocking(url: String) =     println(" ------------  " + (GET > url).~>>(0).attemptRun)
  def nonblocking2(url: String) =     println(" ------------  " + (GET > url).~>>(0).timed(1000))

  def blocking(url: String) =     println((GET > url) ~> {
        _.fold(
          t => "error: " + t._1.getMessage,
          x => x match {
            case (200, _, Some(body), _) => "ok: [" + body + "]"
            case (z, _, _, _) => "unexpected status: " + z
          }
        )
      })

}