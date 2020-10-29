package com.github.fehu.s4nbdtt

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.functor._

class DroneProgExecutor[F[_]: Monad, N: Numeric](ctrl: DroneCtrl[F], initialState: DroneState[N]) {

  /** Execute given program synchronously, returning drone states upon making delivery.
   *  The drone starts at [[initialState]].
   */
  def exec(prog: DroneProg): F[NonEmptyList[DroneState[N]]] =
    prog.routes.foldM(List(initialState)) {
      case (hist @ state :: _, route) =>
        exec(state, route).map(_ :: hist)
    }.map(
      NonEmptyList fromListUnsafe _.reverse.tail
    )


  /** Execute given route synchronously, returning final drone state.
   *  Delivery is executed om route completion.
   */
  def exec(state: DroneState[N], route: DroneRoute): F[DroneState[N]] =
    route.moves
      .foldM(state)(mv)
      .productL(ctrl.deliver)

  private def mv(state: DroneState[N], mv: DroneMv): F[DroneState[N]] = mv match {
    case DroneMv.MoveForward => ctrl.moveForward as state.moveForward
    case DroneMv.RotateLeft  => ctrl.rotateLeft  as state.rotateLeft
    case DroneMv.RotateRight => ctrl.rotateRight as state.rotateRight
  }
}
