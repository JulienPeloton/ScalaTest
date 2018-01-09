import Dependencies._

lazy val root = (project in file(".")).
 settings(
   inThisBuild(List(
     scalaVersion := "2.11.8",
     version      := "0.1.0"
   )),
   name := "sparkdev",
   libraryDependencies += "org.apache.spark" %% "spark-core" % "2.1.0",
   libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.1.0",
   libraryDependencies += scalaTest % Test
 )
