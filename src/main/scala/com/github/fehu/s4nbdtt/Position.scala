package com.github.fehu.s4nbdtt

import scala.Numeric.Implicits._

/** Position on a grid.
 *
 * @param x Horizontal position. West < East.
 * @param y Vertical position. South < North.
 */
final case class Position[N](x: N, y: N) {
  def move(direction: Direction)(implicit num: Numeric[N]): Position[N] =
    Position.move(this, direction)
}

object Position {
  def move[N](position: Position[N], direction: Direction)(implicit num: Numeric[N]): Position[N] =
    direction match {
      case Direction.North => position.copy(y = position.y + num.one)
      case Direction.East  => position.copy(x = position.x + num.one)
      case Direction.South => position.copy(y = position.y - num.one)
      case Direction.West  => position.copy(x = position.x - num.one)
    }
}
