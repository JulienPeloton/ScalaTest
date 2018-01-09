import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "toto",
      scalaVersion := "2.12.4",
      version      := "0.1.0"
    )),
    name := "MyProject",
    parallelExecution in Test := false,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    coverageMinimum := 70,
    publishArtifact in Test := false,
    libraryDependencies += scalaTest % Test
  )
