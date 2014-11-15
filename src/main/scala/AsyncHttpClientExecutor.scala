package org.purang.net
package http.ning

import com.ning.http.client.AsyncHandler.STATE
import org.purang.net.http._
import com.ning.http.client.{AsyncHttpClientConfig, HttpResponseBodyPart, HttpResponseStatus, AsyncHandler, HttpResponseHeaders, RequestBuilder, AsyncHttpClient, Response => AResponse}
import java.lang.{String, Throwable}
import java.util.{List => JUL}
import java.util.concurrent.{TimeUnit, ThreadFactory, ExecutorService, Executors}
import java.util.concurrent.atomic.AtomicInteger

object `package` {
  implicit val nonblockingexecutor = DefaultAsyncHttpClientNonBlockingExecutor

  private object DefaultThreadFactory extends ThreadFactory {
    //based on java.util.concurrent.Executors.DefaultThreadFactory
    val group: ThreadGroup = {
      val s = System.getSecurityManager()
      if (s != null) s.getThreadGroup() else Thread.currentThread().getThreadGroup()
    }
    val threadNumber = new AtomicInteger(1)
    val namePrefix = "org.purang.net.http.ning.pool"

    def newThread(r: Runnable): Thread = {

      val t = new Thread(group, r, s"$namePrefix-${threadNumber.getAndIncrement()}")
      debug{
        s"new thread ${t.getName}"
      }
      if (!t.isDaemon())
        t.setDaemon(true)
      if (t.getPriority() != Thread.NORM_PRIORITY)
        t.setPriority(Thread.NORM_PRIORITY)
      t
    }
  }
  implicit val pool: ExecutorService = Executors.newCachedThreadPool(DefaultThreadFactory)
}



trait AsyncHttpClientNonBlockingExecutor extends NonBlockingExecutor {
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
        (new Handler(): AsyncHandler[AResponse])
      ).get(timeout, TimeUnit.MILLISECONDS)


      import scala.collection.JavaConversions._
      import org.purang.net.http._
      val headers = mapAsScalaMap(response.getHeaders).foldLeft(Vector[Header]()) {
        (x: Vector[Header], y: Tuple2[String, JUL[String]]) => {
          x ++ (y._1 `:` collectionAsScalaIterable(y._2))
        }
      }
      val responseBody: String = response.getResponseBody("UTF-8")
      val code: Int = response.getStatusCode()
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

object DefaultAsyncHttpClientNonBlockingExecutor extends ConfiguredAsyncHttpClientExecutor with AsyncHttpClientNonBlockingExecutor {
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

trait ConfiguredAsyncHttpClientExecutor {
  val config: AsyncHttpClientConfig
  lazy val client: AsyncHttpClient = new AsyncHttpClient(config)
}