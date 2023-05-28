enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

ThisBuild / name         := "asynch"
ThisBuild / version      := "3.2.2"
ThisBuild / organization := "org.purang.net"
ThisBuild / scalaVersion := "3.2.2"

ThisBuild / crossScalaVersions := Seq("3.2.2", "2.13.10")
ThisBuild / versionScheme      := Some("early-semver")

ThisBuild / update / evictionWarningOptions := EvictionWarningOptions.empty

ThisBuild / fork                     := true
ThisBuild / logBuffered              := false
ThisBuild / Test / parallelExecution := true

//ThisBuild / publishArtifact in packageDoc := false
This / sbt.Compile / doc / sources              := Seq()
ThisBuild / packageSrc / publishArtifact        := true
ThisBuild / Test / packageSrc / publishArtifact := false
ThisBuild / licenses += ("BSD", url("https://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

Global / turbo                := true
Global / cancelable           := true
Global / onChangedBuildSource := ReloadOnSourceChanges
Global / lintUnusedKeysOnLoad := false

ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-deprecation",
  "-language:implicitConversions"
) ++ {
  if (scalaVersion.value.matches("^3.")) {
    Seq("-Ykind-projector")
  } else if (scalaVersion.value.matches("^2.12")) {
    Seq("-language:higherKinds")
  } else {
    Seq()
  }
}

val nettyVersion =
  "4.1.93.Final"

ThisBuild / libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client" % "2.12.3",
  "org.typelevel"      %% "cats-effect"       % "3.4.8",
  "io.netty"            % "netty-codec"       % nettyVersion,
  "io.netty"            % "netty-codec-http"  % nettyVersion,
  "io.netty"            % "netty-common"      % nettyVersion,
  "io.netty"            % "netty-handler"     % nettyVersion,
  "ch.qos.logback"      % "logback-classic"   % "1.4.6"  % Test,
  "org.scalameta"      %% "munit"             % "0.7.29" % Test
).map(_.withSources())

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

ThisBuild / console / initialCommands :=
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
  """.stripMargin // to exit the console sse.close and defaultNonBlockingExecutor.close

publishTo := sonatypePublishToBundle.value
