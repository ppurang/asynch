package org.purang.net.http.asynchttpclient

import org.asynchttpclient.{ Response => AResponse, Request => _, _ }
import org.asynchttpclient.{ AsyncHttpClient => UnderlyingClient }
import org.asynchttpclient.{ AsyncHandler => Handler }
import io.netty.handler.codec.http.HttpHeaders
import cats.Defer
import cats.data.NonEmptyChain
import cats.effect.Sync
import cats.effect.Async
import cats.effect.Deferred
import cats.effect.Concurrent
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.purang.net.http._
import Constants.UTF8

import scala.concurrent.duration.FiniteDuration

object AsyncHttpClient {

  val responseToResponse: AResponse => HttpResponse = response => {
    import scala.jdk.CollectionConverters._
    val headers: Vector[Header] =
      response.getHeaders.asScala.groupBy(_.getKey).foldLeft(Vector[Header]()) { case (hdrs, (key, iterable)) =>
        hdrs :+ (Header(
          key,
          NonEmptyChain
            .fromSeq(
              iterable.map(x => x.getValue).toSeq
            )
            .getOrElse(
              NonEmptyChain.one("!!!!!!!should never happen!!!!!!")
            )
        ))
      }
    val responseBody: String    = response.getResponseBody(UTF8)
    val code: Int               = response.getStatusCode
    HttpResponse(HttpStatus(code), NonEmptyChain.fromSeq(headers.toSeq).map(Headers(_)), Option(Body(responseBody)))
  }

  // Do we really need to delay creation of HttpClient ... would HttpClient[F] (instead of F[HttpClient[F]]) be enough?
  def sync[F[+_]](client: => UnderlyingClient)(implicit S: Sync[F]): F[HttpClient[F]] = S.delay {
    new HttpClient[F] {
      def execute(req: HttpRequest, timeout: Timeout): F[HttpResponse] = S.blocking {
        val builder: RequestBuilder = new RequestBuilder(req.method.toString).setUrl(req.url.url)
        for {
          headers <- req.headers
        } {
          for {
            header <- headers.hs
            value  <- header.values
          } yield {
            builder.addHeader(header.name, value)
            () // no foreach hence the yield with unit
          }
        }
        for {
          content <- req.body
        } builder.setBody(content.b.getBytes(UTF8))
        val response =
          client.executeRequest(builder).get(timeout.length, timeout.unit) // oh no!!!!! well we are sync ;)))
        responseToResponse(response)
      }
    }
  }

  def async[F[+_]](client: => UnderlyingClient)(implicit
      A: Async[F]
      // , C: Concurrent[F]
  ): F[HttpClient[F]] = A.delay {
    new HttpClient[F] {
      def execute(req: HttpRequest, timeout: Timeout): F[HttpResponse] = {
        for {
          ft        <- A.delay(FiniteDuration(timeout.length, timeout.unit))
          aresponse <- A.timeout(
                         A.async_[AResponse] { cb =>
                           val builder: RequestBuilder = new RequestBuilder(req.method.toString).setUrl(req.url.url)
                           for {
                             headers <- req.headers
                           } {
                             for {
                               header <- headers.hs
                               value  <- header.values
                             } yield {
                               builder.addHeader(header.name, value)
                               () // no foreach hence the yield with unit
                             }
                           }
                           for {
                             content <- req.body
                           } {
                             builder.setBody(content.b.getBytes(UTF8))
                           }
                           client.executeRequest(builder, CustomHandler(cb))
                         },
                         ft
                       )
        } yield {
          responseToResponse(aresponse)
        }
      }
    }
  }

}

case class CustomHandler(callback: Either[Throwable, AResponse] => Unit) extends Handler[Unit] {
  val builder = new AResponse.ResponseBuilder()

  def onBodyPartReceived(content: HttpResponseBodyPart): Handler.State = {
    builder.accumulate(content)
    Handler.State.CONTINUE
  }

  def onStatusReceived(status: HttpResponseStatus): Handler.State = {
    builder.accumulate(status)
    Handler.State.CONTINUE
  }

  def onHeadersReceived(headers: HttpHeaders): Handler.State = {
    builder.accumulate(headers)
    Handler.State.CONTINUE
  }

  def onCompleted(): Unit =
    callback(Right(builder.build()))

  def onThrowable(t: Throwable): Unit =
    callback(Left(t))
}
