package org.purang.net.http.ning

import java.nio.charset.StandardCharsets

import org.purang.net.http._
import org.asynchttpclient.{Request => _, Response => AResponse, _}
import java.util.concurrent.TimeUnit

import io.netty.handler.codec.http.HttpHeaders

object `package` {
  implicit val defaultNonBlockingExecutor = DefaultAsyncHttpClientNonBlockingExecutor()
}

abstract class AsyncHttpClientNonBlockingExecutor extends NonBlockingExecutor {
  val client: AsyncHttpClient

  import scalaz.concurrent.Task
  def apply(timeout: Timeout) = (req: Request) => {
    debug {
      s"Thread ${Thread.currentThread().getName}-${Thread.currentThread().getId} asking for a task to be run $req"
    }
    Task.apply({
      var builder = new RequestBuilder(req.method).setUrl(req.url.url)
      for {
        header <- req.headers
        value <- header.values
      } builder = builder.addHeader(header.name, value)

      for {
        content <- req.body
        str <- content
      } builder = builder.setBody(str.getBytes("utf-8"))

      //
      debug{
        s"blocking to execute $req on thread '${Thread.currentThread().getName}'-'${Thread.currentThread().getId}'"
      }
      val response: AResponse = client.executeRequest[AResponse](
        builder.build(),
        new Handler(): AsyncHandler[AResponse]
      ).get(timeout, TimeUnit.MILLISECONDS)


      import scala.collection.JavaConverters._
      import org.purang.net.http._

      implicit val mapEntryToTuple : java.util.Map.Entry[String,String] => (String, String) = x => x.getKey -> x.getValue
      val headers = response.getHeaders.asScala.groupBy(_.getKey).foldLeft(Vector[Header]()) {
        case (hdrs, (key, iterable)) =>
          hdrs ++ (key `:` iterable.map(_.getValue))
      }

      val responseBody: String = response.getResponseBody(StandardCharsets.UTF_8)
      val code: Int = response.getStatusCode
      debug {
        f"'${Thread.currentThread().getName}'-'${Thread.currentThread().getId}' req: $req  %n resp: %n code: $code %n headers: $headers %n body: $responseBody"
      }

      (code, headers, Option(responseBody), req)
    })
  }

}

class Handler extends AsyncHandler[AResponse] {
  val builder =
    new AResponse.ResponseBuilder()

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

  def onCompleted(): AResponse = {
    builder.build()
  }

  def onThrowable(t: Throwable) {
    throw t
  }
}

case class DefaultAsyncHttpClientNonBlockingExecutor( config : AsyncHttpClientConfig = {
 new DefaultAsyncHttpClientConfig.Builder()
    .setCompressionEnforced(true)
    .setConnectTimeout(500)
    .setRequestTimeout(3000)
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