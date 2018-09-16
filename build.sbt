
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "flowly-core",
    libraryDependencies ++= Seq(
//      "org.scalatest" %% "scalatest"      % "3.0.5",
      "org.specs2" %% "specs2-core" % "4.3.4" % "test",
      "org.json4s"    %% "json4s-native"  % "3.6.0",
      "org.json4s"    %% "json4s-scalaz"  % "3.6.0",
      "org.typelevel" %% "cats-core" % "1.3.1"
    )
  )

scalacOptions in Test ++= Seq("-Yrangepos")
