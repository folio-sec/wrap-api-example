val scala3Version = "3.3.5"

lazy val root = project
  .in(file("."))
  .settings(
    name := "folio-coding-interview",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % Test
  )
