ThisBuild / name := "asynch"
ThisBuild / version := "0.9.0"
ThisBuild / organization := "org.purang.net"
ThisBuild / scalaVersion := "3.0.0-M3"

ThisBuild / update / evictionWarningOptions := EvictionWarningOptions.empty
ThisBuild / resolvers += Resolver.bintrayRepo("ppurang", "maven")

ThisBuild / fork := true
ThisBuild / logBuffered := false
ThisBuild / parallelExecution in Test := true

ThisBuild / publishArtifact in packageDoc := false
ThisBuild / publishArtifact in packageSrc := true
ThisBuild / publishArtifact in packageSrc in Test := false
ThisBuild / licenses += ("BSD", url("https://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

Global / turbo := true
Global / cancelable := true
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / lintUnusedKeysOnLoad := false

ThisBuild / scalacOptions ++=  Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-deprecation",
    "-language:implicitConversions", 
    "-Ykind-projector"
  ) 

ThisBuild / libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.12.2",
  "org.typelevel" %% "cats-effect" % "3.0.0-M5",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.scalameta" %% "munit" % "0.7.20"  % Test
).map(_.withSources())

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

ThisBuild / initialCommands in console :=
  """
    |import org.purang.net.http._
    |import org.purang.net.http.asynchttpclient.AsyncHttpClient
    |import org.asynchttpclient.{DefaultAsyncHttpClientConfig, DefaultAsyncHttpClient, AsyncHttpClient => UnderlyingHttpClient}
    |
    |import cats.data.NonEmptyChain
    |import cats.effect.IO
    |import cats.effect.unsafe.implicits.global
    |import cats.syntax.all._
    |
    |import java.util.concurrent.TimeUnit
    |
  """.stripMargin //to exit the console sse.close and defaultNonBlockingExecutor.close
