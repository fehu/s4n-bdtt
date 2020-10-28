package com.github.fehu.s4nbdtt

/** Blocking drone control interface. */
trait DroneCtrl[F[_]] {
  def moveForward: F[Unit]
  def rotateLeft: F[Unit]
  def rotateRight: F[Unit]
  def deliver: F[Unit]
}
