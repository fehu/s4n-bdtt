package com.github.fehu.s4nbdtt.emul

import cats.effect.{ Concurrent, Resource, Sync }
import cats.effect.concurrent.Semaphore
import cats.syntax.apply._
import cats.syntax.functor._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import com.github.fehu.s4nbdtt.DroneCtrl

class DronesPool[F[_]: Sync, Ctrl <: DroneCtrl[F]](
  idle: Semaphore[F],
  acquireCtrl: String => F[Ctrl],
  releaseCtrl: Ctrl => F[Unit]
) extends DroneCtrl.Pool[F] {

  def control(name: String): Resource[F, Ctrl] =
    Resource.make(
      idle.acquire *> acquireCtrl(name) <* logger.debug(s"""Acquired drone control "$name"""")
    )(
      releaseCtrl(_) *> idle.release *> logger.debug(s"""Released drone control "$name"""")
    )

  private val logger = Slf4jLogger.getLogger[F]
}

object DronesPool {
  def apply[F[_]: Concurrent, Ctrl <: DroneCtrl[F]](
    drones: Int
  )(
    acquire: String => F[Ctrl]
  )(
    release: Ctrl => F[Unit]
  ): F[DronesPool[F, Ctrl]] =
    Semaphore[F](drones).map(
      new DronesPool(_, acquire, release)
    )
}
