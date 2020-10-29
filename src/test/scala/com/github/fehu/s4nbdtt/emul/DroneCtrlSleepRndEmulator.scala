package com.github.fehu.s4nbdtt.emul

import scala.concurrent.duration._

import cats.effect.{ Concurrent, Sync, Timer }
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import spire.random.rng.Cmwc5
import spire.random.{ Dist, Gaussian, Generator }

import com.github.fehu.s4nbdtt.DroneCtrl

/** Emulates drone control by sleeping random time. */
class DroneCtrlSleepRndEmulator[F[_]: Sync: Timer](
  name: String,
  moveTimeRnd: Rnd[F, FiniteDuration],
  rotateTimeRnd: Rnd[F, FiniteDuration],
  deliverTimeRnd: Rnd[F, FiniteDuration]
) extends DroneCtrl[F] {

  def moveForward: F[Unit] = sleepRndLog("moving forward", moveTimeRnd)
  def rotateLeft: F[Unit]  = sleepRndLog("rotating left",  rotateTimeRnd)
  def rotateRight: F[Unit] = sleepRndLog("rotating right", rotateTimeRnd)
  def deliver: F[Unit]     = sleepRndLog("delivery",       deliverTimeRnd)

  private def sleepRnd(rnd: Rnd[F, FiniteDuration]): F[Unit] =
    rnd.gen flatMap Timer[F].sleep

  private def sleepRndLog(op: String, rnd: Rnd[F, FiniteDuration]): F[Unit] =
    logger.debug(s"Start $op") *> sleepRnd(rnd) <* logger.debug(s"Done $op")

  private val logger = Slf4jLogger.getLoggerFromName[F](s"DroneEmulator($name)")
}

object DroneCtrlSleepRndEmulator {
  def gaussian[F[_]: Concurrent: Timer](
    name: String,
    moveMean: FiniteDuration,
    moveStdDev: FiniteDuration,
    rotateMean: FiniteDuration,
    rotateStdDev: FiniteDuration,
    deliverMean: FiniteDuration,
    deliverStdDev: FiniteDuration,
    generator: => Generator = Cmwc5.fromTime()
  ): F[DroneCtrlSleepRndEmulator[F]] =
    for {
      rndMove <- Rnd(gaussDist(moveMean, moveStdDev), generator)
      rndRot  <- Rnd(gaussDist(rotateMean, rotateStdDev), generator)
      rndDel  <- Rnd(gaussDist(deliverMean, deliverStdDev), generator)
    } yield new DroneCtrlSleepRndEmulator(name, rndMove, rndRot, rndDel)

  private def gaussDist(mean: FiniteDuration, stdDev: FiniteDuration): Dist[FiniteDuration] =
    Gaussian(mean.toNanos.toDouble, stdDev.toNanos.toDouble).map(_.toLong.nanos)
}
