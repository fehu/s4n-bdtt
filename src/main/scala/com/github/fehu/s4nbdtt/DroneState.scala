package com.github.fehu.s4nbdtt

final case class DroneState[N](position: Grid.Position[N], orientation: Direction) {

  /** Uses [[Grid.Position.unsafeMove]]. Therefore the movements should be ''pre-validated''. */
  def moveForward(implicit num: Numeric[N]): DroneState[N] = copy(position = position.unsafeMove(orientation))

  def rotateLeft: DroneState[N] = copy(orientation = orientation.rotateLeft)
  def rotateRight: DroneState[N] = copy(orientation = orientation.rotateRight)
}

object DroneState {
  /** Uses [[Grid.Position.unsafe]]. Therefore the position should be ''pre-validated''. */
  def apply[N](x: N, y: N, orientation: Direction): DroneState[N] = DroneState(Grid.Position.unsafe(x, y), orientation)
}
