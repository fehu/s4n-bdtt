package com.github.fehu.s4nbdtt

sealed trait Direction {
  def rotateLeft: Direction = Direction.rotateLeft(this)
  def rotateRight: Direction = Direction.rotateRight(this)
}

object Direction {
  case object North extends Direction
  case object East  extends Direction
  case object South extends Direction
  case object West  extends Direction

  def rotateLeft(dir: Direction): Direction = dir match {
    case North => West
    case East  => North
    case South => East
    case West  => South
  }

  def rotateRight(dir: Direction): Direction = dir match {
    case North => East
    case East  => South
    case South => West
    case West  => North
  }
}
