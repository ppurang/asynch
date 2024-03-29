package org.purang.net.http

import org.purang.net.http.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.{
  DefaultAsyncHttpClientConfig,
  DefaultAsyncHttpClient,
  AsyncHttpClient => UnderlyingHttpClient
}

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.show._

import java.util.concurrent.TimeUnit

@main def start(): Unit =
  val req = GET > "https://httpbin.org/delay/0" >> Headers(NonEmptyChain(Accept(ApplicationJson)))

  val config = new DefaultAsyncHttpClientConfig.Builder()
    .setCompressionEnforced(true)
    .setConnectTimeout(500)
    .setRequestTimeout(3000)
    .setCookieStore(null)
    .build()

  val underlyingclient: UnderlyingHttpClient = new DefaultAsyncHttpClient(config)

  println(
    (for {
      c <- AsyncHttpClient.sync[IO](
             underlyingclient
           )
      r <- c.execute(
             req,
             Timeout(2000, TimeUnit.MILLISECONDS)
           )
    } yield r.show).attempt.guarantee(IO(underlyingclient.close)).unsafeRunSync()
  ) // this is just a quick and dirty example; don't do this in production code, use a Resource instead
