package com.github.fehu.s4nbdtt

import cats.Parallel
import cats.data.NonEmptyList
import cats.effect.{ Resource, Sync }
import cats.syntax.alternative._
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.show._
import cats.syntax.traverse._
import _root_.io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import com.github.fehu.s4nbdtt.io.FileIO

abstract class DroneApp[F[_]: Parallel, N](implicit F: Sync[F], num: Numeric[N]) {
  def initialState: DroneState[N]
  def droneCtrl(name: String): Resource[F, DroneCtrl[F]]

  private lazy val logger = Slf4jLogger.getLogger[F]

  val config = Config.defaultUnsafe

  def runApp: F[Unit] = {
    for {
      // TODO
      // config   <- Config.default[F]
      _        <- logger.debug(s"Read app configuration: $config")
      rawProgs <- readPrograms(config)
      _        <- logger.info(s"Found ${rawProgs.length} drone programs")
      progs    <- parseAndValidate(config, rawProgs)
      _        <- logger.info("All programs passed validations.")
      _        <- logger.info("Beginning execution.")
      _ <- progs.parTraverse_ { case (name, prog) =>
             droneCtrl(name).use { ctrl =>
               val executor = new DroneProgExecutor(ctrl, initialState)
               for {
                 _   <- logger.info(s"""Start execution of program "$name"""")
                 res <- executor.exec(prog)
                 _   <- logger.info(s"""Program "$name" executed""")
                 _   <- writeReport(config.reports, name, res)
               } yield ()
             }
           }
      _ <- logger.info("All programs executed")
    } yield ()
  }.onError(logger.error(_)("Drone app execution error"))

  protected type ProgName = String
  protected type RawProg = String

  protected def readPrograms(cfg: Config): F[List[(ProgName, RawProg)]] =
    for {
      progs <- FileIO.readAll[F](cfg.routes)
      _     <- F.whenA(progs.isEmpty)(
                 F.raiseError(new NoProgramsFound(cfg.routes.path))
               )
      _     <- F.whenA(progs.length > cfg.drones.value)(
                 F.raiseError(new TooManyProgramsException(progs.length, cfg.drones.value))
               )
    } yield progs

  protected def parseAndValidate(cfg: Config, raw: List[(ProgName, RawProg)]): F[List[(ProgName, DroneProg)]] = {
    val parser    = new DroneProgParser(cfg.drone)
    val grid      = SymmetricZeroCenteredGrid(cfg.grid, fromInt = num.fromInt)
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
      .traverse(errors => F.raiseError[List[(ProgName, DroneProg)]](new DroneProgramsException(errors)))
      .as(progs)
  }

  protected def writeReport[N](cfg: Config.Reports, name: ProgName, result: NonEmptyList[DroneState[N]]): F[Unit] = {
    val report =
      s"""${cfg.header}
         |
         |${result.toList.map{ case DroneState(pos, dir) => s"$pos ${cfg.showDirection.show(dir)}" }.mkString("\n")}
         |""".stripMargin
    FileIO.write[F](cfg.files, name, report) *> logger.debug(s"Wrote report $name")
  }
}
