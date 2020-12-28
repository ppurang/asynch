package org.purang.net.http

import HttpRequest._
import cats.data.NonEmptyChain
import cats.syntax.all._

class HttpRequestSpec extends munit.FunSuite {
  test("show with headers and body") {
    val r = GET > "https://www.google.com" >> Headers(NonEmptyChain(Accept(TextHtml))) >>> "Hmmmmmmmm"

    assertEquals(
      r.show,
      s"""GET https://www.google.com${Constants.CRLF}Accept: text/html${Constants.CRLF}Hmmmmmmmm"""
    )
  }

  test("show with headers only") {
    val r = GET > "https://www.google.com" >> Headers(NonEmptyChain(Accept(TextHtml)))

    assertEquals(
      r.show,
      s"""GET https://www.google.com${Constants.CRLF}Accept: text/html${Constants.CRLF}"""
    )
  }

  test("show with body only") {
    val r = GET > "https://www.google.com" >>> "Hmmmmmmmm"

    assertEquals(
      r.show,
      s"""GET https://www.google.com${Constants.CRLF}Hmmmmmmmm"""
    )
  }

  test("enable a synchronous http request") {
    val req = GET > "https://httpbin.org/get" >> Headers(NonEmptyChain(Accept(ApplicationJson))) >>> "Hmmmmmmmm"

    import org.asynchttpclient.{Request => _, Response => AResponse, _}

    val config = new DefaultAsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setConnectTimeout(500)
      .setRequestTimeout(3000)
      .setCookieStore(null)
      .build()

    val underlyingclient: AsyncHttpClient = new DefaultAsyncHttpClient(config)
    import cats.effect.IO
    import cats.effect.implicits._
    import cats.implicits._
    import cats.syntax.all._
    import cats.effect.syntax.all._
    import java.util.concurrent.TimeUnit

    val result = for {
      c <- org.purang.net.http.asynchttpclient.AsyncHttpClient.sync[IO](
        underlyingclient
      )
      r <- c.execute(
        req,
        Timeout(2000, TimeUnit.MILLISECONDS)
      )
    } yield r

    import cats.effect.unsafe.implicits.global

    assertEquals(result.map(_.status).attempt.unsafeRunSync(), Right(HttpStatus(200)))
  }

}
