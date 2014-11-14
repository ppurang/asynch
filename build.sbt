name := "asynch"

version := "0.4.5"

organization := "org.purang.net"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.8.14" withSources(),
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