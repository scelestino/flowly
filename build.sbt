lazy val root = (project in file("."))
  .settings(publishArtifact := false)
  .settings(
    name := "flowly",
  )
  .aggregate(`flowly-core`, `flowly-mongodb`)

lazy val `flowly-core` = project
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "flowly-core",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "4.3.4" % "test",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8"
    )
  )

lazy val `flowly-mongodb` = project
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "flowly-mongodb",
    libraryDependencies ++= Seq(
      "org.mongojack" % "mongojack" % "2.9.4"
    )
  )
  .dependsOn(`flowly-core`)

scalacOptions in Test ++= Seq("-Yrangepos")