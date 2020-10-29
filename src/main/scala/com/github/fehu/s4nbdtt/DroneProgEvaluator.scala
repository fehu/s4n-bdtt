package com.github.fehu.s4nbdtt

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.bifunctor._
import cats.syntax.functor._

class DroneProgEvaluator[F[_]: Monad, N](
  moveForward: DroneState[N] => F[DroneState[N]],
  rotateLeft: DroneState[N] => F[DroneState[N]],
  rotateRight: DroneState[N] => F[DroneState[N]]
) {

  def evalProg(initialState: DroneState[N], prog: DroneProg): F[NonEmptyList[DroneState[N]]] =
    prog.routes.foldM(List(initialState)) {
      case (hist @ state :: _, route) =>
        evalRoute(state, route).map(_ :: hist)
    }.map(
      NonEmptyList fromListUnsafe _.reverse.tail
    )

  def evalRoute(state: DroneState[N], route: DroneRoute): F[DroneState[N]] =
    route.moves.foldM(state)(evalMove)

  def evalMove(state: DroneState[N], mv: DroneMv): F[DroneState[N]] = mv match {
    case DroneMv.MoveForward => moveForward(state)
    case DroneMv.RotateLeft  => rotateLeft(state)
    case DroneMv.RotateRight => rotateRight(state)
  }

}

class DroneProgValidator[N: Numeric](grid: Grid[N]) {
  /** Validate the program, evaluating expected result. */
  def validate(initialState: DroneState[N], prog: DroneProg): Either[Grid.Invalid, NonEmptyList[DroneState[N]]] =
    initialState.position.validate(grid).leftWiden[Grid.Invalid] *> evaluator.evalProg(initialState, prog)

  private val evaluator = new DroneProgEvaluator[Either[Grid.Invalid, *], N](
    moveForward = state => state.position.move(state.orientation, grid).map(p => state.copy(position = p)),
    rotateLeft  = state => Right(state.rotateLeft),
    rotateRight = state => Right(state.rotateRight)
  )
}

class DroneProgExecutor[F[_]: Monad, N: Numeric](ctrl: DroneCtrl[F], initialState: DroneState[N]) {
  /** Execute given program synchronously, returning drone states upon making delivery.
   *  The drone starts at [[initialState]].
   *  Delivery is executed on each route completion.
   */
  def exec(prog: DroneProg): F[NonEmptyList[DroneState[N]]] = evaluator.evalProg(initialState, prog)

  private val evaluator = new DroneProgEvaluator[F, N](
    moveForward = state => ctrl.moveForward as state.moveForward,
    rotateLeft  = state => ctrl.rotateLeft  as state.rotateLeft,
    rotateRight = state => ctrl.rotateRight as state.rotateRight
  ) {
    override def evalRoute(state: DroneState[N], route: DroneRoute): F[DroneState[N]] =
      super.evalRoute(state, route) <* ctrl.deliver
  }
}
