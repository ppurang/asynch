name := "asynch"

version := "0.2.9"

organization := "org.purang.net"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.7.20" withSources(),
  "org.scalaz" %% "scalaz-core" % "6.0.4"  withSources(),
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