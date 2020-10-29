package com.github.fehu.s4nbdtt

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.validated._
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import com.github.fehu.s4nbdtt.io.FileIO
import com.github.fehu.s4nbdtt.test.DroneCtrlNoOp

class DroneProgExecutorPredefinedTest extends AsyncWordSpec with AsyncIOSpec with Matchers  {
  import DroneProgExecutor.State
  import Direction._

  val state0 = State(0, 0, North)

  val expected1 = NonEmptyList.of(
    State(-2, 4, West),
    State(-1, 3, South),
    State(0,  0, West)
  )

  val expected2 = NonEmptyList.of(
    State(-2,  0, West),
    State(-1,  0, East),
    State(-2, -2, West)
  )

  val expected3 = NonEmptyList.of(
    State(8, -5, South)
  )

  private lazy val cfg = Config.default[IO].unsafeRunSync()
  private lazy val progParser = new DroneProgParser(cfg.drone)

  private lazy val droneCtrl = new DroneCtrlNoOp[IO]
  private lazy val progExecutor = new DroneProgExecutor[IO](droneCtrl, state0)

  def test(subName: String, expected: NonEmptyList[State]): IO[Assertion] =
    for {
      raw  <- FileIO.read[IO](cfg.routes, subName)
      prog <- progParser.parse(raw).leftMap(DroneProgramsParseException.one(subName, _)).liftTo[IO]
      res  <- progExecutor.exec(prog)
    } yield res shouldBe expected

  "DroneProgExecutor" should {
    "work as expected for example `in01.txt`" in test("01", expected1)
    "work as expected for example `in02.txt`" in test("02", expected2)
    "work as expected for example `in03.txt`" in test("03", expected3)
  }

}
