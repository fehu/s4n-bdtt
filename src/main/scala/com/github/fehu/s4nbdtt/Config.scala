package com.github.fehu.s4nbdtt

import java.nio.file.Path

import cats.effect.Sync
import cats.syntax.either._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.pureconfig._
import pureconfig.{ ConfigReader, ConfigSource }
import pureconfig.error.{ ConfigReaderException, FailureReason }
import pureconfig.generic.auto._
import pureconfig.generic.semiauto.deriveReader

final case class Config(
  grid: Config.SymmetricGrid,
  drone: Config.Drone,
  drones: Int Refined Positive,
  routes: Config.Files,
  reports: Config.Reports
)

object Config {
  def defaultUnsafe: Config = ConfigSource.default.load[Config].valueOr(f => throw ConfigReaderException(f))

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
    commands: Drone.Commands
  )

  object Drone {
    final case class Commands(moveForward: Char, rotateLeft: Char, rotateRight: Char)

    object Commands {
      implicit def commandsConfigReader: ConfigReader[Commands] =
        deriveReader[Commands].emap {
          case Commands(mf, tl, _) if mf == tl => AmbiguousCommands(mf, "move forward", "rotate left").asLeft
          case Commands(mf, _, tr) if mf == tr => AmbiguousCommands(mf, "move forward", "rotate right").asLeft
          case Commands(_, tl, tr) if tl == tr => AmbiguousCommands(tl, "rotate left", "rotate right").asLeft
          case cmds                            => cmds.asRight
        }

      final case class AmbiguousCommands(ambiguous: Char, cmd1: String, cmd2: String) extends FailureReason {
        def description: String = s"""Same character '$ambiguous' is defined for commands "$cmd1" and "$cmd2"."""
      }
    }
  }

  final case class Files(path: Path, prefix: String, suffix: String)

  final case class Reports(
    files: Files,
    header: String,
    showDirection: Config.ShowDirection
  )

  final case class ShowDirection(
    north: String Refined NonEmpty,
    east: String Refined NonEmpty,
    south: String Refined NonEmpty,
    west: String Refined NonEmpty
  ) {
    def show(direction: Direction): String = direction match {
      case Direction.North => north.value
      case Direction.East  => east.value
      case Direction.South => south.value
      case Direction.West  => west.value
    }
  }
}
