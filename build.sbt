
lazy val root = (project in file(".")).
  settings(
    organization := "flowly",
    name := "flowly",
    inThisBuild(List(
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    ))
  )
  .aggregate(`flowly-core`, `flowly-mongodb`)

lazy val `flowly-core` = project
  .settings(
    organization := "flowly",
    name := "flowly-core",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "4.3.4" % "test",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
      "org.typelevel" %% "cats-core" % "1.3.1"
    )
  )

lazy val `flowly-mongodb` = project.
  settings(
    organization := "flowly",
    name := "flowly-mongodb",
    libraryDependencies ++= Seq(
      "org.mongojack" % "mongojack" % "2.9.4"
    )
  )
  .dependsOn(`flowly-core`)

scalacOptions in Test ++= Seq("-Yrangepos")
