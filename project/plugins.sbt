resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.5")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.9")
