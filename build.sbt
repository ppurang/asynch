ThisBuild / name := "asynch"
ThisBuild / version := "0.7.20"
ThisBuild / organization := "org.purang.net"
ThisBuild / scalaVersion := "2.13.6"
ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.14", "2.13.6")

libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.12.3" withSources(),
  "org.scalaz" %% "scalaz-core" % "7.2.33" withSources(),
  "org.scalaz" %% "scalaz-concurrent" % "7.2.33" withSources(),
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint" // Lint everything.
)

val oldScalacOptions = Seq(
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

scalacOptions ++= (scalaBinaryVersion.value match {
  case "2.13" =>
    Seq(
      "-Wdead-code", // Warn when dead code is identified.
      "-Wextra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Wnumeric-widen", // Warn when numerics are widened.
      "-Wunused",
      "-Wvalue-discard" // Warn when non-Unit expression results are unused.
    )

  case "2.12" =>
    oldScalacOptions ++ Seq(
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    )

  case "2.11" => oldScalacOptions
})

cancelable in Global := true
fork := true
turbo := true
//publishArtifact in packageDoc := false
This / sources in (sbt.Compile, doc) := Seq()
publishArtifact in packageSrc := true
publishArtifact in packageSrc in Test := false
licenses += ("BSD", url("https://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

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

publishTo := sonatypePublishToBundle.value
