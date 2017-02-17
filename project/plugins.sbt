resolvers ++= Seq(
  Classpaths.sbtPluginSnapshots,
  Classpaths.sbtPluginReleases,
  Resolver.sonatypeRepo("snapshots"),
  "Twitter Maven" at "https://maven.twttr.com"
)

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "4.13.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")