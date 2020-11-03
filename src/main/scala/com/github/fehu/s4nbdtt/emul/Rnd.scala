package com.github.fehu.s4nbdtt.emul

import cats.FlatMap
import cats.effect.concurrent.{ MVar, MVar2 }
import cats.effect.Concurrent
import cats.syntax.flatMap._
import cats.syntax.functor._
import spire.random.{ Dist, Generator }

class Rnd[F[_]: FlatMap, A](val dist: Dist[A], generator: Generator, lock: MVar2[F, Unit]) {
  def gen: F[A] =
    for {
      _ <- lock.take
      next = generator.next(dist)
      _ <- lock.put(())
    } yield next
}

object Rnd {
  def apply[F[_]: Concurrent, A](dist: Dist[A], generator: Generator): F[Rnd[F, A]] =
    MVar[F].of(()).map(new Rnd(dist, generator, _))
}
