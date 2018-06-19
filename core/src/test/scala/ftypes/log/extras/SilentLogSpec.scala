package ftypes.log.extras

import cats.effect.IO
import ftypes.log.{Logger, Logging}
import ftypes.log.extras.SilentLog.LogMessage
import org.scalatest.{FlatSpec, Matchers}

class SilentLogSpec extends FlatSpec with Matchers {

  def test(implicit L: Logging[IO]): IO[Unit] = for {
    _ <- L.trace("Go ahead and leave me...")
    _ <- L.debug("I think I'd prefer to stay inside...")
    _ <- L.warn("Maybe you'll find someone else to help you.")
    _ <- L.info("Maybe Black Mesa?")
    _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
  } yield ()

  "#messages" should "accumulate all log messages" in {
    implicit val log = SilentLog[IO]

    test.unsafeRunSync()
    
    log.messages should contain allOf (
      LogMessage(Trace, "Go ahead and leave me...", None),
      LogMessage(Debug, "I think I'd prefer to stay inside...", None),
      LogMessage(Warn, "Maybe you'll find someone else to help you.", None),
      LogMessage(Info, "Maybe Black Mesa?", None),
      LogMessage(Error, "That was a joke. Ha Ha. Fat Chance!", None)
    )
  }

  "#clear" should "remove all log messages" in {
    implicit val log = SilentLog[IO]

    test.unsafeRunSync()
    
    log.clear()
    log.messages shouldBe empty
  }
}

