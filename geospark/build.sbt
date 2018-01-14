import Dependencies._

lazy val root = (project in file(".")).
 settings(
   inThisBuild(List(
     scalaVersion := "2.11.8",
     version      := "0.1.0"
   )),
   name := "ProcessSims",
   parallelExecution in Test := false,
   coverageFailOnMinimum := true,
   coverageHighlighting := true,
   coverageMinimum := 70,
   publishArtifact in Test := false,
   libraryDependencies ++= Seq(
     "org.apache.spark" %% "spark-core" % "2.1.0",
     "org.apache.spark" %% "spark-sql" % "2.1.0",
     "com.vividsolutions" % "jts" % "1.13",
     "org.datasyslab" % "geospark" % "1.0.1",
     scalaTest % Test
   )
 )
