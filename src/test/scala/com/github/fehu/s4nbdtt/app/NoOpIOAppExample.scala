package com.github.fehu.s4nbdtt.app

import cats.effect.{ ExitCode, IO, IOApp }

import com.github.fehu.s4nbdtt.emul.DroneCtrlNoOp
import com.github.fehu.s4nbdtt.{ DroneApp, DroneCtrl, DroneState }

object NoOpIOAppExample extends IOApp {
  val app = new DroneApp[IO, Int] {
    def initialState: DroneState[Int] = defaultDroneInitialStateInt

    def droneCtrl(name: String): IO[DroneCtrl[IO]] = IO.pure { new DroneCtrlNoOp }
  }

  def run(args: List[String]): IO[ExitCode] = app.runApp.as(ExitCode.Success)
}
