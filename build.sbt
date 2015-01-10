name := "asynch"

version := "0.5.0"

organization := "org.purang.net"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.9.3" withSources(),
  "org.scalaz" %% "scalaz-core" % "7.1.0"  withSources(),
  "org.scalaz" %% "scalaz-concurrent" % "7.1.0"  withSources(),
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked", "-language:_")

cancelable := true

fork := true

seq(bintrayPublishSettings:_*)

licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

initialCommands in console :=
  """
    |import org.purang.net.http._
    |import scalaz._, Scalaz._
    |import org.purang.net.http.ning._
    |
    |implicit val sse = java.util.concurrent.Executors.newScheduledThreadPool(2)
    |
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
  """.stripMargin //to exit the console sse.close and defaultNonBlockingExecutor.close
