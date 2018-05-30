package ftypes

import cats.effect.{IO, Sync}
import cats.implicits._
import ftypes.LoggingSpec.TestLogging
import org.scalatest.{FlatSpec, Matchers}

class LoggingSpec extends FlatSpec with Matchers {
  it should "implicitly resolved in the context by the effect class" in {
    val t = new TestLogging[IO]
    t.prog.unsafeRunSync()
    t.logger.getName shouldBe "ftypes.LoggingSpec.TestLogging"
  }
}

object LoggingSpec {
  class TestLogging[F[_]](implicit F: Sync[F], L: Logging[F]) {

    val logger = L.logger

    def prog: F[Unit] = for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
  }
}

