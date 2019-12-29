package org.purang.net

package http

import java.util.concurrent.{Executors, ScheduledExecutorService}

import org.asynchttpclient._
import org.scalatest.matchers.should.Matchers
import org.purang.net.http.ning.{defaultNonBlockingExecutor => _, _}
import scalaz._
import Scalaz._
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Random

class CustomNingExecutorSpec extends AnyFlatSpec with Matchers {

  "ning package" should "allow configuring and using async http client executor" in {
    implicit val sse = Executors.newScheduledThreadPool(2)
    val config = new DefaultAsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setConnectTimeout(500)
      .setRequestTimeout(3000)
      .setCookieStore(null)
      .build()

    implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
    val url = "http://www.google.com"
    val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

    (url >> headers).~>((x: ExecutedRequest) => x.fold(
      t => {
        t._1.printStackTrace; -1
      },
      {case (status: Status, _, _, _) => status}
    )) should be(200)
  }

  it should "allow configuring and using multiple async http client executor" in {
    {
      implicit val sse = Executors.newScheduledThreadPool(2)

      val config = new DefaultAsyncHttpClientConfig.Builder()
        .setCompressionEnforced(true)
        .setConnectTimeout(500)
        .setRequestTimeout(3000)
        .setCookieStore(null)
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        {case (status: Status, _, _, _) => status}
      )) should be(200)

    }
    {
      implicit val sse = Executors.newScheduledThreadPool(2)
      val config = new DefaultAsyncHttpClientConfig.Builder()
        .setCompressionEnforced(true)
        .setConnectTimeout(500)
        .setRequestTimeout(3000)
        .setCookieStore(null)
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        {case (status: Status, _, _, _) => status}
      )) should be(200)
    }
  }

  it should "allow configuring and using multiple async http client executor even when one is closed" in {
    {
      implicit val sse = Executors.newScheduledThreadPool(2)

      val config = new DefaultAsyncHttpClientConfig.Builder()
        .setCompressionEnforced(true)
        .setConnectTimeout(500)
        .setRequestTimeout(3000)
        .setCookieStore(null)
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        {case (status: Status, _, _, _) => status}
      )) should be(200)

      newExecutor.close()
      sse.shutdownNow()
    }
    {
      implicit val sse: ScheduledExecutorService = Executors.newScheduledThreadPool(2, DefaultThreadFactory("CustomNingExecutorSpec.TF.scheduler"))
      val config = new DefaultAsyncHttpClientConfig.Builder()
        .setCompressionEnforced(true)
        .setConnectTimeout(500)
        .setRequestTimeout(3000)
        .setThreadFactory(DefaultThreadFactory("CustomNingExecutorSpec.TF.client"))
        .setCookieStore(null)
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        {case (status: Status, _, _, _) => status}
      )) should be(200)
    }
  }

  it should "execute requests using tasks with cookies involved" in {

    val config: DefaultAsyncHttpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setConnectTimeout(500)
      .setRequestTimeout(2000)
      .setCookieStore(null)
      .build()
    implicit val newExecutor: DefaultAsyncHttpClientNonBlockingExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)

    import com.sun.net.httpserver._
    import java.net._

    val port: Int = Random.nextInt(10000) + 8000

    val server: HttpServer = HttpServer.create(new InetSocketAddress(port), 0)

    server.createContext("/", new HttpHandler {
      def handle(exchange: HttpExchange): Unit = {
        val headers: com.sun.net.httpserver.Headers = exchange.getResponseHeaders
        headers.add("Set-Cookie", "id=a3fWa; Expires=Wed, 21 Oct 3000 07:28:00 GMT; HttpOnly")
        exchange.sendResponseHeaders(200, 0)
        exchange.getResponseBody.write("hello".getBytes)
        exchange.getResponseBody.close()
      }
    })

    server.start()

    val task: NonBlockingExecutedRequest = (GET > s"http://localhost:$port/").~>>(2000L)

    val shouldNotFail : \/[Int, Header] = task.unsafePerformSyncAttempt.fold(
      _ => -1.left,
      r => r._2.find(_.name.toLowerCase == "set-cookie").toRightDisjunction(-1)
    )

    server.stop(3)

    shouldNotFail should not be (-1.left)
  }

}
