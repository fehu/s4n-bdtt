import sbt._

object Dependencies {
  lazy val `cats-core`          = "org.typelevel"         %% "cats-core"          % Version.cats
  lazy val `cats-effect`        = "org.typelevel"         %% "cats-effect"        % Version.catsEffect
  lazy val  pureconfig          = "com.github.pureconfig" %% "pureconfig"         % Version.pureconfig
  lazy val `refined-pureconfig` = "eu.timepit"            %% "refined-pureconfig" % Version.refined
  lazy val `scala-test`         = "org.scalatest"         %% "scalatest"          % Version.scalaTest

  object Plugin {
    lazy val `kind-projector`     = "org.typelevel" %% "kind-projector"     % Version.kindProjector cross CrossVersion.full
    lazy val `better-monadic-for` = "com.olegpy"    %% "better-monadic-for" % Version.betterMonadicFor
  }

  protected object Version {
    lazy val betterMonadicFor = "0.3.1"
    lazy val cats             = "2.2.0"
    lazy val catsEffect       = "2.2.0"
    lazy val kindProjector    = "0.11.0"
    lazy val pureconfig       = "0.14.0"
    lazy val refined          = "0.9.17"
    lazy val scalaTest        = "3.1.1"
  }
}
