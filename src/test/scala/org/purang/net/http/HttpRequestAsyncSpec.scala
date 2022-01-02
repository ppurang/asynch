package org.purang.net.http

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.effect.implicits._
import cats.implicits._
import cats.syntax.all._
import cats.effect.syntax.all._

import org.purang.util.concurrent.DefaultThreadFactory

import java.util.concurrent.{TimeUnit, TimeoutException}

class HttpRequestAsyncSpec extends munit.FunSuite {
  val saneTimeout      = Timeout(2000L, TimeUnit.MILLISECONDS)
  val shorterTimeout   = Timeout(1000L, TimeUnit.MILLISECONDS)
  val underlyingClient = {
    import org.asynchttpclient.{Request => _, Response => AResponse, _}

    val config = new DefaultAsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setConnectTimeout(500)
      .setRequestTimeout(10000)
      .setThreadFactory(
        new DefaultThreadFactory("HttpRequestSpec.client", true, 10, Thread.currentThread().getUncaughtExceptionHandler)
      )
      .setCookieStore(null)
      .build()

    new DefaultAsyncHttpClient(config)
  }

  test("enable a synchronous http request") {
    val req = GET > "https://httpbin.org/get" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    val call = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.async[IO](
             underlyingClient
           )
      r <- c.execute(
             req,
             saneTimeout
           )
    } yield r

    import cats.effect.unsafe.implicits.global
    assertEquals(call.map(_.status).attempt.unsafeRunSync(), Right(HttpStatus(200)))
  }

  test("a synchronous http request should timeout") {
    val req = GET > "https://httpbin.org/delay/3" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    val call = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.async[IO](
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
            case t: TimeoutException => true
            case _                   => false
          },
          x => false
        )
    )
  }

  test("enable an asynchronous http request") {
    val req = GET > "https://httpbin.org/get" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    val call = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.async[IO](
             underlyingClient
           )
      r <- c.execute(
             req,
             saneTimeout
           )
    } yield r

    import cats.effect.unsafe.implicits.global

    assertEquals(call.map(_.status).attempt.unsafeRunSync(), Right(HttpStatus(200)))
  }

  test("an asynchronous http request should timeout") {
    val req = GET > "https://httpbin.org/delay/5" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    val call = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.async[IO](
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
        result
          .fold(
            {
              case t: TimeoutException => true
              case _                   => false
            },
            x => false
          )
    )
  }

}
