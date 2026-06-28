ThisBuild / name         := "asynch"
ThisBuild / version      := "3.8.4"
ThisBuild / organization := "org.purang.net"
ThisBuild / scalaVersion := "3.8.4"

ThisBuild / crossScalaVersions := Seq(
  "3.8.4",
  "3.3.8",
  "2.13.18"
)

ThisBuild / versionScheme := Some("early-semver")

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
Global / semanticdbEnabled    := true

ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:implicitConversions"
) ++ {
  if (scalaVersion.value.matches("^3.")) {
    Seq("-Ykind-projector")
  } else {
    Seq()
  }
} ++ {
  if (scalaVersion.value.matches("^3.8.*")) {
    Seq("-Werror")
  } else {
    Seq("-Xfatal-warnings")
  }
}

val nettyVersion = "4.2.15.Final"
val munitVersion = "1.3.3"

ThisBuild / libraryDependencies ++= Seq(
  "org.asynchttpclient" % "async-http-client"   % "3.0.11",
  "org.typelevel"      %% "cats-core"           % "2.13.0",
  "org.typelevel"      %% "cats-effect"         % "3.7.0",
  "io.netty"            % "netty-codec"         % nettyVersion,
  "io.netty"            % "netty-codec-http"    % nettyVersion,
  "io.netty"            % "netty-codec-socks"   % nettyVersion,
  "io.netty"            % "netty-codec-dns"     % nettyVersion,
  "io.netty"            % "netty-common"        % nettyVersion,
  "io.netty"            % "netty-handler"       % nettyVersion,
  "io.netty"            % "netty-handler-proxy" % nettyVersion,
  "io.netty"            % "netty-resolver-dns"  % nettyVersion,
  "ch.qos.logback"      % "logback-classic"     % "1.5.37"     % Test,
  "org.scalameta"      %% "munit-scalacheck"    % "1.3.0"      % Test,
  "org.scalameta"      %% "munit"               % munitVersion % Test
).map(_.withSources())

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

ThisBuild / console / initialCommands :=
  """
    |import org.purang.net.http.*
    |import org.purang.net.http.asynchttpclient.AsyncHttpClient
    |import org.asynchttpclient.{DefaultAsyncHttpClientConfig, DefaultAsyncHttpClient, AsyncHttpClient => UnderlyingHttpClient}
    |
    |import cats.data.NonEmptyChain
    |import cats.effect.IO
    |import cats.effect.unsafe.implicits.global
    |import cats.syntax.all.*
    |
    |import java.util.concurrent.TimeUnit
    |
  """.stripMargin // to exit the console sse.close and defaultNonBlockingExecutor.close

ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
