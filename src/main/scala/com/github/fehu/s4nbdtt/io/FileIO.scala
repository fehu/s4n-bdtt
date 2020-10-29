package com.github.fehu.s4nbdtt.io

import java.io.{ File, FilenameFilter }

import cats.effect.Sync
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._

import com.github.fehu.s4nbdtt.Config

object FileIO {
  type SubName = String
  type Contents = String
  
  def readAll[F[_]](cfg: Config.Files)(implicit F: Sync[F]): F[List[(SubName, Contents)]] =
    for {
      dir <- F.catchNonFatal { cfg.path.toFile }
      _   <- F.unlessA(dir.isDirectory)(
               F.raiseError(new Exception(s"${cfg.path} is not a directory."))
             )
      fFilter = new FilenameFilter {
                  def accept(file: File, s: String): Boolean = s.startsWith(cfg.prefix) && s.endsWith(cfg.suffix)
                }
      files    <- F.delay { dir.listFiles(fFilter).toList }
      contents <- files.traverse(read[F])
      prefL     = cfg.prefix.length
      suffL     = cfg.suffix.length
      subNames  = files.map(_.getName.drop(prefL).dropRight(suffL))
    } yield subNames zip contents

  def read[F[_]](cfg: Config.Files, subName: SubName)(implicit F: Sync[F]): F[Contents] =
    for {
      file     <- F.catchNonFatal { cfg.path.resolve(s"${cfg.prefix}$subName${cfg.suffix}").toFile }
      contents <- read(file)
    } yield contents
    

  def read[F[_]](file: File)(implicit F: Sync[F]): F[Contents] =
    F.bracket(
      F.delay(scala.io.Source.fromFile(file))
    )(
      s => F.delay(s.mkString)
    )(
      s => F.delay(s.close())
    )
}
