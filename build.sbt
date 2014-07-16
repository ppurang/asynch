name := "asynch"

version := "0.4.0"

organization := "org.purang.net"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.8.12" withSources(),
  "org.scalaz" %% "scalaz-core" % "7.0.6"  withSources(),
  "org.scalaz" %% "scalaz-concurrent" % "7.0.6"  withSources(),
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked", "-language:_")

cancelable := true

fork := true

seq(bintrayPublishSettings:_*)

licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))