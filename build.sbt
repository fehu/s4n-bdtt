import Dependencies._

ThisBuild / organization := "com.github.fehu"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version      := "0.1.0-SNAPSHOT"

addCommandAlias("fullDependencyUpdates", ";dependencyUpdates; reload plugins; dependencyUpdates; reload return")

inThisBuild(Seq(
  addCompilerPlugin(Plugin.`better-monadic-for`),
  addCompilerPlugin(Plugin.`kind-projector`)
))

lazy val root = (project in file("."))
  .settings(
    name := "s4n-bdtt",
    description := "S4N backend dev technical test",
    libraryDependencies ++= Seq(
      `cats-core`,
      `cats-effect`,
       pureconfig,
      `refined-pureconfig`,
      `scala-test` % Test
    )
  )
