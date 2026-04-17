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
import scala.concurrent.duration.*
import scala.jdk.javaapi.DurationConverters.toJava

import java.util.concurrent.TimeUnit

@main def start(): Unit =
  val req = GET > "https://httpbin.org/delay/0" >> Headers(NonEmptyChain(Accept(ApplicationJson)))

  val config = new DefaultAsyncHttpClientConfig.Builder()
    .setCompressionEnforced(true)
    .setConnectTimeout(toJava(500.millis))
    .setRequestTimeout(toJava(5000.millis))
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
             Timeout(2013, TimeUnit.MILLISECONDS)
           )
    } yield r.show).attempt.guarantee(IO(underlyingclient.close)).unsafeRunSync()
  ) // this is just a quick and dirty example; don't do this in production code, use a Resource instead
