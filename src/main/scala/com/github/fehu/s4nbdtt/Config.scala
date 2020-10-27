package com.github.fehu.s4nbdtt

import java.nio.file.Path

import cats.effect.Sync
import cats.syntax.either._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.pureconfig._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

final case class Config(
  grid: Config.SymmetricGrid,
  drone: Config.Drone,
  routes: Config.Files,
  reports: Config.Files
)

object Config {
  def default[F[_]: Sync]: F[Config] = load(ConfigSource.default)

  def load[F[_]](source: ConfigSource)(implicit sync: Sync[F]): F[Config] =
    sync.suspend {
      source.load[Config].leftMap(ConfigReaderException(_)).liftTo[F]
    }

  final case class SymmetricGrid(
    halfHeight: Int Refined Positive,
    halfWidth: Int Refined Positive,
  )

  final case class Drone(
    capacity: Int Refined Positive,
    count: Int Refined Positive,
    commands: Drone.Commands
  )

  object Drone {
    final case class Commands(moveForward: Char, turnLeft: Char, turnRight: Char)
  }

  final case class Files(path: Path, prefix: String, suffix: String)
}
