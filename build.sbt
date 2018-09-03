import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "flowly-core",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"      % "3.0.5",
      "org.json4s"    %% "json4s-native"  % "3.6.0",
      "org.json4s"    %% "json4s-scalaz"  % "3.6.0"
    )
  )
