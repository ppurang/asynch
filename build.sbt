name := "asynch"

version := "0.7.10"

organization := "org.purang.net"

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.8", "2.12.8")

libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.8.1" withSources(),
  "org.scalaz" %% "scalaz-core" % "7.2.27"  withSources(),
  "org.scalaz" %% "scalaz-concurrent" % "7.2.27"  withSources(),
  "org.scalatest" %% "scalatest" % "3.0.7" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
  )

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match", // Pattern match may not be typesafe.
  "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification", // Enable partial unification in type constructor inference
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)
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
