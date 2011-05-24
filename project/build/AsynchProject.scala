import sbt._

/**
 *
 * @author Piyush Purang
 */

class AsynchProject (info: ProjectInfo) extends DefaultProject(info) {

  //override def packageDocsJar = defaultJarPath("-javadoc.jar")
  override def packageSrcJar= defaultJarPath("-sources.jar")

  val sourceArtifact = Artifact.sources(artifactID)
  //val docsArtifact = Artifact.javadoc(artifactID)

  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)


  val asyncHttpClient= "com.ning" % "async-http-client" % "1.6.3" withSources ()
  val scalazCore = "org.scalaz" %% "scalaz-core" % "6.0-SNAPSHOT"  withSources()

  val scalatest = "org.scalatest" %% "scalatest" % "1.4.1" % "test" withSources()


  val scalaTools = "scala tools repo" at "http://www.scala-tools.org/repo-releases"
  val scalaToolsSnapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
  val ivyLocal = "Local Ivy Repository" at "file://" + Path.userHome + "/.ivy2/local"
  //val twitter = "twitter maven repo" at "http://maven.twttr.com/"

  val sonatype_repo = "Sonatype" at "http://oss.sonatype.org/content/repositories/releases/"
  //val scala_tools_repo  = MavenRepository("Scala Tools",  "http://scala-tools.org/repo-snapshots/")
  //val jboss_repo        = MavenRepository("JBoss",        "http://repository.jboss.org/nexus/content/groups/public/")
  //val akka_repo         = MavenRepository("Akka",         "http://scalablesolutions.se/akka/repository/")


}