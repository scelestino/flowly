import sbt.Keys._
import sbt._

object CommonSettings {

  val settings: Seq[Def.Setting[_]] =
    Seq(organization := "com.despegar.flowly",
        publishTo := Some("Nexus Despegar" at s"http://nexus.despegar.it/nexus/content/repositories/${if (isSnapshot.value) "snapshots" else "releases"}"),
        resolvers += Opts.resolver.mavenLocalFile,
        resolvers += Resolver.mavenLocal,
        resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
        resolvers += "Nexus Public Repository" at "http://vmtilcara.servers.despegar.it:8080/nexus/content/groups/public",
        resolvers += "Nexus Snapshots Repository" at "http://vmtilcara.servers.despegar.it:8080/nexus/content/repositories/snapshots",
        resolvers += "Nexus Proxies Repository" at "http://vmtilcara.servers.despegar.it:8080/nexus/content/groups/proxies",
        scalaVersion := "2.12.8")
}