package com.github.fehu.s4nbdtt

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.functor._

class DroneProgExecutor[F[_]: Monad](ctrl: DroneCtrl[F], initialState: DroneProgExecutor.State) {
  import DroneProgExecutor.State

  /** Execute given program synchronously, returning drone states upon making delivery.
   *  The drone starts at [[initialState]].
   */
  def exec(prog: DroneProg): F[NonEmptyList[State]] =
    prog.routes.foldM(List(initialState)) {
      case (hist@List(state, _), route) =>
        exec(state, route).map(_ :: hist)
    }.map(
      NonEmptyList fromListUnsafe _.reverse.tail
    )


  /** Execute given route synchronously, returning final drone state.
   *  Delivery is executed om route completion.
   */
  def exec(state: State, route: DroneRoute): F[State] =
    route.moves
      .foldM(state)(mv)
      .productL(ctrl.deliver)

  private def mv(state: State, mv: DroneMv): F[State] = mv match {
    case DroneMv.MoveForward => ctrl.moveForward as state.moveForward
    case DroneMv.RotateLeft  => ctrl.rotateLeft  as state.rotateLeft
    case DroneMv.RotateRight => ctrl.rotateRight as state.rotateRight
  }
}

object DroneProgExecutor {
  final case class State(x: Int, y: Int, orientation: Direction) {

    def moveForward: State = orientation match {
      case Direction.North => copy(y = y + 1)
      case Direction.East  => copy(x = x + 1)
      case Direction.South => copy(y = y - 1)
      case Direction.West  => copy(x = x - 1)
    }

    def rotateLeft: State = copy(orientation = orientation.rotateLeft)
    def rotateRight: State = copy(orientation = orientation.rotateRight)
  }
}