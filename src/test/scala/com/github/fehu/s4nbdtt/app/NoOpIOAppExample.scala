package com.github.fehu.s4nbdtt.app

import cats.effect.{ ExitCode, IO, IOApp }

import com.github.fehu.s4nbdtt.emul.{ DroneCtrlNoOp, DronesPool }
import com.github.fehu.s4nbdtt.{ DroneApp, DroneCtrl, DroneState }

object NoOpIOAppExample extends IOApp {
  // The value is hardcoded to denote that it is not application property but rather external restriction
  val hardcodedDrones = 20

  val app = new DroneApp[IO, Int] {
    def initialState: DroneState[Int] = defaultDroneInitialStateInt

    def dronesPool: IO[DroneCtrl.Pool[IO]] =
      DronesPool(hardcodedDrones)(
        name => IO.pure { new DroneCtrlNoOp[IO](name) }
      )(
        _ => IO.unit
      )
  }

  def run(args: List[String]): IO[ExitCode] = app.runApp.as(ExitCode.Success)
}
