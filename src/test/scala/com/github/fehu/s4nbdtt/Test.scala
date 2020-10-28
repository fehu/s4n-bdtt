package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList
import cats.effect.{ ExitCode, IO, IOApp }

import com.github.fehu.s4nbdtt.test.DroneCtrlNoOp

// TODO: temp
object Test extends IOApp {
  import DroneMv._

  val ctrl = new DroneCtrlNoOp[IO]
  val state0 = DroneProgExecutor.State(0, 0, Direction.North)
  val executor = new DroneProgExecutor[IO](ctrl, state0)

  val route1 = DroneRoute(
    NonEmptyList.of(
      RotateLeft,
      MoveForward,
      MoveForward
    )
  )
  // (0, 0, N)
  // (0, 0, W)
  // (-1, 0, W)
  // (-2, 0, W)
  // s01: State(-2,0,West)

  val route2 = DroneRoute(
    NonEmptyList.of(
      RotateRight,
      RotateRight,
      MoveForward
    )
  )
  // (0, 0, N)
  // (0, 0, E)
  // (0, 0, S)
  // (0, -1, S)
  // s02: State(0,-1,South)

  val route3 = DroneRoute(
    NonEmptyList.of(
      RotateRight,
      MoveForward,
      MoveForward,
      RotateRight,
      MoveForward
    )
  )
  // (0, 0, N)
  // (0, 0, E)
  // (1, 0, E)
  // (2, 0, E)
  // (2, 0, S)
  // (2, -1, S)
  // s03: State(2,-1,South)

  val prog1 = DroneProg(
    NonEmptyList.of(
      route1,
      route2,
      route3
    )
  )
  // (0, 0, N)
  // (0, 0, W)
  // (-1, 0, W)
  //!(-2, 0, W)
  //!State(-2,0,West)
  // (-2, 0, N)
  // (-2, 0, E)
  //!(-1, 0, E)
  //!State(-1,0,East)
  // (-1, 0, S)
  // (-1, -1, S)
  // (-1, -2, S)
  // (-1, -2, W)
  //!(-2, -2, W)
  //!State(-2,-2,West)
  // s1: NonEmptyList(
  //  State(-2,0,West),
  //  State(-1,0,East),
  //  State(-2,-2,West)
  // )

  def run(args: List[String]): IO[ExitCode] =
    for {
      s01 <- executor.exec(state0, route1)
      _    = println(s"s01: $s01")
      s02 <- executor.exec(state0, route2)
      _    = println(s"s02: $s02")
      s03 <- executor.exec(state0, route3)
      _    = println(s"s03: $s03")
      s1  <- executor.exec(prog1)
      _    = println(s"s1: $s1")
    } yield ExitCode.Success
}
