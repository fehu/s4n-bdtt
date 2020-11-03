package com.github.fehu.s4nbdtt

package object app {

  lazy val defaultDroneInitialStateInt: DroneState[Int] = DroneState(0, 0, Direction.North)

}
