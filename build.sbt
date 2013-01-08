name := "asynch"

version := "0.2.7"

organization := "org.purang.net"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.6.5" withSources (),
  "org.scalaz" % "scalaz-core_2.9.2" % "6.0.4"  withSources(),
  "org.scalatest" % "scalatest_2.9.2" % "1.8" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")

cancelable := true