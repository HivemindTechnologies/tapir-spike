ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "Tapir Spike",
    libraryDependencies ++= {
      val tapirVersion = "0.18.0-M4"
      Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-core"               % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % tapirVersion
      )
    }
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
