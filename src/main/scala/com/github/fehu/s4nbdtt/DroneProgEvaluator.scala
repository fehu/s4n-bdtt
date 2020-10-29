package com.github.fehu.s4nbdtt

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.bifunctor._
import cats.syntax.functor._

class DroneProgEvaluator[F[_]: Monad, N](
  moveForward: (Int, DroneState[N]) => F[DroneState[N]],
  rotateLeft: (Int, DroneState[N]) => F[DroneState[N]],
  rotateRight: (Int, DroneState[N]) => F[DroneState[N]]
) {

  def evalProg(initialState: DroneState[N], prog: DroneProg): F[NonEmptyList[DroneState[N]]] =
    prog.routes.zipWithIndex.foldM(List(initialState)) {
      case (hist @ state :: _, (route, i)) =>
        evalRoute(i, state, route).map(_ :: hist)
    }.map(
      NonEmptyList fromListUnsafe _.reverse.tail
    )

  def evalRoute(index: Int, state: DroneState[N], route: DroneRoute): F[DroneState[N]] =
    route.moves.foldM(state)(evalMove(index, _, _))

  def evalMove(routeIndex: Int, state: DroneState[N], mv: DroneMv): F[DroneState[N]] = mv match {
    case DroneMv.MoveForward => moveForward(routeIndex, state)
    case DroneMv.RotateLeft  => rotateLeft(routeIndex, state)
    case DroneMv.RotateRight => rotateRight(routeIndex, state)
  }

}

class DroneProgValidator[N: Numeric](grid: Grid[N]) {
  /** Validate the program, evaluating expected result. */
  def validate(initialState: DroneState[N], prog: DroneProg): Either[(Int, Grid.Invalid), NonEmptyList[DroneState[N]]] =
    initialState.position.validate(grid).leftWiden[Grid.Invalid].leftMap(0 -> _) *> evaluator.evalProg(initialState, prog)

  private val evaluator = new DroneProgEvaluator[Either[(Int, Grid.Invalid), *], N](
    moveForward = (routeIndex, state) => state.position
                                              .move(state.orientation, grid)
                                              .leftMap(routeIndex -> _)
                                              .map(p => state.copy(position = p)),
    rotateLeft  = (_, state) => Right(state.rotateLeft),
    rotateRight = (_, state) => Right(state.rotateRight)
  )
}

class DroneProgExecutor[F[_]: Monad, N: Numeric](ctrl: DroneCtrl[F], initialState: DroneState[N]) {
  /** Execute given program synchronously, returning drone states upon making delivery.
   *  The drone starts at [[initialState]].
   *  Delivery is executed on each route completion.
   */
  def exec(prog: DroneProg): F[NonEmptyList[DroneState[N]]] = evaluator.evalProg(initialState, prog)

  private val evaluator = new DroneProgEvaluator[F, N](
    moveForward = (_, state) => ctrl.moveForward as state.moveForward,
    rotateLeft  = (_, state) => ctrl.rotateLeft  as state.rotateLeft,
    rotateRight = (_, state) => ctrl.rotateRight as state.rotateRight
  ) {
    override def evalRoute(index: Int, state: DroneState[N], route: DroneRoute): F[DroneState[N]] =
      super.evalRoute(index, state, route) <* ctrl.deliver
  }
}
