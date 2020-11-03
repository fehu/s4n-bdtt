package com.github.fehu.s4nbdtt.app

import cats.effect.{ ExitCode, IO, IOApp, Resource }

import com.github.fehu.s4nbdtt.emul.DroneCtrlNoOp
import com.github.fehu.s4nbdtt.{ DroneApp, DroneCtrl, DroneState }

object NoOpIOAppExample extends IOApp {
  val app = new DroneApp[IO, Int] {
    def initialState: DroneState[Int] = defaultDroneInitialStateInt

    def droneCtrl(name: String): Resource[IO, DroneCtrl[IO]] =
      Resource.liftF(IO.pure { new DroneCtrlNoOp[IO] })
  }

  def run(args: List[String]): IO[ExitCode] = app.runApp.as(ExitCode.Success)
}
