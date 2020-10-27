package com.github.fehu.s4nbdtt

import cats.effect.{ ExitCode, IO, IOApp }

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = IO.pure(ExitCode.Success)
}
