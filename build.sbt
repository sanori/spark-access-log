ThisBuild / scalaVersion := "2.11.12"
ThisBuild / organization := "net.sanori"

lazy val root = (project in file("."))
  .settings(
    name := "accessLog",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.2",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.2",
    libraryDependencies += "org.apache.spark" %% "spark-hive" % "2.3.2",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

// fork in Test := true
// javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")
// parallelExecution in Test := false