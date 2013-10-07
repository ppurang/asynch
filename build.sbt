name := "asynch"

version := "0.2.9"

organization := "org.purang.net"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.7.19" withSources(),
  "org.scalaz" %% "scalaz-core" % "6.0.4"  withSources(),
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-language: _")

cancelable := true