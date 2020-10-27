import sbt._

object Dependencies {
  lazy val `cats-core`   = "org.typelevel" %% "cats-core"   % Version.cats
  lazy val `cats-effect` = "org.typelevel" %% "cats-effect" % Version.catsEffect
  lazy val `scala-test`  = "org.scalatest" %% "scalatest"   % Version.scalaTest

  object Plugin {
    lazy val `kind-projector`     = "org.typelevel" %% "kind-projector"     % Version.kindProjector cross CrossVersion.full
    lazy val `better-monadic-for` = "com.olegpy"    %% "better-monadic-for" % Version.betterMonadicFor
  }

  protected object Version {
    lazy val betterMonadicFor = "0.3.1"
    lazy val cats             = "2.2.0"
    lazy val catsEffect       = "2.2.0"
    lazy val kindProjector    = "0.11.0"
    lazy val scalaTest        = "3.2.2"
  }
}
