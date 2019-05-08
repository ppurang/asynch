package org.purang.net.http.ning

import java.nio.charset.StandardCharsets

import org.purang.net.http._
import org.asynchttpclient.{Request => _, Response => AResponse, _}
import scala.concurrent.duration._

import io.netty.handler.codec.http.HttpHeaders
import scalaz.{\/, -\/, \/-}

object `package` {
  implicit val defaultNonBlockingExecutor: DefaultAsyncHttpClientNonBlockingExecutor =
    DefaultAsyncHttpClientNonBlockingExecutor()
}

abstract class AsyncHttpClientNonBlockingExecutor extends NonBlockingExecutor {
  val client: AsyncHttpClient

  import scalaz.concurrent.Task
  def apply(timeout: Timeout): Request => Task[(Status, Vector[Header], Option[String], Request)] = (req: Request) => {
    debug {
      s"Thread ${Thread.currentThread().getName}-${Thread.currentThread().getId} asking for a task to be run $req"
    }

    Task.fork(Task.async[AResponse] { cb =>
      val builder: RequestBuilder = new RequestBuilder(req.method).setUrl(req.url.url)

      for {
        header <- req.headers
        value <- header.values
      } builder.addHeader(header.name, value)

      for {
        content <- req.body
        str <- content
      } builder.setBody(str.getBytes("utf-8"))

      //
      debug{
        s"executing $req on thread '${Thread.currentThread().getName}'-'${Thread.currentThread().getId}'"
      }

      client.executeRequest(builder, new Handler(cb))
      ()
    }).timed(timeout.timeout.millis).map { response =>
      import scala.collection.JavaConverters._
      import org.purang.net.http._

      val headers: Vector[Header] = response.getHeaders.asScala.groupBy(_.getKey).foldLeft(Vector[Header]()) {
        case (hdrs, (key, iterable)) =>
          hdrs ++ (key `:` iterable.map(_.getValue))
      }

      val responseBody: String = response.getResponseBody(StandardCharsets.UTF_8)
      val code: Int = response.getStatusCode
      debug {
        f"'${Thread.currentThread().getName}'-'${Thread.currentThread().getId}' req: $req  %n resp: %n code: $code %n headers: $headers %n body: $responseBody"
      }

      (code, headers, Option(responseBody), req)
    }
  }

}

class Handler(callback: (Throwable \/ AResponse) => Unit) extends AsyncHandler[Unit] {
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
    callback(\/-(builder.build()))

  def onThrowable(t: Throwable): Unit =
    callback(-\/(t))
}

case class DefaultAsyncHttpClientNonBlockingExecutor( config : AsyncHttpClientConfig = {
 new DefaultAsyncHttpClientConfig.Builder()
    .setCompressionEnforced(true)
    .setConnectTimeout(500)
    .setRequestTimeout(3000)
    .setCookieStore(null)
    .build()
})
  extends ConfiguredAsyncHttpClientExecutor {

  def close() : Unit = {
    this.client.close()
  }
}

trait ConfiguredAsyncHttpClientExecutor extends AsyncHttpClientNonBlockingExecutor {
  val config: AsyncHttpClientConfig
  lazy val client: AsyncHttpClient = new DefaultAsyncHttpClient(config)
}