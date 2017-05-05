name := "asynch"

version := "0.7.0"

organization := "org.purang.net"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.0")

libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.1.0-alpha17" withSources(),
  "org.scalaz" %% "scalaz-core" % "7.2.8"  withSources(),
  "org.scalaz" %% "scalaz-concurrent" % "7.2.8"  withSources(),
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.1" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked", "-language:_")

cancelable := true

fork := true

licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

initialCommands in console :=
  """
    |import org.purang.net.http._
    |import scalaz._, Scalaz._
    |import org.purang.net.http.ning.DefaultAsyncHttpClientNonBlockingExecutor
    |import org.asynchttpclient.DefaultAsyncHttpClientConfig
    |
    |implicit val sse = java.util.concurrent.Executors.newScheduledThreadPool(2)
    |val config = new DefaultAsyncHttpClientConfig.Builder()
    |  .setCompressionEnforced(true)
    |  .setConnectTimeout(500)
    |  .setRequestTimeout(3000)
    |  .build()
    |implicit val newExecutor = DefaultAsyncHttpClientNonBlockingExecutor(config)
    |
    |val response = (POST >
    |   "http://httpize.herokuapp.com/post" >>
    |   ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
    |   ("Cache-Control" `:` "no-cache") ++
    |   ("Content-Type" `:` "text/plain") >>>
    |   "some very important message").~>(
    |     (x: ExecutedRequest) => x.fold(
    |        t => t._1.getMessage.left,
    |        {
    |          case (200, _, Some(body), _) => body.right
    |          case (status: Status, headers: Headers, body: Body, req: Request) => status.toString.left
    |        }
    |      ))
    |
    |
    |// close the client
    |// newExecutor.close()
    |// sse.shutdownNow()
  """.stripMargin //to exit the console sse.close and defaultNonBlockingExecutor.close
