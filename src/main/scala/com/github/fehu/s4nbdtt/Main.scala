package com.github.fehu.s4nbdtt

import cats.effect.{ ExitCode, IO, IOApp }

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Config.default[IO]
      _ = println(config) // TODO
    } yield ExitCode.Success
}
