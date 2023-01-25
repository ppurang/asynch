package org.purang.net.http

import org.purang.util.concurrent.DefaultThreadFactory

import java.util.concurrent.{ TimeUnit, TimeoutException }
import cats.data.NonEmptyChain
import cats.syntax.all._

class HttpRequestShowSpec extends munit.FunSuite {

  test("show with headers and body") {
    val r = GET > "https://www.google.com" >> Headers(NonEmptyChain(Accept(TextHtml))) >>> "Hmmmmmmmm"

    assertEquals(
      r.show,
      s"""GET https://www.google.com${Constants.CRLF}ACCEPT: text/html${Constants.CRLF}Hmmmmmmmm"""
    )
  }

  test("show with headers only") {
    val r = GET > "https://www.google.com" >> Headers(NonEmptyChain(Accept(TextHtml)))

    assertEquals(
      r.show,
      s"""GET https://www.google.com${Constants.CRLF}ACCEPT: text/html${Constants.CRLF}"""
    )
  }

  test("show with body only") {
    val r = GET > "https://www.google.com" >>> "Hmmmmmmmm"

    assertEquals(
      r.show,
      s"""GET https://www.google.com${Constants.CRLF}Hmmmmmmmm"""
    )
  }

}
