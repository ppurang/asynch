name := "asynch"

version := "0.2.3"

organization := "org.purang.net"

scalaVersion := "2.9.0"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.6.4" withSources (),
  "org.scalaz" % "scalaz-core_2.9.0-1" % "6.0.1"  withSources(),
  "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
  )

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")