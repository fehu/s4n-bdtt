package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.scalacheck.numeric._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalacheck.Arbitrary.arbitrary

object Arbitraries {

  implicit lazy val arbitraryDroneCommands: Arbitrary[Config.Drone.Commands] = Arbitrary {
    for {
      forward  <- Gen.alphaUpperChar
      rotLeft  <- Gen.alphaUpperChar.suchThat(_ != forward)
      rotRight <- Gen.alphaUpperChar.suchThat(c => c != forward && c != rotLeft)
    } yield Config.Drone.Commands(forward, rotLeft, rotRight)
  }

  implicit lazy val arbitraryDroneConfig: Arbitrary[Config.Drone] = Arbitrary {
    for {
      capacity <- arbitrary[Int Refined Positive].suchThat(_.value < 10)
      commands <- arbitrary[Config.Drone.Commands]
    } yield Config.Drone(capacity, commands)
  }

  implicit lazy val arbitraryDroneMv: Arbitrary[DroneMv] = Arbitrary {
    Gen.oneOf(DroneMv.MoveForward, DroneMv.RotateLeft, DroneMv.RotateRight)
  }

  implicit def arbitraryDroneProg(implicit aRoute: Arbitrary[DroneRoute], cfg: Config.Drone): Arbitrary[DroneProg] =
    Arbitrary {
      AGens.genDroneProg(1, cfg.capacity.value).map(_._1)
    }

  object AGens {
    def genDroneProg(minRoutes: Int, maxRoutes: Int)(implicit aRoute: Arbitrary[DroneRoute]): Gen[(DroneProg, Int)] =
      for {
        n      <- Gen.chooseNum(minRoutes, maxRoutes)
        routes <- Gen.listOfN(n, arbitrary[DroneRoute])
      } yield DroneProg(NonEmptyList.fromListUnsafe(routes)) -> n
  }
}
