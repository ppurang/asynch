package org.purang.net

package http

import java.util.concurrent.{Executors, ScheduledExecutorService}

import org.asynchttpclient._
import org.scalatest.{FlatSpec, Matchers}
import org.purang.net.http.ning.{defaultNonBlockingExecutor => _, _}

import scalaz._
import Scalaz._

class CustomNingExecutorSpec extends FlatSpec with Matchers {

  "ning package" should "allow configuring and using async http client executor" in {
    implicit val sse = Executors.newScheduledThreadPool(2)
    val config = new DefaultAsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setConnectTimeout(500)
      .setRequestTimeout(3000)
      .build()
    implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
    val url = "http://www.google.com"
    val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

    (url >> headers).~>((x: ExecutedRequest) => x.fold(
      t => {
        t._1.printStackTrace; -1
      },
      (status: Status, headers: Headers, body: Body, req: Request) => status
    )) should be(200)
  }

  it should "allow configuring and using multiple async http client executor" in {
    {
      implicit val sse = Executors.newScheduledThreadPool(2)

      val config = new DefaultAsyncHttpClientConfig.Builder()
        .setCompressionEnforced(true)
        .setConnectTimeout(500)
        .setRequestTimeout(3000)
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        (status: Status, headers: Headers, body: Body, req: Request) => status
      )) should be(200)

    }
    {
      implicit val sse = Executors.newScheduledThreadPool(2)
      val config = new DefaultAsyncHttpClientConfig.Builder()
        .setCompressionEnforced(true)
        .setConnectTimeout(500)
        .setRequestTimeout(3000)
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        (status: Status, headers: Headers, body: Body, req: Request) => status
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
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        (status: Status, headers: Headers, body: Body, req: Request) => status
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
        .build()
      implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {
          t._1.printStackTrace;
          -1
        },
        (status: Status, headers: Headers, body: Body, req: Request) => status
      )) should be(200)
    }
  }

}
