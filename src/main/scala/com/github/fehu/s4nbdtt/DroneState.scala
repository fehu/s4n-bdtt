package com.github.fehu.s4nbdtt

final case class DroneState[N](position: Position[N], orientation: Direction) {
  def moveForward(implicit num: Numeric[N]): DroneState[N] = copy(position = position.move(orientation))

  def rotateLeft: DroneState[N] = copy(orientation = orientation.rotateLeft)
  def rotateRight: DroneState[N] = copy(orientation = orientation.rotateRight)
}

object DroneState {
  def apply[N](x: N, y: N, orientation: Direction): DroneState[N] = DroneState(Position(x, y), orientation)
}
