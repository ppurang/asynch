resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.6.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.15")

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")

addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.4.3")
