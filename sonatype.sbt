sonatypeProfileName := "org.purang"

publishMavenStyle := true

// Open-source license of your choice
//licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("ppurang", "asynch", "ppurang@gmail.com"))

// or if you want to set these fields manually
//homepage := Some(url("https://(your project url)"))
//scmInfo := Some(
//  ScmInfo(
//    url("https://github.com/(account)/(project)"),
//    "scm:git@github.com:(account)/(project).git"
//  )
//)
//developers := List(
//  Developer(id="(your id)", name="(your name)", email="(your e-mail)", url=url("(your home page)"))
//)