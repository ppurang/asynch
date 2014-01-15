name := "asynch"

version := "0.3.0"

organization := "org.purang.net"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.7.22" withSources(),
  "org.scalaz" %% "scalaz-core" % "7.0.5"  withSources(),
  "org.scalaz" %% "scalaz-concurrent" % "7.0.5"  withSources(),
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  )

resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "hexx-releases" at "https://github.com/hexx/maven/tree/gh-pages/releases"
)

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked", "-language:_")

cancelable := true

seq(bintrayPublishSettings:_*)

licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))