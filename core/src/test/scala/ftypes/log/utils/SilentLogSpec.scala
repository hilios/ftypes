package ftypes.log.utils

import cats.effect.{IO, Sync}
import cats.implicits._
import ftypes.log.{Logger, Logging}
import ftypes.log.utils.SilentLog.LogMessage
import ftypes.log.utils.SilentLogSpec.TestLogger
import org.scalatest.{FlatSpec, Matchers}

class SilentLogSpec extends FlatSpec with Matchers {

  it should "accumulate all logs in a internal list" in {

    implicit val log = SilentLog[IO]

    new TestLogger[IO].prog.unsafeRunSync()
    log.messages should contain allOf (
      LogMessage(Trace, "Go ahead and leave me...", None),
      LogMessage(Debug, "I think I'd prefer to stay inside...", None),
      LogMessage(Warn, "Maybe you'll find someone else to help you.", None),
      LogMessage(Info, "Maybe Black Mesa?", None),
      LogMessage(Error, "That was a joke. Ha Ha. Fat Chance!", None)
    )
  }
}

object SilentLogSpec {
  class TestLogger[F[_]](implicit F: Sync[F], L: Logging[F]) {

    val logger = L

    implicit val l = Logger.withName("test")

    def prog: F[Unit] = for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
  }
}
