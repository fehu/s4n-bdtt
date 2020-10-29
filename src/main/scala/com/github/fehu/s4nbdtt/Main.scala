package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList
import cats.effect.{ ExitCode, IO, IOApp }
import cats.syntax.alternative._
import cats.syntax.bifunctor._
import cats.syntax.traverse._
import cats.syntax.parallel._

import com.github.fehu.s4nbdtt.io.FileIO
import com.github.fehu.s4nbdtt.stub.DroneCtrlNoOp

object Main extends IOApp {
  val initialState = DroneState(0, 0, Direction.North)

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Config.default[IO]
      // Parse progs
      progs0 <- FileIO.readAll[IO](config.routes)
      _      <- IO.whenA(progs0.length > config.drones.value)(
                  IO.raiseError(new TooManyProgramsException(progs0.length, config.drones.value))
                )
      parser  = new DroneProgParser(config.drone)
      (failed0, progs) = progs0.map { case (name, raw) => parser.parse(raw).bimap(name -> _, name -> _) }.separate
      failedOpt = NonEmptyList.fromList(failed0)
      _        <- failedOpt.traverse(errors => IO.raiseError(new DroneProgramsParseException(errors)))
      // Validate progs
      grid      = SymmetricZeroCenteredGrid(config.grid)
      validator = new DroneProgValidator(grid)
      (invalid0, _) = progs.map { case (name, prog) => validator.validate(initialState, prog).bimap(name -> _, name -> _) }.separate
      invalidOpt = NonEmptyList.fromList(invalid0)
      _         <- invalidOpt.traverse(errors => IO.raiseError(new Exception(s"TODO: $errors"))) // TODO
      // Execute progs
      ctrl = new DroneCtrlNoOp[IO] // TODO: inject?
      executor = new DroneProgExecutor(ctrl, initialState)
      _ <- progs.parTraverse_ { case (name, prog) =>
             executor.exec(prog).flatMap(res => IO { println(s"$name: $res") }) // TODO
           }
      // Done
    } yield ExitCode.Success

  class TooManyProgramsException(progs: Int, drones: Int) extends Exception(
    s"$progs programs found, but there are only $drones drones."
  )
}
