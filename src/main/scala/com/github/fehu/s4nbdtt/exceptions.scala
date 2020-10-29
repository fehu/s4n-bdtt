package com.github.fehu.s4nbdtt

import java.nio.file.Path

import cats.data.NonEmptyList

class DroneProgramsException(
  failed: NonEmptyList[(DroneProgramsException.ProgramName, NonEmptyList[DroneProgramsException.ErrorDescription])]
) extends Exception(
  {
    val indented = failed.toList.flatMap { case (name, errors) =>
      val shownErrs = errors.toList.map(err => s"    - $err")
      s"  $name:" :: shownErrs
    }
    s"""Errors on parsing drone program(s):
       |${indented.mkString("\n")}
       |""".stripMargin
  }
)

object DroneProgramsException {
  type ProgramName = String
  type ErrorDescription = String
}

class NoProgramsFound(path: Path) extends Exception(
  s"No programs were found at ${path.toAbsolutePath}."
)

class TooManyProgramsException(progs: Int, drones: Int) extends Exception(
  s"$progs programs found, but there are only $drones drones."
)
