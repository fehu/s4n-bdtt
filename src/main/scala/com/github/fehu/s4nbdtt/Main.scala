package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList
import cats.effect.{ ExitCode, IO, IOApp }
import cats.syntax.alternative._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.show._
import cats.syntax.traverse._

import com.github.fehu.s4nbdtt.io.FileIO
import com.github.fehu.s4nbdtt.stub.DroneCtrlNoOp

object Main extends IOApp {
  val initialState = DroneState(0, 0, Direction.North)

  def run(args: List[String]): IO[ExitCode] =
    for {
      config   <- Config.default[IO]
      rawProgs <- readPrograms(config)
      progs    <- parseAndValidate(config, rawProgs)
      // Execute progs
      ctrl = new DroneCtrlNoOp[IO] // TODO: inject?
      executor = new DroneProgExecutor(ctrl, initialState)
      _ <- progs.parTraverse_ { case (name, prog) =>
             executor.exec(prog).flatMap(writeReport(config, name, _))
           }
      // Done
    } yield ExitCode.Success

  private type ProgName = String
  private type RawProg = String

  private def readPrograms(cfg: Config): IO[List[(ProgName, RawProg)]] =
    for {
      progs <- FileIO.readAll[IO](cfg.routes)
      _     <- IO.raiseWhen(progs.isEmpty)(
                 new NoProgramsFound(cfg.routes.path)
               )
      _     <- IO.raiseWhen(progs.length > cfg.drones.value)(
                 new TooManyProgramsException(progs.length, cfg.drones.value)
               )
    } yield progs

  private def parseAndValidate(cfg: Config, raw: List[(ProgName, RawProg)]): IO[List[(ProgName, DroneProg)]] = {
    val parser    = new DroneProgParser(cfg.drone)
    val grid      = SymmetricZeroCenteredGrid(cfg.grid)
    val validator = new DroneProgValidator(grid)

    val (failed, progs) = raw.map { case (name, raw) =>
      parser.parse(raw)
        .leftMap(name -> _.map(_.show))
        .andThen { prog =>
          validator.validate(initialState, prog)
            .leftMap { case (routeIndex, invalid) => name -> NonEmptyList.of(s"Route $routeIndex: ${invalid.show}") }
            .toValidated
            .as(name -> prog)
        }
    }.separate

    NonEmptyList.fromList(failed)
      .traverse(errors => IO.raiseError(new DroneProgramsException(errors)))
      .as(progs)
  }

  private def writeReport[N](cfg: Config, name: ProgName, result: NonEmptyList[DroneState[N]]): IO[Unit] = {
    val report =
      s"""${cfg.reportHeader}
         |
         |${result.toList.map{ case DroneState(pos, dir) => s"$pos ${cfg.showDirection.show(dir)}" }.mkString("\n")}
         |""".stripMargin
    FileIO.write[IO](cfg.reports, name, report)
  }
}
