## Prologue

Sbt library dependency

    libraryDependencies += "org.purang.net" %% "asynch" %"0.5.0" withSources()

From

    resolvers += "ppurang bintray" at " http://dl.bintray.com/ppurang/maven"

## Quick

The code below executes a **blocking** `POST` against `http://httpize.herokuapp.com/post` with request headers `Accept: application/json, text/html, text/plain`,  `Cache-Control: no-cache` and `Content-Type: text/plain`, and request entity `some very important message`. It expects a `200` with some response body. If it encounters an exception or another status code then they are returned too. The type returned is `\/[String, String]`: left (`-\/[String]`) String indicates the error, and the right (`\/-[String]`) String contains the successful response body.


```scala
    import org.purang.net.http._
    import scalaz._, Scalaz._
    import org.purang.net.http.ning._

    implicit val sse = java.util.concurrent.Executors.newScheduledThreadPool(2) //needed for timeouts

    (POST >
    "http://httpize.herokuapp.com/post" >>
    ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
    ("Cache-Control" `:` "no-cache") ++
    ("Content-Type" `:` "text/plain") >>>
    "some very important message").~>(
      (x: ExecutedRequest) => x.fold(
         t => t._1.getMessage.left,
         {
           case (200, _, Some(body), _) => body.right
           case (status: Status, headers: Headers, body: Body, req: Request) => status.toString.left
         }
       )
    )
```

For examples of **non blocking/ asynchronous calls** look at  [src/test/scala/NonBlockingExecutorSpec.scala](https://github.com/ppurang/asynch/blob/master/src/test/scala/NonBlockingExecutorSpec.scala)

For examples of **blocking calls** look at  [src/test/scala/ExecutorSpec.scala](https://github.com/ppurang/asynch/blob/master/src/test/scala/ExecutorSpec.scala)

For an example of a **custom configured executor** look at   [src/test/scala/CustomNingExecutorSpec.scala](https://github.com/ppurang/asynch/blob/master/src/test/scala/CustomNingExecutorSpec.scala)


```scala
    implicit val sse = Executors.newScheduledThreadPool(2)
    val pool = Executors.newCachedThreadPool(DefaultThreadFactory())
    val config = new AsyncHttpClientConfig.Builder()
      .setCompressionEnforced(true)
      .setAllowPoolingConnections(true)
      .setConnectTimeout(500)
      .setRequestTimeout(3000)
      .setExecutorService(pool)
      .build()
    implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config, pool.just)
```

## Testing support? Easy.

Here is an example of test executor [src/test/scala/TestExecutor.scala](https://github.com/ppurang/asynch/blob/master/src/test/scala/TestExecutor.scala)
 that looks up things in a Map used internally to test


## Philosophy

0. Timeouts - Yes! we do timeouts.
1. Immutable - API to assemble requests and response handling is immutable.
2. Easy parts are easy (if you can look beyond weird operators and operator precedence). For example a request is easy to assemble
`GET > "http://www.google.com"` actually even the `GET` isn't really needed either `("http://www.host.com" >> Accept(ApplicationJson))`.
3. Full control -  you are forced to deal with the exceptions and responses. You even have the request that gt executed if you wanted to modify it to re-execute.
4. Parts are done with scalaz goodness.


## Limitations

    1. Entity bodies can only be strings or types that can implictly be converted to strings. No endless Streams.
    2. No explicit Authentication support.
    3. No web socket or such support.
    4. Underlying http call infrastructure is as asynchronous, fast, bug-free as async-http-client.
    5. No metrics and no circuit breakers.


## Help/Join

Critique is sought actively. Help will be provided keenly. Contributions are welcome. Install simple build tool 0.10+, fork the repo and get hacking.


## LICENSE

```scala

licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

```

## Disclaimer

Use at your own risk. See LICENSE for more details.