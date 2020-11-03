import sbt._

object Dependencies {
  lazy val `cats-core`              = "org.typelevel"         %% "cats-core"                     % Version.cats
  lazy val `cats-effect`            = "org.typelevel"         %% "cats-effect"                   % Version.catsEffect
  lazy val `cats-effect-scala-test` = "com.codecommit"        %% "cats-effect-testing-scalatest" % Version.catsEffectTest
  lazy val `log4cats-slf4j`         = "io.chrisdavenport"     %% "log4cats-slf4j"                % Version.log4cats
  lazy val `logback-classic`        = "ch.qos.logback"         % "logback-classic"               % Version.logback
  lazy val  pureconfig              = "com.github.pureconfig" %% "pureconfig"                    % Version.pureconfig
  lazy val `refined-pureconfig`     = "eu.timepit"            %% "refined-pureconfig"            % Version.refined
  lazy val `refined-scala-check`    = "eu.timepit"            %% "refined-scalacheck"            % Version.refined
  lazy val `scala-check`            = "org.scalacheck"        %% "scalacheck"                    % Version.scalaCheck
  lazy val `scala-test`             = "org.scalatest"         %% "scalatest"                     % Version.scalaTest
  lazy val `scala-test-check`       = "org.scalatestplus"     %% "scalacheck-1-14"               % Version.scalaTestCheck
  lazy val  spire                   = "org.typelevel"         %% "spire"                         % Version.spire

  object Plugin {
    lazy val `kind-projector`     = "org.typelevel" %% "kind-projector"     % Version.kindProjector cross CrossVersion.full
    lazy val `better-monadic-for` = "com.olegpy"    %% "better-monadic-for" % Version.betterMonadicFor
  }

  // Graal / Provided
  lazy val svm = "org.graalvm.nativeimage" % "svm" % Version.graal % Provided

  protected object Version {
    lazy val betterMonadicFor = "0.3.1"
    lazy val cats             = "2.2.0"
    lazy val catsEffect       = "2.2.0"
    lazy val catsEffectTest   = "0.4.1"
    lazy val graal            = "20.2.0"
    lazy val kindProjector    = "0.11.0"
    lazy val log4cats         = "1.1.1"
    lazy val logback          = "1.2.3"
    lazy val pureconfig       = "0.14.0"
    lazy val refined          = "0.9.17"
    lazy val scalaCheck       = "1.14.3"
    lazy val scalaTest        = "3.2.2"
    lazy val scalaTestCheck   = "3.2.2.0"
    lazy val spire            = "0.17.0"
  }
}
