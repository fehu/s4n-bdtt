package com.github.fehu.s4nbdtt.io

import java.io.{ File, FileWriter, FilenameFilter }

import cats.effect.Sync
import cats.instances.list._
import cats.syntax.apply._
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

  def read[F[_]: Sync](cfg: Config.Files, subName: SubName): F[Contents] =
    resolveFile[F](cfg, subName).flatMap(read[F])

  def read[F[_]](file: File)(implicit F: Sync[F]): F[Contents] =
    F.bracket(
      F.delay(scala.io.Source.fromFile(file))
    )(
      s => F.delay(s.mkString)
    )(
      s => F.delay(s.close())
    )

  def write[F[_]](cfg: Config.Files, subName: SubName, contents: Contents)(implicit F: Sync[F]): F[Unit] =
    F.delay { cfg.path.toFile.mkdirs() } *>
    resolveFile[F](cfg, subName).flatMap(write(_, contents))

  def write[F[_]](file: File, contents: Contents)(implicit F: Sync[F]): F[Unit] =
    F.delay { file.createNewFile() } *>
    F.bracket(
      F.delay(new FileWriter(file))
    )(
      w => F.delay(w.write(contents))
    )(
      w => F.delay(w.close())
    )

  private def resolveFile[F[_]: Sync](cfg: Config.Files, subName: SubName) =
    Sync[F].catchNonFatal {
      cfg.path.resolve(s"${cfg.prefix}$subName${cfg.suffix}").toFile
    }

}
