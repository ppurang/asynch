package org.purang.net.http

import java.util.concurrent.TimeUnit
import cats.syntax.either._
import org.asynchttpclient.{ DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig }
import org.purang.util.concurrent.DefaultThreadFactory

object HttpRequestSpecConstants {
  val saneConnectTimeout: Timeout        = Timeout(500L, TimeUnit.MILLISECONDS)
  val saneTimeout: Timeout               = Timeout(5000L, TimeUnit.MILLISECONDS)
  val shorterTimeout: Timeout            = Timeout(1000L, TimeUnit.MILLISECONDS)
  val OK: HttpStatus                     = HttpStatus(200)
  val OKR: Either[Throwable, HttpStatus] = OK.asRight

  val underlyingClient = {

    val config = new DefaultAsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setConnectTimeout(saneConnectTimeout.msJDuration)
      .setRequestTimeout(saneTimeout.msJDuration)
      .setThreadFactory(
        new DefaultThreadFactory(
          "HttpRequestSpecConstants.client",
          true,
          10,
          Thread.currentThread().getUncaughtExceptionHandler
        )
      )
      .setCookieStore(null)
      .build()

    new DefaultAsyncHttpClient(config)
  }
}
