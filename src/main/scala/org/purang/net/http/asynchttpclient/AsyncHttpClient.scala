package org.purang.net.http.asynchttpclient

import org.asynchttpclient.{Request => _, Response => AResponse, _}
import org.asynchttpclient.{AsyncHttpClient => UnderlyingClient}
import io.netty.handler.codec.http.HttpHeaders

import cats.Defer
import cats.data.NonEmptyChain
import cats.effect.Sync

import org.purang.net.http._

object AsyncHttpClient {

  def sync[F[+_]](client: => UnderlyingClient)(implicit S: Sync[F]): F[HttpClient[F]] = S.delay {
    new HttpClient[F] {
      def execute(req: HttpRequest, timeout: Timeout): F[HttpResponse] = S.delay {
        val builder: RequestBuilder = new RequestBuilder(req.method.toString).setUrl(req.url.url)
        for {
          headers <- req.headers
        } {
          for {
            header <- headers.hs
            value <- header.values
          } yield {
            builder.addHeader(header.name, value)
            () //no foreach hence the yield with unit
          }
        }
        for {
          content <- req.body
        } builder.setBody(content.b.getBytes("utf-8"))
        val response =
          client.executeRequest(builder).get(timeout.length, timeout.unit) //oh no!!!!! well we are sync ;)))
        import scala.collection.JavaConverters._
        val headers: Vector[Header] =
          response.getHeaders.asScala.groupBy(_.getKey).foldLeft(Vector[Header]()) { case (hdrs, (key, iterable)) =>
            hdrs :+ (Header(key,
              NonEmptyChain
                .fromSeq(
                  iterable.map(x => x.getValue).toSeq
                )
                .getOrElse(
                  NonEmptyChain.one("!!!!!!!should never happen!!!!!!")
                )))
          }
        val responseBody: String = response.getResponseBody(java.nio.charset.StandardCharsets.UTF_8)
        val code: Int = response.getStatusCode
        HttpResponse(HttpStatus(code), NonEmptyChain.fromSeq(headers.toSeq).map(Headers(_)), Option(Body(responseBody)))
      }
    }
  }

}

case class Handler(callback: Either[Throwable, AResponse] => Unit) extends AsyncHandler[Unit] {
  val builder = new AResponse.ResponseBuilder()

  def onBodyPartReceived(content: HttpResponseBodyPart): AsyncHandler.State = {
    builder.accumulate(content)
    AsyncHandler.State.CONTINUE
  }

  def onStatusReceived(status: HttpResponseStatus): AsyncHandler.State = {
    builder.accumulate(status)
    AsyncHandler.State.CONTINUE
  }

  def onHeadersReceived(headers: HttpHeaders): AsyncHandler.State = {
    builder.accumulate(headers)
    AsyncHandler.State.CONTINUE
  }

  def onCompleted(): Unit =
    callback(Right(builder.build()))

  def onThrowable(t: Throwable): Unit =
    callback(Left(t))
}
