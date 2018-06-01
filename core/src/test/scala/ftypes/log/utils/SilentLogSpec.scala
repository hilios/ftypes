package ftypes.log.utils

import cats.effect.{IO, Sync}
import ftypes.log.Logging
import ftypes.log.utils.SilentLogSpec.TestLogging
import org.scalatest.{FlatSpec, Matchers}

class SilentLogSpec extends FlatSpec with Matchers {
  it should "accumulate all logs in a internal list" in {
    implicit val log = SilentLog[IO]

    val t = TestLogging[IO]
    
  }
}

object SilentLogSpec {
  class TestLogging[F[_]](implicit F: Sync[F], L: Logging[F]) {

    val logger = L

    def prog: F[Unit] = for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
  }
}
