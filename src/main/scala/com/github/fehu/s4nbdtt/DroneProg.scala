package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList

final case class DroneProg(routes: NonEmptyList[DroneRoute]) extends AnyVal

final case class DroneRoute(moves: NonEmptyList[DroneMv]) extends AnyVal

sealed trait DroneMv
object DroneMv {
  case object MoveForward extends DroneMv
  case object RotateLeft  extends DroneMv
  case object RotateRight extends DroneMv
}

