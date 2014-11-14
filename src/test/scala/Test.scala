package org.purang.net

package http

import com.ning.http.client.AsyncHttpClient

import concurrent._
import concurrent.duration._
import FiniteDuration._
import ExecutionContext.Implicits.global

object Test {
  import ning._

  implicit val sse = java.util.concurrent.Executors.newScheduledThreadPool(5)

  def eventually[A](i: Int)(a: => A) = {
    Await.ready(Future {
      blocking(Thread.sleep(i)); a
    }, i + 100 milliseconds)
  }

  def main(args: Array[String]) {
    System.setProperty("asynch.debug", "true")
    block(args(0))
    //nonblocking2(args(0), 1000)

    eventually(2000) {
      pool.shutdownNow()
      sse.shutdownNow()
      nonblockingexecutor.client.close()
    }
  }

  def nonblocking(url: String, timeout: Long) = println(" ------------  " + {
    (GET > url).~>>(timeout).attemptRun
  })

  def nonblocking2(url: String, timeout: Long) = println(" ------------  " + {
    (GET > url).~>>(timeout).timed(1000).attemptRun
  })

  def block(url: String) = println((GET > url) ~> {
    _.fold(
    t => "error: " + t._1.getMessage, {
      case (200, _, Some(body), _) => "ok" //"ok: [" + body + "]"
      case (z, _, _, _) => "unexpected status: " + z
    }
    )
  })

}