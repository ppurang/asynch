package org.purang.net.http

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.effect.implicits._
import cats.implicits._
import cats.syntax.all._
import cats.effect.syntax.all._

import org.purang.util.concurrent.DefaultThreadFactory

import java.util.concurrent.{ TimeUnit, TimeoutException }

class HttpRequestSyncSpec extends munit.FunSuite {

  import HttpRequestSpecConstants._

  test("enable a synchronous http request") {
    val req: HttpRequest =
      GET > "https://httpbin.org/get" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    val call: IO[HttpResponse] = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.sync[IO](
             underlyingClient
           )
      r <- c.execute(
             req,
             saneTimeout
           )
    } yield r

    import cats.effect.unsafe.implicits.global
    assertEquals(call.map(_.status).attempt.unsafeRunSync(), OKR)
  }

  test("a synchronous http request should timeout") {
    val req: HttpRequest =
      GET > "https://httpbin.org/delay/3" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    val call: IO[HttpResponse] = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.sync[IO](
             underlyingClient
           )
      r <- c.execute(
             req,
             shorterTimeout
           )
    } yield r

    import cats.effect.unsafe.implicits.global
    val result = call.map(_.status).attempt.unsafeRunSync()
    assert(
      result.isLeft &&
        result.fold(
          {
            case _: TimeoutException => true
            case _                   => false
          },
          _ => false
        )
    )
  }

}
