package org.purang.net
package http.ning

import com.ning.http.client.AsyncHandler.STATE
import org.purang.net.http.`package`._
import com.ning.http.client.{ProxyServer, AsyncHttpClientConfig, HttpResponseBodyPart, HttpResponseStatus, AsyncHandler, HttpResponseHeaders, RequestBuilder, AsyncHttpClient, Response => AResponse}
import java.lang.{String, Throwable}
import java.util.{List => JUL}
import http.Request
import collection.{Iterable, immutable}

object `package` {
  implicit val executor = DefaultAsyncHttpClientExecutor
}

trait AsyncHttpClientExecutor extends (Request => Response) {
  val client: AsyncHttpClient

  def apply(req: Request) = {
    throwableToLeft {
      var builder = new RequestBuilder(req.method).setUrl(req.url)
      for (header <- req.headers;
           value <- header.values
      ) builder = builder.addHeader(header.name, value)
      for (content <- req.body;
           str <- content
      ) builder = builder.setBody(str)

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
      (response.getStatusCode(), headers, Option(response.getResponseBody("UTF-8")))
    }
  }
}

class Handler extends AsyncHandler[AResponse] {
  val builder =
          new AResponse.ResponseBuilder();

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
    builder.build();
  }

  def onThrowable(t: Throwable) {
    throw t
  }
}

object DefaultAsyncHttpClientExecutor extends ConfiguredAsyncHttpClientExecutor {
  lazy val config: AsyncHttpClientConfig = new AsyncHttpClientConfig.Builder().setCompressionEnabled(true)
          //.setProxyServer(new ProxyServer("172.16.42.42", 8080))
          .setAllowPoolingConnection(true)
          .setConnectionTimeoutInMs(500)
          .setRequestTimeoutInMs(3000).build();
}

trait ConfiguredAsyncHttpClientExecutor extends AsyncHttpClientExecutor {
  val config: AsyncHttpClientConfig
  lazy val client: AsyncHttpClient = new AsyncHttpClient(config)
}
