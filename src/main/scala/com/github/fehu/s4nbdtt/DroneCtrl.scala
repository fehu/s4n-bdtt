package com.github.fehu.s4nbdtt

import cats.effect.Resource

/** Blocking drone control interface. */
trait DroneCtrl[F[_]] {
  def name: String

  def moveForward: F[Unit]
  def rotateLeft: F[Unit]
  def rotateRight: F[Unit]
  def deliver: F[Unit]
}

object DroneCtrl {
  trait Pool[F[_]] {
    def control(name: String): Resource[F, DroneCtrl[F]]
  }
}
