package com.github.fehu.s4nbdtt

import scala.jdk.CollectionConverters._

import cats.arrow.Arrow
import cats.data.NonEmptyList
import cats.syntax.validated._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import com.github.fehu.s4nbdtt.Arbitraries._

class ProgParseSpec extends AnyWordSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  import ProgParseSpec._
  import RouteParseSpec._

  implicit lazy val arbitraryDroneRoute = Arbitrary { genDroneRoute(1, 100) }

  "DroneProgParser" should {
    "correctly parse valid program (sequence of routes)" in
      forAll { implicit cfg: Config.Drone =>
        val parser = new DroneProgParser(cfg)
        forAll { prog: DroneProg =>
          val progStr = stringifyDroneProg(prog, cfg.commands)
          parser.parse(progStr) shouldBe prog.valid
        }
      }

    "report error if invalid commands are present in route definitions" in
      forAll { cfg: Config.Drone =>
        val parser = new DroneProgParser(cfg)
        forAll(genInvalidDroneProgString(cfg, 5)) { case (progStr, errors0) =>
          val errors = errors0.map{ case (i, s, cs) =>
            DroneProgDefinitionError.InvalidCommands(i, s, cs.map(DroneProgDefinitionError.InvalidCommand(_)))
          }
          parser.parse(progStr) shouldBe errors.invalid
        }
      }

    "report error if there are no routes defined" in
      forAll { cfg: Config.Drone =>
        val parser = new DroneProgParser(cfg)
        forAll(genEmptyProgString) { progStr =>
          parser.parse(progStr) shouldBe DroneProgDefinitionError.NoRoutes.invalidNel
        }
      }

    "report error if the number of defined routes exceeds drone's capacity" in
      forAll { cfg: Config.Drone =>
        val parser = new DroneProgParser(cfg)
        val cap1 = cfg.capacity.value + 1
        forAll(AGens.genDroneProg(minRoutes = cap1, maxRoutes = cap1 * 10)) { case (prog, n) =>
          val progStr = stringifyDroneProg(prog, cfg.commands)
          parser.parse(progStr) shouldBe DroneProgDefinitionError.TooManyRoutes(n).invalidNel
        }
      }

  }
}

object ProgParseSpec {
  import RouteParseSpec._

  def stringifyDroneProg(prog: DroneProg, cfg: Config.Drone.Commands): String =
    prog.routes
      .map(stringifyDroneRoute(_, cfg).value)
      .toList
      .mkString("\n")

  def genInvalidDroneProgString(cfg: Config.Drone, maxInvalidCount: Int): Gen[(String, NonEmptyList[(Int, String, NonEmptyList[Char])])] =
    for {
      n  <- Gen.chooseNum(1, cfg.capacity.value)
      bs <- Gen.listOfN(n, arbitrary[Boolean]).suchThat(_.exists(!_))
      routeGens = bs.map {
                    if (_)
                      genDroneRoute(1, 5).map(stringifyDroneRoute(_, cfg.commands).value -> None)
                    else
                      genInvalidDroneRouteString(cfg.commands, maxInvalidCount)
                        .map(Arrow[Function1].split(_.value * 2, Some(_)))
                  }
      routes0 <- Gen.sequence(routeGens)
      (routes, errors0) = routes0.asScala.zipWithIndex.map {
                            case ((raw, None), _)          => raw -> None
                            case ((raw, Some(invalid)), i) => raw -> Some((i, raw, invalid))
                          }.unzip
      errors = NonEmptyList.fromListUnsafe(errors0.flatten.toList)
    } yield (routes.mkString("\n"), errors)

  def genEmptyProgString: Gen[String] = Gen.chooseNum(0, 10).map("\n" * _)

}
