import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "toto",
      scalaVersion := "2.12.4",
      version      := "0.1.0",
      parallelExecution in Test := false,
      coverageFailOnMinimum := false,
      coverageHighlighting := true,
      coverageMinimum := 70
    )),
    name := "MyProject",
    libraryDependencies += scalaTest % Test
  )
