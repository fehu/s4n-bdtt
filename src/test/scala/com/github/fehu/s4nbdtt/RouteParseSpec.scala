package com.github.fehu.s4nbdtt

import scala.jdk.CollectionConverters._

import cats.data.NonEmptyList
import cats.syntax.validated._
import eu.timepit.refined.refineV
import eu.timepit.refined.collection.NonEmpty
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import Arbitraries._

class RouteParseSpec extends AnyWordSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  import RouteParseSpec._

  implicit lazy val arbitraryDroneRoute = Arbitrary { genDroneRoute(1, 100) }

  "DroneRouteParser" should {
    "correctly parse valid route" in
      forAll { cfg: Config.Drone.Commands =>
        val parser = new DroneRouteParser(cfg)
        forAll { route: DroneRoute =>
          val routeStr = stringifyDroneRoute(route, cfg)
          parser.parse(routeStr) shouldBe route.valid
        }
      }

    "report error if invalid commands are present in route definition" in
      forAll { cfg: Config.Drone.Commands =>
        val parser = new DroneRouteParser(cfg)
        forAll(genInvalidDroneRouteString(cfg, 5)) { case (routeStr, invalid) =>
          parser.parse(routeStr) shouldBe invalid.map(DroneProgDefinitionError.InvalidCommand(_)).invalid
        }
      }
  }
}

object RouteParseSpec {
  def genDroneRoute(min: Int, max: Int): Gen[DroneRoute] =
    for {
      n    <- Gen.chooseNum(min, max)
      cmds <- Gen.listOfN(n, arbitrary[DroneMv])
    } yield DroneRoute(NonEmptyList.fromListUnsafe(cmds))

  def droneMvToChar(mv: DroneMv, cfg: Config.Drone.Commands): Char = mv match {
    case DroneMv.MoveForward => cfg.moveForward
    case DroneMv.RotateLeft  => cfg.rotateLeft
    case DroneMv.RotateRight => cfg.rotateRight
  }

  def stringifyDroneRoute(route: DroneRoute, cfg: Config.Drone.Commands): DroneRouteParser.Raw =
    refineV[NonEmpty].unsafeFrom( // `route.moves` is a NEL => the string is never empty
      route.moves.toList.map(droneMvToChar(_, cfg)).mkString
    )

  def genInvalidDroneRouteString(cfg: Config.Drone.Commands, maxInvalidCount: Int): Gen[(DroneRouteParser.Raw, NonEmptyList[Char])] = {
    val validCmds = List(cfg.moveForward, cfg.rotateLeft, cfg.rotateRight)
    val validCmdsGen = Gen.someOf(validCmds)
    val invalidCmdGen = Gen.alphaNumChar.suchThat(!validCmds.contains(_))
    for {
      invalidCmds0  <- Gen.listOfN(maxInvalidCount, invalidCmdGen)
      invalidCmds    = invalidCmds0.distinct
      invalidCmdsNel = NonEmptyList.fromListUnsafe(invalidCmds)
      cmdsGens       = invalidCmds.map(c => validCmdsGen.map(_ :+ c))
      cmds0         <- Gen.sequence(cmdsGens)
      cmds           = cmds0.asScala.flatten.mkString
    } yield (refineV[NonEmpty].unsafeFrom(cmds), invalidCmdsNel)

  }

}
