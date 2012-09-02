name := "asynch"

version := "0.2.5"

organization := "org.purang.net"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.6.5" withSources (),
  "org.scalaz" % "scalaz-core_2.9.1" % "6.0.3"  withSources(),
  "org.scalatest" % "scalatest_2.9.1" % "1.6.1" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")

cancelable := true