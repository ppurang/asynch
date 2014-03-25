package org.purang.net
package http.ning

import com.ning.http.client.AsyncHandler.STATE
import org.purang.net.http._
import com.ning.http.client.{AsyncHttpClientConfig, HttpResponseBodyPart, HttpResponseStatus, AsyncHandler, HttpResponseHeaders, RequestBuilder, AsyncHttpClient, Response => AResponse}
import java.lang.{String, Throwable}
import java.util.{List => JUL}
import java.util.concurrent.{ExecutorService, Executors}

object `package` {
  implicit val executor = DefaultAsyncHttpClientExecutor
  implicit val pool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
}

trait AsyncHttpClientExecutor extends (Request => ExecutedRequest) {
  val client: AsyncHttpClient

  def apply(req: Request): ExecutedRequest = {
    throwableToFailure(req){
      var builder = new RequestBuilder(req.method).setUrl(req.url)
      for (header <- req.headers;
           value <- header.values
      ) builder = builder.addHeader(header.name, value)
      for (content <- req.body;
           str <- content
      ) builder = builder.setBody(str.getBytes("utf-8"))

      val response: AResponse = client.executeRequest[AResponse](builder.build(),
        (new Handler(): AsyncHandler[AResponse]))
              .get()
      import scala.collection.JavaConversions._
      import org.purang.net.http._
      val headers = mapAsScalaMap(response.getHeaders).foldLeft(Vector[Header]()) {
        (x: Vector[Header], y: Tuple2[String, JUL[String]]) => {
          x ++ (y._1 `:` collectionAsScalaIterable(y._2))
        }
      }
      val responseBody: String = response.getResponseBody("UTF-8")
      val code: Int = response.getStatusCode()
      val property: String = System.getProperty("asynch.debug")
      if (property != null && property.toBoolean) {
        println("[AsyncHttpClientExecutor]" + (code, headers, Option(responseBody), req))
      }
      (code, headers, Option(responseBody), req)
    }
  }
}

class Handler extends AsyncHandler[AResponse] {
  val builder =
          new AResponse.ResponseBuilder()

  def onBodyPartReceived(content: HttpResponseBodyPart): STATE = {
      builder.accumulate(content)
      STATE.CONTINUE
  }

  def onStatusReceived(status: HttpResponseStatus): STATE = {
      builder.accumulate(status)
      STATE.CONTINUE
  }

  def onHeadersReceived( headers: HttpResponseHeaders) : STATE = {
      builder.accumulate(headers)
      STATE.CONTINUE
  }

  def onCompleted(): AResponse  = {
    builder.build()
  }

  def onThrowable(t: Throwable) {
    throw t
  }
}

object DefaultAsyncHttpClientExecutor extends ConfiguredAsyncHttpClientExecutor {
  lazy val config: AsyncHttpClientConfig = {
    new AsyncHttpClientConfig.Builder()
      .setCompressionEnabled(true)
      .setAllowPoolingConnection(true)
      .setConnectionTimeoutInMs(500)
      .setRequestTimeoutInMs(3000)
      .setExecutorService(pool)
      .build()
  }
}

trait ConfiguredAsyncHttpClientExecutor extends AsyncHttpClientExecutor {
  val config: AsyncHttpClientConfig
  lazy val client: AsyncHttpClient = new AsyncHttpClient(config)
}