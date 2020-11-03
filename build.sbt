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
      `log4cats-slf4j`,
      `logback-classic`,
       pureconfig,
      `refined-pureconfig`,
       spire,
      `cats-effect-scala-test` % Test,
      `refined-scala-check` % Test,
      `scala-check` % Test,
      `scala-test` % Test,
      `scala-test-check` % Test
    ),
    mainClass in Compile := Some("com.github.fehu.s4nbdtt.app.DroneEmulatorIOAppExample"),
    // TODO: make separate `run` task
    fork in (Compile, run) := true,
    javaHome in (Compile, run) := Some(file(sys.env.getOrElse("GRAAL_HOME", ""))),
    javaOptions in (Compile, run) += s"-agentlib:native-image-agent=config-output-dir=${ baseDirectory.value / "graal" }"
  )

enablePlugins(NativeImagePlugin)

nativeImageVersion := "20.2.0"

lazy val initializeAtRunTime = Seq(
  "org.slf4j.impl.StaticLoggerBinder"
)

nativeImageOptions ++= Seq(
  "--no-fallback",
  "--initialize-at-build-time",
  "--initialize-at-run-time=" + initializeAtRunTime.mkString(","),
  s"-H:DynamicProxyConfigurationFiles=${ baseDirectory.value / "graal" / "proxy-config.json" }",
  s"-H:JNIConfigurationFiles=${ baseDirectory.value / "graal" / "jni-config.json" }",
  s"-H:ReflectionConfigurationFiles=${ baseDirectory.value / "graal" / "reflect-config.json" }",
  s"-H:ResourceConfigurationFiles=${ baseDirectory.value / "graal" / "resource-config.json" }",
  "--allow-incomplete-classpath",
  "--report-unsupported-elements-at-runtime",
  // dev
  "-H:+ReportExceptionStackTraces",
  // This flag greatly helps to configure the image build to work as intended;
  // the goal is to have as many classes initialized at build time and yet keep the correct semantics of the program.
  // [[https://github.com/oracle/graal/blob/master/substratevm/CLASS-INITIALIZATION.md]]
  "-H:+PrintClassInitialization"
)
