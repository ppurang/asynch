package org.purang.net

package http

import java.util.concurrent.ScheduledExecutorService

import concurrent._
import concurrent.duration._
import FiniteDuration._
import ExecutionContext.Implicits.global

object Test {
  import ning._

  implicit val sse: ScheduledExecutorService = java.util.concurrent.Executors.newScheduledThreadPool(5)

  def eventually[A](i: Int)(a: => A): Future[A] = {
    Await.ready(Future {
      blocking(Thread.sleep(i)); a
    }, i + 100 milliseconds)
  }

  def main(args: Array[String]) {
    System.setProperty("asynch.debug", "true")
    block(args(0))
    //nonblocking2(args(0), 1000)

    eventually(2000) {
      sse.shutdownNow()
      defaultNonBlockingExecutor.close()
    }
  }

  def nonblocking(url: String, timeout: Long): Unit = println(" ------------  " + {
    (GET > url).~>>(timeout).unsafePerformSyncAttempt
  })

  def nonblocking2(url: String, timeout: Long): Unit = println(" ------------  " + {
    (GET > url).~>>(timeout).timed(1000).unsafePerformSyncAttempt
  })

  def block(url: String): Unit = println((GET > url) ~> {
    _.fold(
    t => "error: " + t._1.getMessage, {
      case (200, _, Some(body), _) => "ok" //"ok: [" + body + "]"
      case (z, _, _, _) => "unexpected status: " + z
    }
    )
  })

}