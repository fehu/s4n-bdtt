package com.github.fehu.s4nbdtt

import scala.Numeric.Implicits._
import scala.Ordering.Implicits._

trait Grid[N] {
  def isValid(position: Grid.Position[N]): Boolean

  def validate[L](position: Grid.Position[N], onInvalid: Grid.Position[N] => L): Either[L, Grid.Position[N]] =
    Either.cond(isValid(position), position, onInvalid(position))
}

object Grid {

  /** Position on a grid.
   *
   * @param x Horizontal position. West < East.
   * @param y Vertical position. South < North.
   */
  final case class Position[N] protected(x: N, y: N) {
    def unsafeMove(direction: Direction)(implicit num: Numeric[N]): Position[N] =
      Position.unsafeMove(this, direction)
    
    def move(direction: Direction, grid: Grid[N])(implicit num: Numeric[N]): Either[InvalidMove[N], Position[N]] =
      Position.move(this, direction, grid)

    def validate(grid: Grid[N]): Either[InvalidPosition[N], Position[N]] = grid.validate(this, InvalidPosition(grid, _))
  }
  
  object Position {
    def apply[N](grid: Grid[N], x: N, y: N): Either[InvalidPosition[N], Position[N]] =
      grid.validate(unsafe(x, y), InvalidPosition(grid, _))

    def unsafe[N](x: N, y: N): Position[N] = Position(x, y)

    def unsafeMove[N](position: Position[N], direction: Direction)(implicit num: Numeric[N]): Position[N] =
      direction match {
        case Direction.North => position.copy(y = position.y + num.one)
        case Direction.East  => position.copy(x = position.x + num.one)
        case Direction.South => position.copy(y = position.y - num.one)
        case Direction.West  => position.copy(x = position.x - num.one)
      }

    /** Assumes that current position is valid. */
    def move[N: Numeric](position: Position[N], direction: Direction, grid: Grid[N]): Either[InvalidMove[N], Position[N]] =
      grid.validate(unsafeMove(position, direction), InvalidMove(grid, position, direction, _))
  }

  sealed trait Invalid
  final case class InvalidPosition[N](grid: Grid[N], invalid: Position[N]) extends Invalid
  final case class InvalidMove[N](grid: Grid[N], from: Position[N], move: Direction, invalid: Position[N]) extends Invalid
}

/**
 * A grid with center at {{{(0, 0)}}}.
 *  Vertical range: (North) {{{[-halfHeight, halfHeight]}}} (South)
 *  Horizontal range: (West) {{{[-halfWidth, halfWidth]}}} (East)
 */
class SymmetricZeroCenteredGrid[N: Numeric](halfHeight: N, halfWidth: N) extends Grid[N] {
  def isValid(position: Grid.Position[N]): Boolean =
    position.x >= -halfWidth  &&
    position.x <=  halfWidth  &&
    position.y >= -halfHeight &&
    position.y <=  halfHeight
}

object SymmetricZeroCenteredGrid {
  def apply[N: Numeric](cfg: Config.SymmetricGrid, fromInt: Int => N = identity[Int] _): SymmetricZeroCenteredGrid[N] =
    new SymmetricZeroCenteredGrid[N](fromInt(cfg.halfHeight.value), fromInt(cfg.halfWidth.value))
}
