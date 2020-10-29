package com.github.fehu.s4nbdtt.emul

import cats.Applicative

import com.github.fehu.s4nbdtt.DroneCtrl

class DroneCtrlNoOp[F[_]](val name: String)(implicit A: Applicative[F]) extends DroneCtrl[F] {
  def moveForward: F[Unit] = A.unit
  def rotateLeft: F[Unit] = A.unit
  def rotateRight: F[Unit] = A.unit
  def deliver: F[Unit] = A.unit
}
