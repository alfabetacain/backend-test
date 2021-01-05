import Dependencies._

ThisBuild / scalaVersion := "2.12.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dk.alfabetacain"
ThisBuild / organizationName := "example"

val finchVersion = "0.31.0"
val finagleVersion = "20.12.0"

lazy val contract = (project in file("contract"))
  .settings(
    name := "contract",
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  ).enablePlugins(AkkaGrpcPlugin)

lazy val primeNumberService = (project in file("prime-number-service"))
  .settings(
    name := "prime-number-service",
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.14.1" % Test,
      scalaTest % Test
    )
  ).dependsOn(contract).enablePlugins(AkkaGrpcPlugin)

lazy val proxyService = (project in file("proxy-service"))
  .settings(
    name := "proxy-service",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % "2.5.31",
      scalaTest % Test
    )
  ).dependsOn(contract).enablePlugins(AkkaGrpcPlugin)

lazy val root = (project in file("."))
  .settings(
    name := "backend-test",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "com.github.finagle" %% "finch-core" % finchVersion,
      "com.github.finagle" %% "finch-circe" % finchVersion,
      "io.circe" %% "circe-generic" % "0.9.0"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
