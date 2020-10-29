package com.github.fehu.s4nbdtt

import cats.{ Order, Show }
import cats.data.{ NonEmptyList, Validated, ValidatedNel }
import cats.instances.list._
import cats.syntax.apply._
import cats.syntax.traverse._
import cats.syntax.show._
import cats.syntax.validated._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty

/** Parser for [[DroneRoute]]. */
class DroneRouteParser(cfg: Config.Drone.Commands) {
  import DroneProgDefinitionError.InvalidCommand

  def parseCmd(cmd: Char): Validated[InvalidCommand, DroneMv] = cmd match {
    case cfg.moveForward => DroneMv.MoveForward.valid
    case cfg.rotateLeft  => DroneMv.RotateLeft.valid
    case cfg.rotateRight => DroneMv.RotateRight.valid
    case _               => InvalidCommand(cmd).invalid
  }

  def parse(raw: DroneRouteParser.Raw): ValidatedNel[InvalidCommand, DroneRoute] =
    NonEmptyList.fromListUnsafe(raw.value.toList)
      .traverse(parseCmd _ andThen (_.toValidatedNel))
      .map(DroneRoute(_))
}

object DroneRouteParser {
  type Raw = String Refined NonEmpty
}

/** Parser for [[DroneProg]]. */
class DroneProgParser(cfg: Config.Drone) {
  import DroneProgDefinitionError._

  lazy val routeParser: DroneRouteParser = new DroneRouteParser(cfg.commands)

  def parse(raw: String): ValidatedNel[DroneProgDefinitionError, DroneProg] = {
    val rawRoutes = raw.split('\n').mapInPlace(_.trim).filter(_.nonEmpty)
    val validatedRoutes = rawRoutes
      .toList.zipWithIndex
      .traverse{ case (s, i) =>
        routeParser.parse(Refined.unsafeApply(s)) // `s` is never empty due to `.filter(_.nonEmpty)`
          .leftMap(InvalidCommands(i, s, _))
          .toValidatedNel
      }
    val validatedCount = rawRoutes.length match {
      case 0                           => NoRoutes.invalidNel
      case n if n > cfg.capacity.value => TooManyRoutes(n, cfg.capacity.value).invalidNel
      case _                           => ().validNel
    }
    (validatedCount *> validatedRoutes).map { routes =>
      // empty list case is handled by `validatedCount`
      DroneProg(NonEmptyList.fromListUnsafe(routes))
    }
  }
}


sealed trait DroneProgDefinitionError
object DroneProgDefinitionError {

  case class InvalidCommand(cmd: Char) extends AnyVal {
    override def toString: String = s"InvalidCommand($cmd)"
  }
  object InvalidCommand {
    implicit lazy val invalidCommandOrder: Order[InvalidCommand] = Order.by(_.cmd)
  }

  final case class InvalidCommands protected(
    index: Int,
    raw: String,
    invalid: NonEmptyList[InvalidCommand]
  ) extends DroneProgDefinitionError

  object InvalidCommands {
    def apply(index: Int, raw: String, invalid: NonEmptyList[InvalidCommand]): InvalidCommands =
      new InvalidCommands(index, raw, invalid.distinct)
  }

  case object NoRoutes extends DroneProgDefinitionError
  final case class TooManyRoutes(routes: Int, max: Int) extends DroneProgDefinitionError

  implicit lazy val showDroneProgDefinitionError: Show[DroneProgDefinitionError] =
    Show.show {
      case InvalidCommands(index, raw, invalid) =>
        val invalidStr = invalid.toList.map(i => s"'${i.cmd}'").mkString("[", ", ", "]")
        s"""Invalid drone command(s) $invalidStr at route "$raw""""
      case NoRoutes =>
        "No routes are defined."
      case TooManyRoutes(n, max) =>
        s"Too many routes defined for the drone: $n. Max: $max."
    }
}

class DroneProgramsParseException(failed: NonEmptyList[(String, NonEmptyList[DroneProgDefinitionError])])
  extends Exception({
    val indented = failed.toList.flatMap { case (name, errors) =>
      val shownErrs = errors.toList.map(err => s"    - ${err.show}")
      s"  $name:" :: shownErrs
    }
    s"""Errors on parsing drone program(s):
       |$indented
       |""".stripMargin
  })

object DroneProgramsParseException {
  def one(progName: String, errors: NonEmptyList[DroneProgDefinitionError]): DroneProgramsParseException =
    new DroneProgramsParseException(NonEmptyList.one(progName -> errors))
}
