import Dependencies._

lazy val root = (project in file(".")).
 settings(
   inThisBuild(List(
     scalaVersion := "2.11.8",
     version      := "0.1.0"
   )),
   name := "healpix",
   parallelExecution in Test := false,
   coverageFailOnMinimum := true,
   coverageHighlighting := true,
   coverageMinimum := 70,
   publishArtifact in Test := false,
   // fork in run := true,
   // javaOptions += "-Djava.library.path=/Users/julien/Documents/workspace/otrepos/jep/build/lib.macosx-10.7-x86_64-3.6/",
   // javaOptions += "-Djava.library.path=./lib",
   libraryDependencies ++= Seq(
     "org.apache.spark" %% "spark-core" % "2.1.0",
     scalaTest % Test
   )
 )
