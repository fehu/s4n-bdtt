package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec

class DroneProgValidatorSimpleTest extends AnyWordSpec with TableDrivenPropertyChecks with Matchers {
  import Direction._
  import DroneMv._

  val grid = new SymmetricZeroCenteredGrid[Int](3, 3)
  val validator = new DroneProgValidator(grid)

  val validState0 = DroneState(0, 0, North)
  val validRoute1 = DroneRoute(NonEmptyList.fromListUnsafe(List.fill(3)(MoveForward)))
  val validRoute2 = DroneRoute(NonEmptyList(RotateLeft, List.fill(2)(MoveForward)))

  val validProg = DroneProg(NonEmptyList.of(validRoute1, validRoute2))
  val expected  = NonEmptyList.of(DroneState(0, 3, North), DroneState(-2, 3, West))

  val invalidRoute1 = DroneRoute(NonEmptyList.fromListUnsafe(List.fill(4)(MoveForward)))
  val invalidRoute2 = DroneRoute(NonEmptyList(RotateLeft, List.fill(5)(MoveForward)))

  val invalidProg1 = DroneProg(NonEmptyList.of(invalidRoute1))
  val error1       = Grid.InvalidMove(grid, Grid.Position.unsafe(0, 3), North, Grid.Position.unsafe(0, 4))
  val invalidProg2 = DroneProg(NonEmptyList.of(invalidRoute2))
  val error2       = Grid.InvalidMove(grid, Grid.Position.unsafe(-3, 0), West, Grid.Position.unsafe(-4, 0))
  val invalidProg3 = DroneProg(NonEmptyList.of(validRoute1, invalidRoute2))
  val error3       = Grid.InvalidMove(grid, Grid.Position.unsafe(-3, 3), West, Grid.Position.unsafe(-4, 3))

  val invalidState0 = DroneState(4, 4, North)

  "DroneProgValidator" should {
    "validate a program that doesn't take the drone out of grid" in {
      validator.validate(validState0, validProg) shouldBe Right(expected)
    }

    "report errors on validation of a programs that take the drone out of grid" in
      forAll(
        Table(
          ("Program",   "Error"),
          (invalidProg1, error1),
          (invalidProg2, error2),
          (invalidProg3, error3)
        )
      ) { (prog, error) =>
        validator.validate(validState0, prog) shouldBe Left(error)
      }

    "report error on invalid initial drone position" in {
      validator.validate(invalidState0, validProg) shouldBe Left(Grid.InvalidPosition(grid, invalidState0.position))
    }
  }

}
