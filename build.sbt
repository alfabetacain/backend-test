import Dependencies._

ThisBuild / scalaVersion     := "2.12.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val finchVersion = "0.22.0"

lazy val root = (project in file("."))
  .settings(
    name := "backend-test",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "com.github.finagle" %% "finch-core" % finchVersion,
      "com.github.finagle" %% "finch-circe" %  finchVersion,
      "io.circe" %% "circe-generic" % "0.9.0"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
