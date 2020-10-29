package com.github.fehu.s4nbdtt.app

import scala.concurrent.duration._

import cats.effect.{ ExitCode, IO, IOApp, Resource }

import com.github.fehu.s4nbdtt.emul.DroneCtrlSleepRndEmulator
import com.github.fehu.s4nbdtt.{ DroneApp, DroneCtrl, DroneState }

object DroneEmulatorIOAppExample extends IOApp {
  val app = new DroneApp[IO, Int] {
    def initialState: DroneState[Int] = defaultDroneInitialStateInt

    def droneCtrl(name: String): Resource[IO, DroneCtrl[IO]] =
      Resource.make(newCtrl(name))(_.returnToBase)

    private def newCtrl(name: String) = DroneCtrlSleepRndEmulator
      .gaussian[IO](
        name = name,
        moveMean   = 2.seconds,
        moveStdDev = 500.millis,
        rotateMean   = 500.millis,
        rotateStdDev = 100.millis,
        deliverMean   = 5.seconds,
        deliverStdDev = 1.second,
        returnMean   = 10.seconds,
        returnStdDev = 5.seconds
      )
  }

  def run(args: List[String]): IO[ExitCode] = app.runApp.as(ExitCode.Success)
}
