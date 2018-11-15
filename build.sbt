ThisBuild / scalaVersion := "2.11.12"
ThisBuild / organization := "net.sanori.spark"
ThisBuild / organizationName := "SanoriNet"
ThisBuild / organizationHomepage := Some(url("http://sanori.github.io/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/sanori/spark-access-log"),
    "scm:git@github.com:sanori/spark-access-log.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "sanori",
    name = "Joo-Won Jung",
    email = "sanori@sanori.net",
    url = url("https://sanori.github.io/")
  )
)
ThisBuild / licenses := Seq(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / homepage := Some(url("https://github.com/sanori/spark-access-log"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

lazy val root = (project in file("."))
  .settings(
    name := "access-log",
    version := "0.1.0b-SNAPSHOT",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.2",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.2",
    libraryDependencies += "org.apache.spark" %% "spark-hive" % "2.3.2",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

// fork in Test := true
// javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")
// parallelExecution in Test := false

credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credential")

useGpg := true
// workaround for sbt/sbt-pgp#126
pgpSecretRing := pgpPublicRing.value
// To specify signing key
usePgpKeyHex("10CB79CA9DDE74FD")
