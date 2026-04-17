addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.11.3")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")

addDependencyTreePlugin

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")

addSbtPlugin("com.github.sbt" % "sbt-github-actions" % "0.24.0")

//addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.9")

addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.13.8" cross CrossVersion.full)
